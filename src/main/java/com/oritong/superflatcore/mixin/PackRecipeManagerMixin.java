package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.oritong.superflatcore.recipe.ComponentAssemblyRecipes;
import com.oritong.superflatcore.recipe.PackRecipeRestrictions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class PackRecipeManagerMixin {
    @Shadow private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;
    @Shadow private Map<ResourceLocation, Recipe<?>> byName;

    // Cover both datapack reloads and recipes received from a server or replaced by scripts.
    @Inject(method = {"apply", "replaceRecipes"}, at = @At("TAIL"))
    private void superflatcore$removePackRecipes(CallbackInfo ci) {
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> keptTypes = new LinkedHashMap<>();
        Map<ResourceLocation, Recipe<?>> keptByName = new LinkedHashMap<>(byName);
        recipes.forEach((type, entries) -> {
            ResourceLocation typeId = BuiltInRegistries.RECIPE_TYPE.getKey(type);
            boolean removeType = typeId != null
                    && PackRecipeRestrictions.removesRecipeType(typeId.getNamespace(), typeId.getPath());
            Map<ResourceLocation, Recipe<?>> kept = new LinkedHashMap<>();
            entries.forEach((id, recipe) -> {
                if (removeType
                        || PackRecipeRestrictions.removesManualEnergyHatchRecipe(type, id, recipe)
                        || recipe instanceof IDisplayModifierRecipe
                        || recipe instanceof ToolBuildingRecipe) {
                    keptByName.remove(id);
                } else {
                    kept.put(id, recipe);
                }
            });
            keptTypes.put(type, Collections.unmodifiableMap(kept));
        });
        ComponentAssemblyRecipes.populate(keptTypes, keptByName);
        superflatcore$rebuildGregTechLookups(keptTypes);
        recipes = Collections.unmodifiableMap(keptTypes);
        byName = Collections.unmodifiableMap(keptByName);
    }

    /**
     * GTCEu clones vanilla crafting recipes into its own lookup database. Rebuild
     * those lookups from the filtered recipe maps so removed manual hatches cannot
     * remain visible through GTCEu or EMI after KubeJS has injected recipes.
     */
    private static void superflatcore$rebuildGregTechLookups(
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> keptTypes) {
        for (RecipeType<?> recipeType : ForgeRegistries.RECIPE_TYPES) {
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                continue;
            }

            gtRecipeType.beginStagingRecipes();
            gtRecipeType.getProxyRecipes().forEach((proxyType, proxyRecipes) -> {
                Map<ResourceLocation, Recipe<?>> entries = keptTypes.get(proxyType);
                if (entries != null) {
                    RecipeManagerHandler.addProxyRecipesToLookup(
                            entries, gtRecipeType, proxyType, proxyRecipes);
                }
            });

            Map<ResourceLocation, Recipe<?>> entries = keptTypes.get(gtRecipeType);
            if (entries != null) {
                RecipeManagerHandler.addRecipesToLookup(entries, gtRecipeType);
            }
            gtRecipeType.getAdditionHandler().completeStaging();
        }
        MapIngredientPool.clear();
    }
}
