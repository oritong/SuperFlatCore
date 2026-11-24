package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.recipe.GmcSmelteryRecipes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipe;

import java.util.List;

@Mixin(value = MaterialMeltingRecipe.class, remap = false)
public abstract class GmcMaterialMeltingRecipeMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lslimeknights/tconstruct/library/materials/definition/MaterialVariantId;ILslimeknights/mantle/recipe/helper/FluidOutput;Ljava/util/List;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int superflatcore$setMaterialTemperature(int temperature, ResourceLocation id,
                                                           MaterialVariantId input, int originalTemperature,
                                                           FluidOutput result, List<FluidOutput> byproducts) {
        return GmcSmelteryRecipes.meltingTemperature(result.get().getFluid(), temperature);
    }
}
