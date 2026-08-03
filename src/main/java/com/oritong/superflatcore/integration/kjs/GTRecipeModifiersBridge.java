package com.oritong.superflatcore.integration.kjs;

import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.oritong.superflatcore.recipe.SFRecipeModifiers;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class GTRecipeModifiersBridge {
    public static final Function<OverclockingLogic, RecipeModifier> ELECTRIC_OVERCLOCK = GTRecipeModifiers.ELECTRIC_OVERCLOCK;
    public static final RecipeModifier OC_PERFECT = GTRecipeModifiers.OC_PERFECT;
    public static final RecipeModifier OC_NON_PERFECT = GTRecipeModifiers.OC_NON_PERFECT;
    public static final RecipeModifier OC_PERFECT_SUBTICK = GTRecipeModifiers.OC_PERFECT_SUBTICK;
    public static final RecipeModifier OC_NON_PERFECT_SUBTICK = GTRecipeModifiers.OC_NON_PERFECT_SUBTICK;
    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT =
            GTRecipeModifiers.ENVIRONMENT_REQUIREMENT;
    public static final RecipeModifier DEFAULT_ENVIRONMENT_REQUIREMENT = GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT;
    public static final RecipeModifier PARALLEL_HATCH = GTRecipeModifiers.PARALLEL_HATCH;
    public static final RecipeModifier BATCH_MODE = GTRecipeModifiers.BATCH_MODE;
    public static final RecipeModifier MULTIPLERECIPE = SFRecipeModifiers.MULTIPLERECIPE;
    public static final RecipeModifier PROPORTIONAL_INPUT = SFRecipeModifiers.PROPORTIONAL_INPUT;

    private GTRecipeModifiersBridge() {}

    public static RecipeModifierList list(RecipeModifier... modifiers) {
        return new RecipeModifierList(modifiers);
    }

    public static ModifierFunction hatchParallel(MetaMachine machine, GTRecipe recipe) {
        return GTRecipeModifiers.hatchParallel(machine, recipe);
    }

    public static ModifierFunction batchMode(MetaMachine machine, GTRecipe recipe) {
        return GTRecipeModifiers.batchMode(machine, recipe);
    }

    public static ModifierFunction crackerOverclock(MetaMachine machine, GTRecipe recipe) {
        return GTRecipeModifiers.crackerOverclock(machine, recipe);
    }

    public static ModifierFunction ebfOverclock(MetaMachine machine, GTRecipe recipe) {
        return GTRecipeModifiers.ebfOverclock(machine, recipe);
    }

    public static ModifierFunction pyrolyseOvenOverclock(MetaMachine machine, GTRecipe recipe) {
        return GTRecipeModifiers.pyrolyseOvenOverclock(machine, recipe);
    }

    public static ModifierFunction multiSmelterParallel(MetaMachine machine, GTRecipe recipe) {
        return GTRecipeModifiers.multiSmelterParallel(machine, recipe);
    }
}
