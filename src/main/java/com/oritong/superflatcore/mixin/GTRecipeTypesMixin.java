package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTRecipeTypes.class, remap = false)
public abstract class GTRecipeTypesMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void superflatcore$configureRecipeSlots(CallbackInfo ci) {
        GTRecipeTypes.COMPRESSOR_RECIPES.setMaxIOSize(1, 1, 1, 1);
        GTRecipeTypes.LASER_ENGRAVER_RECIPES.setMaxIOSize(2, 1, 1, 0);
        GTRecipeTypes.PACKER_RECIPES.setMaxIOSize(3, 2, 0, 0);
    }
}
