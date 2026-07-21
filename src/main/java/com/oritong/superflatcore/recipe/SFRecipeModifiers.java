package com.oritong.superflatcore.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;

public final class SFRecipeModifiers {
    public static final RecipeModifier MULTIPLERECIPE = new MultipleRecipeMarkerModifier();

    private SFRecipeModifiers() {}

    public static boolean hasMultipleRecipeModifier(MachineDefinition definition) {
        return definition != null && contains(definition.getRecipeModifier());
    }

    public static boolean contains(RecipeModifier modifier) {
        if (modifier == null) {
            return false;
        }
        if (modifier == MULTIPLERECIPE || modifier instanceof MultipleRecipeMarkerModifier) {
            return true;
        }
        if (modifier instanceof RecipeModifierList list) {
            for (RecipeModifier child : list.getModifiers()) {
                if (contains(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class MultipleRecipeMarkerModifier implements RecipeModifier {
        @Override
        public ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe) {
            return ModifierFunction.IDENTITY;
        }

        @Override
        public String toString() {
            return "GTRecipeModifiers.MULTIPLERECIPE";
        }
    }
}
