package com.oritong.superflatcore.mixin.botania;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import vazkii.botania.common.entity.ManaSparkEntity;
import vazkii.botania.common.item.BotaniaItems;

@Mixin(value = ManaSparkEntity.class, remap = false)
public abstract class ManaSparkEntityMixin {
    @Redirect(method = "m_6096_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;m_41720_()Lnet/minecraft/world/item/Item;", ordinal = 0),
            remap = false)
    private Item superflatcore$recognizeDominionWand(ItemStack stack) {
        // Only extend Botania's wand type check. Its own interaction handles
        // upgrades, transfer cleanup and removal across Botania versions.
        return DominionWandBotaniaIntegration.isDominionWand(stack) ? BotaniaItems.twigWand : stack.getItem();
    }
}
