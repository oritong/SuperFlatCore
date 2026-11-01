package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.create.CorundumSpoutingIntegration;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpoutBlockEntity.class, remap = false)
public abstract class CreateSpoutCorundumMixin {
    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private void superflatcore$startCorundumSpouting(CallbackInfo ci) {
        CorundumSpoutingIntegration.startSpoutProcessIfNeeded((SpoutBlockEntity) (Object) this);
    }
}
