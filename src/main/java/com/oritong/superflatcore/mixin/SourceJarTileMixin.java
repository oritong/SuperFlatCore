package com.oritong.superflatcore.mixin;

import com.hollingsworth.arsnouveau.common.block.SourceJar;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SourceJarTile.class, remap = false)
public abstract class SourceJarTileMixin {
    private static final int SUPERFLATCORE_SOURCE_CAPACITY = 128_000;

    @Inject(method = "getMaxSource", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$expandSourceCapacity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(SUPERFLATCORE_SOURCE_CAPACITY);
    }

    @Inject(method = "updateBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$scaleFillLevel(CallbackInfoReturnable<Boolean> cir) {
        SourceJarTile tile = (SourceJarTile) (Object) this;
        BlockEntity blockEntity = tile;
        if (blockEntity.getLevel() == null) {
            cir.setReturnValue(false);
            return;
        }

        BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
        if (state.hasProperty(SourceJar.fill)) {
            int source = Math.max(0, Math.min(tile.getSource(), SUPERFLATCORE_SOURCE_CAPACITY));
            int fill = source == 0 ? 0 : (int) (((long) source * 11 + SUPERFLATCORE_SOURCE_CAPACITY - 1)
                    / SUPERFLATCORE_SOURCE_CAPACITY);
            BlockState filledState = state.setValue(SourceJar.fill, fill);
            if (filledState != state) {
                blockEntity.getLevel().setBlock(blockEntity.getBlockPos(), filledState, 3);
            } else {
                blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, 3);
            }
        } else {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, 3);
        }
        blockEntity.setChanged();
        cir.setReturnValue(true);
    }
}
