package com.oritong.superflatcore.event;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.haoict.tiab.common.entities.TimeAcceleratorEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

public final class MachineSoulTimeEffects {
    public static final float TWO_X_CHANCE = 0.75F;
    private static final String DISPLEASURE_ACCELERATION_TAG =
            "SuperFlatCoreMachineSoulDispleasureAcceleration";

    private MachineSoulTimeEffects() {}

    public static boolean isGenerator(MetaMachine machine) {
        return machine instanceof SimpleGeneratorMachine
                || machine instanceof SteamBoilerMachine
                || machine instanceof LargeBoilerMachine
                || machine instanceof WorkableElectricMultiblockMachine electricMachine
                && electricMachine.isGenerator();
    }

    public static boolean hasTimeAcceleration(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).stream()
                .anyMatch(accelerator -> accelerator.getTimeRate() > 0);
    }

    public static void applyTimeAcceleration(
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            int durationTicks
    ) {
        if (hasTimeAcceleration(level, pos)) {
            return;
        }

        MachineSoulDispleasureManager.clear(level, pos);
        TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(level, pos,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        accelerator.setTimeRate(random.nextFloat() < TWO_X_CHANCE ? 1 : 2);
        accelerator.setRemainingTime(durationTicks);
        level.addFreshEntity(accelerator);
    }

    public static void applyDispleasureTimeAcceleration(
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            int durationTicks
    ) {
        if (hasTimeAcceleration(level, pos)) {
            return;
        }

        MachineSoulDispleasureManager.clear(level, pos);
        TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(level, pos,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        accelerator.setTimeRate(random.nextFloat() < TWO_X_CHANCE ? 1 : 2);
        accelerator.setRemainingTime(durationTicks);
        accelerator.getPersistentData().putBoolean(DISPLEASURE_ACCELERATION_TAG, true);
        level.addFreshEntity(accelerator);
    }

    public static void clearDispleasureTimeAcceleration(ServerLevel level, BlockPos pos) {
        level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).stream()
                .filter(accelerator -> accelerator.getPersistentData()
                        .getBoolean(DISPLEASURE_ACCELERATION_TAG))
                .forEach(TimeAcceleratorEntity::discard);
    }
}
