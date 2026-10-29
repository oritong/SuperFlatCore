package com.oritong.superflatcore.mixin.client;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = TagEmiIngredient.class, remap = false)
public abstract class EmiLostItemTagMixin {
    @Inject(method = "getEmiStacks", at = @At("RETURN"), cancellable = true, remap = false)
    private void superflatcore$filterLostItems(CallbackInfoReturnable<List<EmiStack>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(stack -> !superflatcore$isLostItem(stack))
                .toList());
    }

    private static boolean superflatcore$isLostItem(EmiStack stack) {
        ResourceLocation id = stack.getId();
        return id != null && id.equals(new ResourceLocation("kubejs", "lost_item"));
    }
}
