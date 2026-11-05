package com.oritong.superflatcore.blackhole;

import com.oritong.superflatcore.registry.SFBlockEntities;
import com.oritong.superflatcore.registry.SFBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


/** Server-side black-hole suction state ported from Oritech, without accelerator output logic. */
public final class BlackHoleBlockEntity extends BlockEntity {
    public BlockState currentlyPulling;
    public BlockPos currentlyPullingFrom;
    public long pullingStartedAt;
    public long pullTime;
    private int waitTicks;

    public BlackHoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlackHoleBlockEntity entity) {
        if (level.isClientSide) {
            return;
        }
        if (entity.waitTicks-- > 0) {
            return;
        }
        if (entity.currentlyPullingFrom != null
                && entity.pullingStartedAt + entity.pullTime - 5 < level.getGameTime()) {
            entity.currentlyPullingFrom = null;
            entity.currentlyPulling = null;
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if (entity.currentlyPullingFrom != null) {
            return;
        }

        for (BlockPos candidate : BlockPos.withinManhattan(pos, 16, 16, 16)) {
            BlockState candidateState = level.getBlockState(candidate);
            if (candidate.equals(pos) || candidateState.isAir()
                    || candidateState.is(SFBlocks.BLACK_HOLE_BLACKLIST)
                    || !candidateState.getFluidState().isEmpty()
                    || candidateState.getBlock() == Blocks.MOVING_PISTON
                    || candidateState.getBlock() == SFBlocks.BLACK_HOLE.get()) {
                continue;
            }
            entity.currentlyPullingFrom = candidate.immutable();
            entity.currentlyPulling = candidateState;
            entity.pullingStartedAt = level.getGameTime();
            entity.pullTime = Math.max(1L, (long) candidate.distManhattan(pos) * 8L);
            level.setBlockAndUpdate(candidate, Blocks.AIR.defaultBlockState());
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }
        entity.waitTicks = 200;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeSync(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readSync(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeSync(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readSync(tag);
    }

    private void writeSync(CompoundTag tag) {
        if (currentlyPullingFrom != null) {
            tag.putLong("PullingFrom", currentlyPullingFrom.asLong());
            tag.putLong("PullingStartedAt", pullingStartedAt);
            tag.putLong("PullTime", pullTime);
            if (currentlyPulling != null) {
                tag.put("PullingState", NbtUtils.writeBlockState(currentlyPulling));
            }
        } else {
            tag.remove("PullingFrom");
            tag.remove("PullingState");
        }
    }

    private void readSync(CompoundTag tag) {
        if (tag.contains("PullingFrom")) {
            currentlyPullingFrom = BlockPos.of(tag.getLong("PullingFrom"));
            pullingStartedAt = tag.getLong("PullingStartedAt");
            pullTime = tag.getLong("PullTime");
            currentlyPulling = tag.contains("PullingState")
                    ? NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("PullingState"))
                    : null;
        } else {
            currentlyPullingFrom = null;
            currentlyPulling = null;
        }
    }
}
