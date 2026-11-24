package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeAmperageEnergyContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeAmperageEnergyContainer.class, remap = false)
public abstract class SmallGeneratorEnergyOutputMixin {
    @Shadow @Final protected IRecipeLogicMachine machine;

    @Inject(method = "getOutputAmperage", at = @At("HEAD"), cancellable = true)
    private void superflatcore$twoAmpSmallGeneratorOutput(CallbackInfoReturnable<Long> cir) {
        // GT derives output amperage from the running recipe. Doubling the
        // generation budget alone would still export one packet per tick.
        if (machine instanceof SimpleGeneratorMachine) {
            cir.setReturnValue(2L);
        }
    }
}
