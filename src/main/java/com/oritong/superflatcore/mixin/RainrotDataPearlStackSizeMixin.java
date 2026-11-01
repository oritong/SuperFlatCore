package com.oritong.superflatcore.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class RainrotDataPearlStackSizeMixin {
    private static final ResourceLocation DATA_PEARL_ID = new ResourceLocation("rainrot", "data_pearl");

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void superflatcore$setDataPearlStackSize(CallbackInfoReturnable<Integer> callback) {
        if (DATA_PEARL_ID.equals(BuiltInRegistries.ITEM.getKey((Item) (Object) this))) {
            callback.setReturnValue(64);
        }
    }
}
