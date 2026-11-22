package com.oritong.superflatcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import java.util.List;

@Mixin(value = MaterialMeltingRecipe.class, remap = false)
public interface GmcMaterialMeltingRecipeAccessor {
    @Accessor("result")
    FluidOutput superflatcore$getResult();

    @Accessor("byproducts")
    List<FluidOutput> superflatcore$getByproducts();

    @Accessor("temperature")
    int superflatcore$getTemperature();

    @Mutable
    @Accessor("temperature")
    void superflatcore$setTemperature(int temperature);

    @Accessor("multiRecipes")
    void superflatcore$setMultiRecipes(List<MeltingRecipe> recipes);
}
