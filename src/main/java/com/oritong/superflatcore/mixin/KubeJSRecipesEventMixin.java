package com.oritong.superflatcore.mixin;

import com.google.gson.JsonElement;
import com.oritong.superflatcore.recipe.PackRecipeManagerAccess;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipesEventJS.class, remap = false)
public abstract class KubeJSRecipesEventMixin {
    // KubeJS cancels RecipeManager.apply and assigns its maps directly, so neither
    // the vanilla apply tail nor replaceRecipes runs for the server's recipe set.
    @Inject(method = "post", at = @At("TAIL"))
    private void superflatcore$finalizeScriptRecipes(RecipeManager manager,
                                                     Map<ResourceLocation, JsonElement> datapackRecipes,
                                                     CallbackInfo ci) {
        ((PackRecipeManagerAccess) manager).superflatcore$finalizeRecipes();
    }
}
