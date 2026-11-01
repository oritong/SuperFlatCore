package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.moreburners.MoreBurnersIntegration;
import com.rekindled.embers.blockentity.MiniBoilerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MiniBoilerBlockEntity.class, remap = false)
public abstract class EmbersMiniBoilerMixin {
    @Inject(method = "serverTick", at = @At("TAIL"), remap = false)
    private static void superflatcore$heatFromMoreBurners(Level level, BlockPos pos, BlockState state,
                                                           MiniBoilerBlockEntity boiler, CallbackInfo ci) {
        MoreBurnersIntegration.heatMiniBoiler(level, pos, boiler);
    }
}
