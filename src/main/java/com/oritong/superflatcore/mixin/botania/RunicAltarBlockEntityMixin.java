package com.oritong.superflatcore.mixin.botania;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;

@Mixin(value = RunicAltarBlockEntity.class, remap = false)
public abstract class RunicAltarBlockEntityMixin {
    @Inject(method = "addItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$doNotInsertDominionWand(Player player, ItemStack stack, InteractionHand hand,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (DominionWandBotaniaIntegration.isDominionWand(stack)) {
            cir.setReturnValue(false);
        }
    }
}
