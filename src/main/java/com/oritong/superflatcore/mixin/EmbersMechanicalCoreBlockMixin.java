package com.oritong.superflatcore.mixin;

import com.rekindled.embers.block.MechanicalCoreBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MechanicalCoreBlock.class, remap = false)
public abstract class EmbersMechanicalCoreBlockMixin {
    @Inject(method = "onNeighborChange", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$ignoreMissingBlockEntity(BlockState state, LevelReader level,
                                                         BlockPos pos, BlockPos neighbor,
                                                         CallbackInfo ci) {
        if (level.getBlockEntity(pos) == null) ci.cancel();
    }
}
