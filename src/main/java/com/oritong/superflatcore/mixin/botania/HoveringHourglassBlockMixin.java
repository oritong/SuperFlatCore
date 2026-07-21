package com.oritong.superflatcore.mixin.botania;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.block.HoveringHourglassBlock;

@Mixin(value = HoveringHourglassBlock.class, remap = false)
public abstract class HoveringHourglassBlockMixin {
    @Inject(method = "m_6227_", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$passDominionWand(BlockState state, Level level, BlockPos pos, Player player,
                                                InteractionHand hand, BlockHitResult hit,
                                                CallbackInfoReturnable<InteractionResult> cir) {
        if (DominionWandBotaniaIntegration.isDominionWand(player.getItemInHand(hand))) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
