package com.oritong.superflatcore.mixin;

import com.google.gson.JsonElement;
import com.oritong.superflatcore.recipe.GmcSmelteryRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;

/** Changes both recipe display and the fuel lookup before the constructor registers the fuel. */
@Mixin(value = MeltingFuel.class, remap = false)
public abstract class TinkersMeltingFuelMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static FluidIngredient superflatcore$replaceBlazingBlood(FluidIngredient input) {
        JsonElement original = input.serialize();
        JsonElement replacement = GmcSmelteryRules.replaceBlazingBloodFuel(original);
        return replacement == original ? input : FluidIngredient.LOADABLE.convert(replacement, "fluid");
    }
}
