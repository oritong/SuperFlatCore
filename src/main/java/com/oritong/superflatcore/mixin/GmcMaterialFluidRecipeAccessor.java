package com.oritong.superflatcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;

@Mixin(value = MaterialFluidRecipe.class, remap = false)
public interface GmcMaterialFluidRecipeAccessor {
    @Accessor("fluid")
    FluidIngredient superflatcore$getFluid();

    @Mutable
    @Accessor("temperature")
    void superflatcore$setTemperature(int temperature);
}
