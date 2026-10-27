package com.oritong.superflatcore.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.oritong.superflatcore.SuperFlatCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ComponentAssemblyRecipes {
    private static final String GENERATED_PREFIX = "component_assembly_line/from_assembler/";

    private ComponentAssemblyRecipes() {}

    public static void populate(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes,
                                Map<ResourceLocation, Recipe<?>> byName) {
        var type = SFComponentRecipeTypes.COMPONENT_ASSEMBLY_LINE_RECIPES;
        Map<ResourceLocation, Recipe<?>> copies = new LinkedHashMap<>(recipes.getOrDefault(type, Map.of()));
        // Rebuild only our derived recipes. Server sync and repeated reloads must never compound the multipliers.
        copies.keySet().removeIf(ComponentAssemblyRecipes::isGenerated);
        byName.keySet().removeIf(ComponentAssemblyRecipes::isGenerated);
        for (Recipe<?> value : recipes.getOrDefault(GTRecipeTypes.ASSEMBLER_RECIPES, Map.of()).values()) {
            if (!(value instanceof GTRecipe source) || !hasComponentOutput(source)) {
                continue;
            }
            ResourceLocation sourceId = source.getId();
            ResourceLocation id = SuperFlatCore.id(GENERATED_PREFIX + sourceId.getNamespace() + "/" + sourceId.getPath());
            GTRecipe copy = copyRecipe(source, id);
            copies.put(id, copy);
            byName.put(id, copy);
        }
        recipes.put(type, Collections.unmodifiableMap(copies));

        // KubeJS may have built GT's lookup before RecipeManager.replaceRecipes. Refresh this type from the final map.
        type.beginStagingRecipes();
        RecipeManagerHandler.addRecipesToLookup(copies, type);
        type.getAdditionHandler().completeStaging();
        MapIngredientPool.clear();
    }

    private static boolean isGenerated(ResourceLocation id) {
        return SuperFlatCore.MODID.equals(id.getNamespace()) && id.getPath().startsWith(GENERATED_PREFIX);
    }

    private static boolean hasComponentOutput(GTRecipe recipe) {
        for (Content output : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            for (var stack : ItemRecipeCapability.CAP.of(output.content).getItems()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!stack.isEmpty() && ComponentAssemblyRecipeRules.isComponent(itemId.getNamespace(), itemId.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static GTRecipe copyRecipe(GTRecipe source, ResourceLocation id) {
        GTRecipe copied = source.copy();
        var type = SFComponentRecipeTypes.COMPONENT_ASSEMBLY_LINE_RECIPES;
        var energyInputs = copied.tickInputs.get(EURecipeCapability.CAP);
        if (energyInputs != null) {
            var scaled = new ArrayList<Content>(energyInputs.size());
            for (Content input : energyInputs) {
                var energy = EURecipeCapability.CAP.of(input.content);
                scaled.add(new Content(energy.withVoltage(ComponentAssemblyRecipeRules.scaleVoltage(energy.voltage())),
                        input.chance, input.maxChance, input.tierChanceBoost));
            }
            copied.tickInputs.put(EURecipeCapability.CAP, scaled);
        }
        return new GTRecipe(type, id, copied.inputs, copied.outputs, copied.tickInputs, copied.tickOutputs,
                copied.inputChanceLogics, copied.outputChanceLogics, copied.tickInputChanceLogics,
                copied.tickOutputChanceLogics, copied.conditions, copied.ingredientActions, copied.data.copy(),
                ComponentAssemblyRecipeRules.scaleDuration(source.duration), type.getCategory(), copied.groupColor);
    }
}
