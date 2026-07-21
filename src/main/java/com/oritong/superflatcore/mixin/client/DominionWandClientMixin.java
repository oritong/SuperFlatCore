package com.oritong.superflatcore.mixin.client;

import com.hollingsworth.arsnouveau.common.items.DominionWand;
import com.oritong.superflatcore.client.DominionWandClientIntegration;
import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DominionWand.class, remap = false)
public abstract class DominionWandClientMixin {
    @Inject(method = "m_7203_", at = @At("TAIL"), cancellable = true, remap = false)
    private void superflatcore$showBotaniaWandModeName(Level level, Player player, InteractionHand hand,
                                                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (DominionWandBotaniaIntegration.canToggleBotaniaMode(player, stack)
                && DominionWandClientIntegration.isLookingAtAir(player)) {
            DominionWandBotaniaIntegration.use(level, player, hand);
            DominionWandClientIntegration.showSelectedItemName(stack);
            cir.setReturnValue(InteractionResultHolder.success(stack));
        }
    }
}
