package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.recipe.GmcSmelteryRecipes;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipeLookup;

/** Prevents EV+ materials from remaining meltable through Tinkers' auxiliary lookup. */
@Mixin(value = MeltingRecipeLookup.class, remap = false)
public abstract class GmcMeltingRecipeLookupMixin {
    @ModifyVariable(method = "addMeltingFluid", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int superflatcore$normalizeLookupTemperature(int temperature, Ingredient input, FluidOutput result,
                                                                int originalTemperature) {
        return GmcSmelteryRecipes.meltingTemperature(result.get().getFluid(), temperature);
    }

    @Inject(method = "addMeltingFluid", at = @At("HEAD"), cancellable = true)
    private static void superflatcore$excludeHighTierMelting(Ingredient input, FluidOutput result, int temperature,
                                                             CallbackInfo ci) {
        if (GmcSmelteryRecipes.isBlockedFluid(result.get().getFluid())) ci.cancel();
    }
}
