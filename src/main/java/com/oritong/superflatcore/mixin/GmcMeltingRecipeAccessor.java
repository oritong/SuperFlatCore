package com.oritong.superflatcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

@Mixin(value = MeltingRecipe.class, remap = false)
public interface GmcMeltingRecipeAccessor {
    @Mutable
    @Accessor("temperature")
    void superflatcore$setTemperature(int temperature);
}
