package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.oritong.superflatcore.event.MachineSoulDispleasureManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class RecipeLogicMixin {
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$applyMachineSoulDispleasure(CallbackInfo ci) {
        MetaMachine machine = ((MachineTrait) (Object) this).getMachine();
        if (MachineSoulDispleasureManager.shouldSkipRecipeTick(machine)) {
            ci.cancel();
        }
    }
}
