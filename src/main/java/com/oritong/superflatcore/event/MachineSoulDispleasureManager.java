package com.oritong.superflatcore.event;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.haoict.tiab.common.entities.TimeAcceleratorEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

public final class MachineSoulDispleasureManager {
    private static final Map<MachineKey, Slowdown> ACTIVE_SLOWDOWNS = new HashMap<>();

    private MachineSoulDispleasureManager() {}

    public static void apply(ServerLevel level, BlockPos pos, int tickInterval, int durationTicks) {
        Slowdown activeSlowdown = getActiveSlowdown(level, pos);
        if (activeSlowdown != null && activeSlowdown.tickInterval() == 4) {
            ensureVisualMarker(level, pos, activeSlowdown.tickInterval(),
                    (int) Math.max(1L, activeSlowdown.expiresAt() - level.getServer().getTickCount()));
            return;
        }

        long expiresAt = level.getServer().getTickCount() + durationTicks;
        ACTIVE_SLOWDOWNS.put(new MachineKey(level.dimension(), pos.immutable()),
                new Slowdown(expiresAt, tickInterval));
        refreshVisualMarker(level, pos, tickInterval, durationTicks);
    }

    public static boolean shouldSkipRecipeTick(MetaMachine machine) {
        if (!(machine.getLevel() instanceof ServerLevel level)) {
            return false;
        }

        Slowdown slowdown = getActiveSlowdown(level, machine.getPos());
        if (slowdown == null) {
            return false;
        }

        return level.getServer().getTickCount() % slowdown.tickInterval() != 0;
    }

    public static SlowdownInfo getInfo(ServerLevel level, BlockPos pos) {
        Slowdown slowdown = getActiveSlowdown(level, pos);
        if (slowdown == null) {
            return null;
        }

        long remainingTicks = Math.max(0L, slowdown.expiresAt() - level.getServer().getTickCount());
        return new SlowdownInfo(1.0D / slowdown.tickInterval(), (int) Math.min(Integer.MAX_VALUE, remainingTicks));
    }

    public static void clear(ServerLevel level, BlockPos pos) {
        ACTIVE_SLOWDOWNS.remove(new MachineKey(level.dimension(), pos));
        removeVisualMarkers(level, pos);
    }

    private static Slowdown getActiveSlowdown(ServerLevel level, BlockPos pos) {
        MachineKey key = new MachineKey(level.dimension(), pos);
        Slowdown slowdown = ACTIVE_SLOWDOWNS.get(key);
        if (slowdown == null) {
            return null;
        }

        long currentTick = level.getServer().getTickCount();
        if (currentTick >= slowdown.expiresAt()) {
            ACTIVE_SLOWDOWNS.remove(key);
            return null;
        }
        if (hasTimeAcceleration(level, pos)) {
            ACTIVE_SLOWDOWNS.remove(key);
            removeVisualMarkers(level, pos);
            return null;
        }
        return slowdown;
    }

    private static boolean hasTimeAcceleration(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).stream()
                .anyMatch(accelerator -> accelerator.getTimeRate() > 0);
    }

    private static void ensureVisualMarker(ServerLevel level, BlockPos pos, int tickInterval, int remainingTicks) {
        TimeAcceleratorEntity marker = findVisualMarker(level, pos);
        if (marker == null) {
            marker = createVisualMarker(level, pos);
            marker.setTimeRate(toVisualTimeRate(tickInterval));
            marker.setRemainingTime(remainingTicks);
            level.addFreshEntity(marker);
        }
    }

    private static void refreshVisualMarker(ServerLevel level, BlockPos pos, int tickInterval, int durationTicks) {
        TimeAcceleratorEntity marker = findVisualMarker(level, pos);
        if (marker == null) {
            marker = createVisualMarker(level, pos);
            level.addFreshEntity(marker);
        }
        marker.setTimeRate(toVisualTimeRate(tickInterval));
        marker.setRemainingTime(durationTicks);
    }

    private static TimeAcceleratorEntity createVisualMarker(ServerLevel level, BlockPos pos) {
        return new TimeAcceleratorEntity(level, pos,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static TimeAcceleratorEntity findVisualMarker(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).stream()
                .filter(accelerator -> accelerator.getTimeRate() < 0)
                .findFirst()
                .orElse(null);
    }

    private static void removeVisualMarkers(ServerLevel level, BlockPos pos) {
        level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).stream()
                .filter(accelerator -> accelerator.getTimeRate() < 0)
                .forEach(accelerator -> accelerator.discard());
    }

    private static int toVisualTimeRate(int tickInterval) {
        return tickInterval == 4 ? -2 : -1;
    }

    private record MachineKey(ResourceKey<Level> dimension, BlockPos pos) {}

    private record Slowdown(long expiresAt, int tickInterval) {}

    public record SlowdownInfo(double multiplier, int remainingTicks) {}
}
