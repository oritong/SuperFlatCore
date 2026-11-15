package com.oritong.superflatcore.blackhole;

import com.oritong.superflatcore.registry.SFBlockEntities;
import com.oritong.superflatcore.registry.SFBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


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
            boolean absorbedReinforcedDeepslate = entity.currentlyPulling != null
                    && entity.currentlyPulling.is(Blocks.REINFORCED_DEEPSLATE);
            entity.currentlyPullingFrom = null;
            entity.currentlyPulling = null;
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            if (absorbedReinforcedDeepslate && level instanceof ServerLevel serverLevel) {
                triggerEchoShardReaction(serverLevel, pos);
            }
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

    private static void triggerEchoShardReaction(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        level.explode(null, x, y, z, 3.5F, false, Level.ExplosionInteraction.NONE);

        level.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 120, 1.1, 1.0, 1.1, 0.38);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 128, 1.15, 1.0, 1.15, 0.3);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 64, 0.9, 0.8, 0.9, 0.16);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 48, 0.75, 0.75, 0.75, 0.12);
        spawnSonicWave(level, x, y, z);
        level.playSound(null, pos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 3.0F, 0.72F);

        ItemEntity shard = new ItemEntity(level, x, y + 0.65, z, new ItemStack(Items.ECHO_SHARD));
        shard.setDeltaMovement(0, 0.35, 0);
        level.addFreshEntity(shard);
    }

    private static void spawnSonicWave(ServerLevel level, double x, double y, double z) {
        final int pointsPerWave = 32;
        final double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int wave = 0; wave < 3; wave++) {
            double startRadius = 0.2 + wave * 0.45;
            double speed = 0.28 + wave * 0.12;
            for (int i = 0; i < pointsPerWave; i++) {
                double unitY = 1.0 - 2.0 * (i + 0.5) / pointsPerWave;
                double planarRadius = Math.sqrt(1.0 - unitY * unitY);
                double angle = i * goldenAngle + wave * 0.31;
                double directionX = Math.cos(angle) * planarRadius;
                double directionZ = Math.sin(angle) * planarRadius;
                level.sendParticles(ParticleTypes.SONIC_BOOM,
                        x + directionX * startRadius,
                        y + unitY * startRadius,
                        z + directionZ * startRadius,
                        0, directionX * speed, unitY * speed, directionZ * speed, 1.0);
            }
        }
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
