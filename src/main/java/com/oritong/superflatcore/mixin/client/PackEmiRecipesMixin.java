package com.oritong.superflatcore.mixin.client;

import com.oritong.superflatcore.recipe.PackRecipeRestrictions;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.registry.EmiRecipes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = EmiRecipes.class, remap = false)
public abstract class PackEmiRecipesMixin {
    @Shadow public static List<Predicate<EmiRecipe>> invalidators;

    @Inject(method = "addCategory", at = @At("HEAD"), cancellable = true)
    private static void superflatcore$hideCategory(EmiRecipeCategory category, CallbackInfo ci) {
        if (superflatcore$hiddenCategory(category)) ci.cancel();
    }

    @Inject(method = "addWorkstation", at = @At("HEAD"), cancellable = true)
    private static void superflatcore$hideWorkstation(EmiRecipeCategory category, EmiIngredient workstation,
                                                     CallbackInfo ci) {
        if (superflatcore$hiddenCategory(category)) ci.cancel();
    }

    @Inject(method = "addRecipe", at = @At("HEAD"), cancellable = true)
    private static void superflatcore$hideRecipe(EmiRecipe recipe, CallbackInfo ci) {
        if (superflatcore$hiddenRecipe(recipe)) ci.cancel();
    }

    @Inject(method = "bake", at = @At("HEAD"))
    private static void superflatcore$filterDeferredRecipes(CallbackInfo ci) {
        // Resource-defined and deferred recipes may bypass addRecipe.
        invalidators.add(PackEmiRecipesMixin::superflatcore$hiddenRecipe);
    }

    @Unique
    private static boolean superflatcore$hiddenCategory(EmiRecipeCategory category) {
        ResourceLocation id = category.getId();
        return PackRecipeRestrictions.hidesCategory(id.getNamespace(), id.getPath());
    }

    @Unique
    private static boolean superflatcore$hiddenRecipe(EmiRecipe recipe) {
        if (superflatcore$hiddenCategory(recipe.getCategory())) return true;
        ResourceLocation category = recipe.getCategory().getId();
        if (!category.equals(new ResourceLocation("industrialforegoing", "machine_produce"))) return false;
        return recipe.getOutputs().isEmpty() || recipe.getOutputs().stream()
                .anyMatch(stack -> !PackRecipeRestrictions.keepsIndustrialOutput(stack.getId().toString()));
    }
}
