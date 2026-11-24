package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.recipe.GmcSmelteryRecipes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;

@Mixin(value = MaterialFluidRecipe.class, remap = false)
public abstract class GmcMaterialFluidRecipeMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int superflatcore$setCastingMaterialTemperature(int temperature, ResourceLocation id,
                                                                  FluidIngredient fluid, int originalTemperature,
                                                                  MaterialVariantId input, MaterialVariantId output) {
        return GmcSmelteryRecipes.materialFluidTemperature(fluid, temperature);
    }
}
