package com.oritong.superflatcore.mixin.botania;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.entity.CorporeaSparkEntity;
import vazkii.botania.common.helper.PlayerHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CorporeaSparkEntity.class, remap = false)
public abstract class CorporeaSparkEntityMixin {
    @Shadow(remap = false)
    private void dropAndKill() {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private void restartNetwork() {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private static void displayRelatives(Player player, List<CorporeaSpark> checked, CorporeaSpark spark) {
        throw new AssertionError();
    }

    @Inject(method = "m_6096_", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$interactWithDominionWand(Player player, InteractionHand hand,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
        CorporeaSparkEntity self = (CorporeaSparkEntity) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        if (!self.isAlive() || stack.isEmpty() || !DominionWandBotaniaIntegration.isDominionWand(stack)) {
            return;
        }

        if (!self.level().isClientSide) {
            if (player.isShiftKeyDown() || !PlayerHelper.isTruePlayer(player)) {
                dropAndKill();
                if (self.isMaster()) {
                    restartNetwork();
                }
            } else {
                displayRelatives(player, new ArrayList<>(), self.getMaster());
            }
        }

        cir.setReturnValue(InteractionResult.sidedSuccess(self.level().isClientSide));
    }
}
