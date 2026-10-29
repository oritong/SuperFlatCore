package com.oritong.superflatcore.mixin.client;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

/** Keeps KubeJS placeholder items out of EMI's item index and tag previews. */
@Mixin(value = EmiStackList.class, remap = false)
public abstract class EmiLostItemStackMixin {
    @org.spongepowered.asm.mixin.Shadow
    public static List<Predicate<EmiStack>> invalidators;

    @Unique
    private static final Predicate<EmiStack> superflatcore$hideLostItem = EmiLostItemStackMixin::superflatcore$isLostItem;

    @Inject(method = "bake", at = @At("HEAD"), remap = false)
    private static void superflatcore$registerLostItemFilter(CallbackInfo ci) {
        if (!invalidators.contains(superflatcore$hideLostItem)) {
            invalidators.add(superflatcore$hideLostItem);
        }
    }

    @Unique
    private static boolean superflatcore$isLostItem(EmiStack stack) {
        ResourceLocation id = stack.getId();
        return id != null && id.equals(new ResourceLocation("kubejs", "lost_item"));
    }
}
