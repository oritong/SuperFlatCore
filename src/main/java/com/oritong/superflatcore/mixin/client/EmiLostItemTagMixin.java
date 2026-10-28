package com.oritong.superflatcore.mixin.client;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = TagEmiIngredient.class, remap = false)
public abstract class EmiLostItemTagMixin {
    @Inject(method = "getEmiStacks", at = @At("RETURN"), cancellable = true, remap = false)
    private void superflatcore$filterLostItems(CallbackInfoReturnable<List<EmiStack>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(stack -> !EmiLostItemStackMixin.superflatcore$isLostItem(stack))
                .toList());
    }
}
