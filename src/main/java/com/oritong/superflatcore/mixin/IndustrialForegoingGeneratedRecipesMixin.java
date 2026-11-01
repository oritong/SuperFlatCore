package com.oritong.superflatcore.mixin;

import java.util.List;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.buuz135.industrial.plugin.jei.JEICustomPlugin", remap = false)
public abstract class IndustrialForegoingGeneratedRecipesMixin {
    @Redirect(
            method = "registerRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/registration/IRecipeRegistration;addRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/List;)V",
                    remap = false),
            remap = false)
    private <T> void superflatcore$removeGeneratedRecipes(IRecipeRegistration registration, RecipeType<T> type, List<T> recipes) {
        ResourceLocation recipeType = type.getUid();
        if ("industrialforegoing".equals(recipeType.getNamespace())
                && ("fermenter".equals(recipeType.getPath())
                || "ore_washer".equals(recipeType.getPath())
                || "ore_sieve".equals(recipeType.getPath())
                || recipeType.getPath().startsWith("mycelial_"))) {
            return;
        }
        registration.addRecipes(type, recipes);
    }
}
