package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.data.recipe.misc.CustomToolRecipes;
import com.oritong.superflatcore.recipe.ToolAssemblyRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = CustomToolRecipes.class, remap = false)
public class GTCustomToolRecipesMixin {
    @ModifyVariable(method = "init", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private static Consumer<FinishedRecipe> superflatcore$assembleCustomTools(Consumer<FinishedRecipe> provider) {
        return ToolAssemblyRecipes.wrap(provider);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private static void superflatcore$machineRecipes(Consumer<FinishedRecipe> provider, CallbackInfo ci) {
        ToolAssemblyRecipes.registerMachineRecipes(provider);
    }
}
