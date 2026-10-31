package com.oritong.superflatcore.mixin.client;

import mezz.jei.api.ingredients.ITypedIngredient;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.stream.Stream;

/** Bridges the JEI 15.21+ list API to the ingredients exposed by EMI's JEMI slot. */
@Mixin(value = JemiRecipeSlot.class, remap = false)
public abstract class JemiRecipeSlotIngredientListMixin {
    @Shadow(remap = false)
    public abstract Stream<ITypedIngredient<?>> getAllIngredients();

    public List<ITypedIngredient<?>> getAllIngredientsList() {
        return getAllIngredients().toList();
    }
}
