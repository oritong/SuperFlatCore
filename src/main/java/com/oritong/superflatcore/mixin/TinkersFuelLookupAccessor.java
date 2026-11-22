package com.oritong.superflatcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;

@Mixin(value = MeltingFuelLookup.class, remap = false)
public interface TinkersFuelLookupAccessor {
    @Accessor("LISTENER")
    static RecipeCacheInvalidator.DuelSidedListener superflatcore$getListener() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
