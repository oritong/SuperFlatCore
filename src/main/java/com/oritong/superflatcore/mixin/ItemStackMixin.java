package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void superflatcore$appendDominionWandBotaniaMode(CallbackInfoReturnable<Component> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!DominionWandBotaniaIntegration.isDominionWand(self)) {
            return;
        }

        cir.setReturnValue(DominionWandBotaniaIntegration.getNameWithBotaniaMode(self, cir.getReturnValue()));
    }
}
