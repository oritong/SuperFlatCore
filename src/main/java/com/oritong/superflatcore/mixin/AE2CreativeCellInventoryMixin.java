package com.oritong.superflatcore.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "appeng.me.cells.CreativeCellInventory", remap = false)
public abstract class AE2CreativeCellInventoryMixin {
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$rejectRadioactive(AEKey what, long amount, Actionable mode, IActionSource source,
                                                 CallbackInfoReturnable<Long> cir) {
        if (what instanceof AEFluidKey fluidKey
                && com.oritong.superflatcore.integration.radioactive.RadioactiveFluidPolicy
                        .isRadioactive(fluidKey.toStack(1))) {
            cir.setReturnValue(0L);
        }
    }
}
