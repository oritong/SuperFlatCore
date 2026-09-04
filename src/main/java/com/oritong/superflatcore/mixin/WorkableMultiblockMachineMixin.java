package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.oritong.superflatcore.machine.MultipleRecipesLogic;
import com.oritong.superflatcore.recipe.SFRecipeModifiers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public abstract class WorkableMultiblockMachineMixin implements ICleanroomReceiver {
    @Inject(method = "createRecipeLogic", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$createMultipleRecipesLogic(Object[] args,
                                                          CallbackInfoReturnable<RecipeLogic> cir) {
        WorkableMultiblockMachine self = (WorkableMultiblockMachine) (Object) this;
        if (SFRecipeModifiers.hasMultipleRecipeModifier(self.getDefinition())) {
            cir.setReturnValue(new MultipleRecipesLogic((IRecipeLogicMachine) self));
        }
    }
}
