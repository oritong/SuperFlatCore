package com.oritong.superflatcore.mixin.botania;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.WandOfTheForestItem;

@Mixin(value = PlayerHelper.class, remap = false)
public abstract class PlayerHelperMixin {
    @Inject(method = "hasHeldItemClass", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$acceptDominionWandHeld(Player player, Class<?> template,
                                                             CallbackInfoReturnable<Boolean> cir) {
        if (template == WandOfTheForestItem.class
                && (DominionWandBotaniaIntegration.isDominionWand(player.getMainHandItem())
                || DominionWandBotaniaIntegration.isDominionWand(player.getOffhandItem()))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getFirstHeldItemClass", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$getDominionWandHeld(LivingEntity living, Class<?> template,
                                                          CallbackInfoReturnable<ItemStack> cir) {
        if (template != WandOfTheForestItem.class) {
            return;
        }

        ItemStack mainHand = living.getMainHandItem();
        if (DominionWandBotaniaIntegration.isDominionWand(mainHand)) {
            cir.setReturnValue(mainHand);
            return;
        }

        ItemStack offHand = living.getOffhandItem();
        if (DominionWandBotaniaIntegration.isDominionWand(offHand)) {
            cir.setReturnValue(offHand);
        }
    }

    @Inject(method = "getItemClassFromInventory", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$getDominionWandFromInventory(Player player, Class<?> template,
                                                                   CallbackInfoReturnable<ItemStack> cir) {
        if (template != WandOfTheForestItem.class) {
            return;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (DominionWandBotaniaIntegration.isDominionWand(stack)) {
                cir.setReturnValue(stack);
                return;
            }
        }
    }
}
