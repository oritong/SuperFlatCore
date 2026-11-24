package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.recipe.GmcSmelteryRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import java.util.List;

/** Sets the temperature before the recipe and auxiliary lookup are created. */
@Mixin(value = MeltingRecipe.class, remap = false)
public abstract class GmcMeltingRecipeMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Lnet/minecraft/world/item/crafting/Ingredient;Lslimeknights/mantle/recipe/helper/FluidOutput;IILjava/util/List;Z)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int superflatcore$setMeltingTemperature(int temperature, ResourceLocation id, String group,
                                                          Ingredient input, FluidOutput output,
                                                          int originalTemperature, int time,
                                                          List<FluidOutput> byproducts, boolean addLookup) {
        return GmcSmelteryRecipes.meltingTemperature(output.get().getFluid(), temperature);
    }
}
