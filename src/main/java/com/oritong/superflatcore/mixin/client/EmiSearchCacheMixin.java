package com.oritong.superflatcore.mixin.client;

import com.oritong.superflatcore.integration.emi.EmiSearchCache;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiSearch.class, remap = false)
public abstract class EmiSearchCacheMixin {
    @Inject(method = "bake", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$loadSearchCache(CallbackInfo ci) {
        if (EmiSearchCache.load()) {
            ci.cancel();
        }
    }

    @Inject(method = "bake", at = @At("RETURN"), remap = false)
    private static void superflatcore$saveSearchCache(CallbackInfo ci) {
        EmiSearchCache.saveAsync();
    }
}
