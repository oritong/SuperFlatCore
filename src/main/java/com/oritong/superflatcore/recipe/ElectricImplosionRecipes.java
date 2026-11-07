package com.oritong.superflatcore.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Creates the electric implosion recipes from the final implosion recipe map. */
public final class ElectricImplosionRecipes {
    private static final String GENERATED_PREFIX = "electric_implosion_compressor/from_implosion/";

    private ElectricImplosionRecipes() {}

    public static void populate(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes,
                                Map<ResourceLocation, Recipe<?>> byName) {
        GTRecipeType type = SFRecipeTypes.ELECTRIC_IMPLOSION_COMPRESSOR_RECIPES;
        Map<ResourceLocation, Recipe<?>> copies = new LinkedHashMap<>(recipes.getOrDefault(type, Map.of()));
        copies.keySet().removeIf(ElectricImplosionRecipes::isGenerated);
        byName.keySet().removeIf(ElectricImplosionRecipes::isGenerated);

        for (Recipe<?> value : recipes.getOrDefault(GTRecipeTypes.IMPLOSION_RECIPES, Map.of()).values()) {
            if (!(value instanceof GTRecipe source)) {
                continue;
            }
            ResourceLocation sourceId = source.getId();
            ResourceLocation id = SuperFlatCore.id(GENERATED_PREFIX
                    + sourceId.getNamespace() + "/" + sourceId.getPath());
            GTRecipe copy = copyRecipe(source, id, type);
            if (copy == null) {
                continue;
            }
            copies.put(id, copy);
            byName.put(id, copy);
        }

        recipes.put(type, Collections.unmodifiableMap(copies));
        type.beginStagingRecipes();
        RecipeManagerHandler.addRecipesToLookup(copies, type);
        type.getAdditionHandler().completeStaging();
        MapIngredientPool.clear();
    }

    private static boolean isGenerated(ResourceLocation id) {
        return SuperFlatCore.MODID.equals(id.getNamespace())
                && id.getPath().startsWith(GENERATED_PREFIX);
    }

    private static GTRecipe copyRecipe(GTRecipe source, ResourceLocation id, GTRecipeType targetType) {
        Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> inputs =
                filterContents(source.inputs, true);
        Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> outputs =
                filterContents(source.outputs, false);
        Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> tickInputs =
                filterContents(source.tickInputs, true);
        Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> tickOutputs =
                filterContents(source.tickOutputs, false);

        // A source recipe with no real item input/output cannot be represented by this machine.
        if (inputs.getOrDefault(ItemRecipeCapability.CAP, List.of()).isEmpty()
                || outputs.getOrDefault(ItemRecipeCapability.CAP, List.of()).isEmpty()) {
            return null;
        }

        GTRecipe copy = new GTRecipe(targetType, id, inputs, outputs, tickInputs, tickOutputs,
                new IdentityHashMap<>(source.inputChanceLogics),
                new IdentityHashMap<>(source.outputChanceLogics),
                new IdentityHashMap<>(source.tickInputChanceLogics),
                new IdentityHashMap<>(source.tickOutputChanceLogics),
                new ArrayList<>(source.conditions),
                new ArrayList<>(source.ingredientActions),
                source.data.copy(), source.duration, targetType.getCategory(), source.groupColor);
        copy.parallels = source.parallels;
        copy.subtickParallels = source.subtickParallels;
        copy.batchParallels = source.batchParallels;
        copy.ocLevel = source.ocLevel;
        return copy;
    }

    private static Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> filterContents(
            Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> source,
            boolean input) {
        Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> result =
                new IdentityHashMap<>();
        source.forEach((capability, contents) -> {
            List<Content> filtered = new ArrayList<>(contents.size());
            for (Content content : contents) {
                if (capability == ItemRecipeCapability.CAP
                        && shouldRemoveItem(content, input)) {
                    continue;
                }
                filtered.add(content.copy(capability));
            }
            if (!filtered.isEmpty()) {
                result.put(capability, filtered);
            }
        });
        return result;
    }

    private static boolean shouldRemoveItem(Content content, boolean input) {
        Object raw = content.content;
        Ingredient ingredient = ItemRecipeCapability.CAP.of(raw);
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return false;
        }

        // Every implosion explosive is an item input. Remove all known GT/vanilla/mod variants,
        // including tag-based ingredients whose displayed stacks are explosive items.
        boolean explosive = true;
        for (ItemStack stack : stacks) {
            if (!isExplosive(stack)) {
                explosive = false;
                break;
            }
        }
        if (explosive) {
            return true;
        }

        // Dark ash is an implosion by-product. It must not appear in the electric copy either.
        for (ItemStack stack : stacks) {
            if (isDarkAsh(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDarkAsh(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getNamespace().equals("gtceu") && id.getPath().contains("dark_ash");
    }

    private static boolean isExplosive(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return false;
        }
        String path = id.getPath();
        return path.equals("tnt")
                || path.contains("powderbarrel")
                || path.contains("powder_barrel")
                || path.contains("dynamite")
                || path.contains("tnt")
                || path.contains("bomb")
                || path.contains("explosive")
                || path.contains("explosives");
    }
}
