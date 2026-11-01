package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.industrial.IndustrialForegoingAugments;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.hrznstudio.titanium.item.AugmentWrapper", remap = false)
public abstract class IndustrialForegoingAugmentMixin {
    @Inject(method = "isAugment", at = @At("HEAD"), remap = false)
    private static void superflatcore$initializeAugmentRecognition(ItemStack stack,
                                                                  CallbackInfoReturnable<Boolean> callback) {
        IndustrialForegoingAugments.initializeMissingData(stack);
    }

    @Inject(method = "getAugment", at = @At("HEAD"), remap = false)
    private static void superflatcore$initializeAugmentProperties(ItemStack stack,
                                                                 CallbackInfoReturnable<?> callback) {
        IndustrialForegoingAugments.initializeMissingData(stack);
    }
}
