package com.oritong.superflatcore.mixin.client;

import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.platform.forge.EmiAgnosForge;
import dev.emi.emi.registry.EmiPluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Disables EMI's JEI bridge, which is incompatible with the pack's JEI API. */
@Mixin(value = EmiAgnosForge.class, remap = false)
public abstract class EmiAgnosForgeMixin {
    @Inject(method = "getPluginsAgnos", at = @At("RETURN"), cancellable = true, remap = false)
    private void superflatcore$removeJemi(CallbackInfoReturnable<List<EmiPluginContainer>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(container -> !(container.plugin() instanceof JemiPlugin))
                .toList());
    }
}
