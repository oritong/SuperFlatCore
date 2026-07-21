package com.oritong.superflatcore.mixin.botania;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.entity.ManaSparkEntity;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.SparkAugmentItem;

import java.util.ArrayList;
import java.util.Set;

@Mixin(value = ManaSparkEntity.class, remap = false)
public abstract class ManaSparkEntityMixin {
    @Shadow(remap = false)
    public abstract SparkUpgradeType getUpgrade();

    @Shadow(remap = false)
    public abstract void setUpgrade(SparkUpgradeType upgrade);

    @Shadow(remap = false)
    private Set<ManaSpark> outgoingTransfers;

    @Shadow(remap = false)
    private ArrayList<ManaSpark> transfersTowardsSelfToRegister;

    @Shadow(remap = false)
    private void dropAndKill() {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private void notifyOthers(DyeColor network) {
        throw new AssertionError();
    }

    @Inject(method = "m_6096_", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$interactWithDominionWand(Player player, InteractionHand hand,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
        ManaSparkEntity self = (ManaSparkEntity) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        if (!self.isAlive() || stack.isEmpty() || !DominionWandBotaniaIntegration.isDominionWand(stack)) {
            return;
        }

        SparkUpgradeType upgrade = getUpgrade();
        if (!self.level().isClientSide) {
            if (player.isShiftKeyDown() || !PlayerHelper.isTruePlayer(player)) {
                if (upgrade != SparkUpgradeType.NONE) {
                    self.spawnAtLocation(SparkAugmentItem.getByType(upgrade), 0F);
                    setUpgrade(SparkUpgradeType.NONE);
                    outgoingTransfers.clear();
                    transfersTowardsSelfToRegister.clear();
                    notifyOthers(self.getNetwork());
                } else {
                    dropAndKill();
                }
            } else {
                SparkHelper.getSparksAround(self.level(), self.getX(), self.getY() + (self.getBbHeight() / 2.0),
                                self.getZ(), self.getNetwork())
                        .forEach(spark -> ManaSparkEntity.particleBeam(player, self, spark.entity()));
            }
        }

        cir.setReturnValue(InteractionResult.sidedSuccess(self.level().isClientSide));
    }
}
