package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.moreburners.MoreBurnersEmberBurnerAccess;
import com.oritong.superflatcore.integration.moreburners.MoreBurnersIntegration;
import com.rekindled.embers.api.power.IEmberCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.dragonegg.moreburners.content.block.entity.EmberBurnerBlockEntity", remap = false)
public abstract class MoreBurnersEmberBurnerMixin implements MoreBurnersEmberBurnerAccess {
    @Unique
    private boolean superflatcore$cycleWorking;

    @Unique
    private int superflatcore$cycleTicks;

    @Override
    public boolean superflatcore$isCycleWorking() {
        return superflatcore$cycleWorking;
    }

    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private void superflatcore$advanceCycle(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!level.isClientSide()) {
            superflatcore$cycleTicks = superflatcore$cycleTicks % 100 + 1;
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/rekindled/embers/api/power/IEmberCapability;getEmber()D",
                    ordinal = 2),
            remap = false)
    private double superflatcore$keepCycleWorking(IEmberCapability capability) {
        double current = capability.getEmber();
        if (superflatcore$cycleTicks == 1) {
            return current >= MoreBurnersIntegration.getCycleEmberCost()
                    ? Math.max(current, MoreBurnersIntegration.getConfiguredEmberCost())
                    : 0.0;
        }
        if (superflatcore$cycleWorking) {
            return Math.max(current, MoreBurnersIntegration.getConfiguredEmberCost());
        }
        return 0.0;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/rekindled/embers/api/power/IEmberCapability;removeAmount(DZ)D",
                    ordinal = 2),
            remap = false)
    private double superflatcore$consumeCycle(IEmberCapability capability, double ignoredAmount, boolean doRemove) {
        if (superflatcore$cycleTicks == 1) {
            double cycleCost = MoreBurnersIntegration.getCycleEmberCost();
            if (capability.getEmber() >= cycleCost) {
                superflatcore$cycleWorking = true;
                return capability.removeAmount(cycleCost, doRemove);
            }
            superflatcore$cycleWorking = false;
            return 0.0;
        }
        return 0.0;
    }
}
