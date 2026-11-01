package com.oritong.superflatcore.mixin;

import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mekanism.common.recipe.MekanismRecipeType", remap = false)
public abstract class MekanismSmeltingRecipeGenerationMixin {
    @Shadow(remap = false)
    @Final
    private ResourceLocation registryName;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getRecipesUncached", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$preventVanillaSmeltingRecipes(RecipeManager recipeManager, RegistryAccess registryAccess,
                                                              CallbackInfoReturnable<List<?>> cir) {
        if (!new ResourceLocation("mekanism", "smelting").equals(registryName)) {
            return;
        }
        cir.setReturnValue(recipeManager.getAllRecipesFor((RecipeType) (Object) this));
    }
}
