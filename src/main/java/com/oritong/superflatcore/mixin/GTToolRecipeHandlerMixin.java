package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler;
import com.oritong.superflatcore.recipe.ToolAssemblyRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(value = ToolRecipeHandler.class, remap = false)
public class GTToolRecipeHandlerMixin {
    @ModifyVariable(method = {"run", "addToolRecipe", "addDyeableToolRecipe", "addNetheriteToolRecipe"},
            at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private static Consumer<FinishedRecipe> superflatcore$assembleTools(Consumer<FinishedRecipe> provider) {
        return ToolAssemblyRecipes.wrap(provider);
    }
}
