package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import mekanism.generators.common.content.blocktype.BlockShapes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachineDefinition.class)
public abstract class WindGeneratorCollisionMixin {
    @Inject(method = "getShape(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$useMekanismWindShape(Direction direction,
                                                    CallbackInfoReturnable<VoxelShape> cir) {
        MachineDefinition definition = (MachineDefinition) (Object) this;
        if (!definition.getId().getNamespace().equals("superflatcore")
                || !definition.getId().getPath().equals("wind_generator")) {
            return;
        }

        int shapeIndex = direction.getAxis().isHorizontal() ? direction.ordinal() - 2 : 0;
        cir.setReturnValue(BlockShapes.WIND_GENERATOR[shapeIndex]);
    }
}
