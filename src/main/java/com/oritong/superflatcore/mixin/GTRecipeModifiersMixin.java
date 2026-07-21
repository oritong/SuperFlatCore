package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import com.oritong.superflatcore.integration.kjs.GTRecipeModifiersBridge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GregTechKubeJSPlugin.class, remap = false)
public class GTRecipeModifiersMixin {
    @Redirect(
            method = "registerBindings",
            at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/BindingsEvent;add(Ljava/lang/String;Ljava/lang/Object;)V")
    )
    private void superflatcore$replaceRecipeModifiersBinding(@Coerce Object event, String name, Object value) {
        if ("GTRecipeModifiers".equals(name)) {
            superflatcore$addBinding(event, name, GTRecipeModifiersBridge.class);
            return;
        }
        superflatcore$addBinding(event, name, value);
    }

    private static void superflatcore$addBinding(Object event, String name, Object value) {
        try {
            event.getClass().getMethod("add", String.class, Object.class).invoke(event, name, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to add KubeJS binding " + name, e);
        }
    }
}
