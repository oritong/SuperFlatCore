package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.mekanism.MekanismRadiationIntegration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.reactor.block.ReactorMemberBlock", remap = false)
public abstract class AtomicReactorMemberBlockMixin {
    @Inject(method = "m_6810_", at = @At("HEAD"), remap = false)
    private void superflatcore$radiateOnRemove(BlockState state, Level level, BlockPos pos,
                                                BlockState newState, boolean moving,
                                                CallbackInfo ci) {
        if (!moving && state.getBlock() != newState.getBlock() && level instanceof ServerLevel server) {
            MekanismRadiationIntegration.radiateReactorAt(server, pos);
        }
    }
}
