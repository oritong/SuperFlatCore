package com.oritong.superflatcore.mixin.client;

import com.haoict.tiab.client.renderer.TimeAcceleratorEntityRenderer;
import com.haoict.tiab.common.entities.TimeAcceleratorEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = TimeAcceleratorEntityRenderer.class, remap = false)
public abstract class TiabTimeAcceleratorRendererMixin {
    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0, remap = false)
    private String superflatcore$renderMachineSoulDispleasureMultiplier(
            String original,
            TimeAcceleratorEntity accelerator
    ) {
        return switch (accelerator.getTimeRate()) {
            case -1 -> "\u00a7cx0.5";
            case -2 -> "\u00a7cx0.25";
            default -> original;
        };
    }
}
