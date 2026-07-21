package com.oritong.superflatcore.mixin;

import com.hollingsworth.arsnouveau.common.items.DominionWand;
import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DominionWand.class, remap = false)
public abstract class DominionWandMixin {
    @Inject(method = "m_6225_", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$useAsBotaniaWand(UseOnContext context,
                                                CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = DominionWandBotaniaIntegration.useOn(context);
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$blockAirModeAfterBlockUse(Level level, Player player, InteractionHand hand,
                                                         CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()
                && DominionWandBotaniaIntegration.isDominionWand(stack)
                && DominionWandBotaniaIntegration.wasUsedOnBlockThisTick(player)) {
            cir.setReturnValue(InteractionResultHolder.pass(stack));
        }
    }

    @Inject(method = "m_7203_", at = @At("TAIL"), cancellable = true, remap = false)
    private void superflatcore$toggleBotaniaWandMode(Level level, Player player, InteractionHand hand,
                                                     CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && DominionWandBotaniaIntegration.canToggleBotaniaMode(player, stack)) {
            cir.setReturnValue(DominionWandBotaniaIntegration.use(level, player, hand));
        }
    }

    @Inject(method = "m_6883_", at = @At("TAIL"), remap = false)
    private void superflatcore$clearInvalidBotaniaBinding(ItemStack stack, Level level, Entity entity, int slot,
                                                          boolean selected, CallbackInfo ci) {
        DominionWandBotaniaIntegration.inventoryTick(stack, level, entity, slot, selected);
    }

    @Inject(method = "m_7373_", at = @At("TAIL"), remap = false)
    private void superflatcore$appendBotaniaWandMode(ItemStack stack, Level level, List<Component> tooltip,
                                                     TooltipFlag flag, CallbackInfo ci) {
        DominionWandBotaniaIntegration.appendBotaniaTooltip(stack, tooltip);
    }
}
