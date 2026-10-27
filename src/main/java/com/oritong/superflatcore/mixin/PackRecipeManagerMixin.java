package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.recipe.PackRecipeRestrictions;
import com.oritong.superflatcore.recipe.ComponentAssemblyRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
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
                if (removeType || recipe instanceof IDisplayModifierRecipe || recipe instanceof ToolBuildingRecipe) {
                    keptByName.remove(id);
                } else {
                    kept.put(id, recipe);
                }
            });
            keptTypes.put(type, Collections.unmodifiableMap(kept));
        });
        ComponentAssemblyRecipes.populate(keptTypes, keptByName);
        recipes = Collections.unmodifiableMap(keptTypes);
        byName = Collections.unmodifiableMap(keptByName);
    }
}
