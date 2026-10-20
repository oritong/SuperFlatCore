package com.oritong.superflatcore.mixin;

import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityNutritionalLiquifier.class, remap = false)
public abstract class MekanismNutritionalLiquifierMixin {
    // This machine creates food recipes on demand instead of reading RecipeManager.
    @Inject(method = "getRecipe(I)Lmekanism/api/recipes/ItemStackToFluidRecipe;",
            at = @At("HEAD"), cancellable = true)
    private void superflatcore$removeGeneratedFoodRecipes(int cacheIndex,
                                                          CallbackInfoReturnable<ItemStackToFluidRecipe> cir) {
        cir.setReturnValue(null);
    }
}
