package com.oritong.superflatcore.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class RecountItemDecorationPatch {
    private static final String RECOUNT_MIXIN = "meow.binary.o123456789.mixin.GuiGraphicsMixin";
    private static final String BRIDGE = "com/oritong/superflatcore/client/ForgeItemDecorationBridge";
    private static final String ARGUMENTS =
            "Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II";
    private static final String HANDLER_DESCRIPTOR = "(" + ARGUMENTS
            + "Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";

    private RecountItemDecorationPatch() {}

    public static void apply(ClassNode target) {
        for (MethodNode method : target.methods) {
            if (!method.desc.equals(HANDLER_DESCRIPTOR) || !belongsToRecount(method)) {
                continue;
            }
            MethodInsnNode cancel = null;
            for (var instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call)) {
                    continue;
                }
                // Do not duplicate the hook if a future Recount release provides it itself.
                if (call.owner.equals("net/minecraftforge/client/ItemDecoratorHandler")
                        || call.owner.equals(BRIDGE)) {
                    return;
                }
                if (call.owner.equals("org/spongepowered/asm/mixin/injection/callback/CallbackInfo")
                        && call.name.equals("cancel") && call.desc.equals("()V")) {
                    cancel = call;
                }
            }
            if (cancel == null) {
                throw new IllegalStateException("Recount item-decoration handler no longer has a cancellation hook");
            }

            // Recount draws counts/bars/cooldowns, then cancels vanilla's method before Forge's
            // ItemDecoratorHandler call. Insert that call at the end of Recount's handler.
            // The merged handler is an instance method on GuiGraphics; its argument slots are
            // this, font, stack, x, y, text, callback. Leave the callback receiver on the stack.
            InsnList callback = new InsnList();
            callback.add(new VarInsnNode(Opcodes.ALOAD, 0));
            callback.add(new VarInsnNode(Opcodes.ALOAD, 1));
            callback.add(new VarInsnNode(Opcodes.ALOAD, 2));
            callback.add(new VarInsnNode(Opcodes.ILOAD, 3));
            callback.add(new VarInsnNode(Opcodes.ILOAD, 4));
            callback.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "render",
                    "(Lnet/minecraft/client/gui/GuiGraphics;" + ARGUMENTS + ")V", false));
            method.instructions.insertBefore(cancel, callback);
            method.maxStack = Math.max(method.maxStack, 6);
            return;
        }
        throw new IllegalStateException("Could not find Recount's merged item-decoration handler");
    }

    private static boolean belongsToRecount(MethodNode method) {
        if (method.visibleAnnotations == null) {
            return false;
        }
        for (AnnotationNode annotation : method.visibleAnnotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;")) {
                continue;
            }
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (annotation.values.get(i).equals("mixin") && annotation.values.get(i + 1).equals(RECOUNT_MIXIN)) {
                    return true;
                }
            }
        }
        return false;
    }
}
