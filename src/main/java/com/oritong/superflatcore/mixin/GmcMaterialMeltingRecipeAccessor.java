package com.oritong.superflatcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipe;

import java.util.List;

@Mixin(value = MaterialMeltingRecipe.class, remap = false)
public interface GmcMaterialMeltingRecipeAccessor {
    @Accessor("result")
    FluidOutput superflatcore$getResult();

    @Accessor("byproducts")
    List<FluidOutput> superflatcore$getByproducts();
}
