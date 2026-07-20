package com.oritong.superflatcore.event;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.haoict.tiab.common.entities.TimeAcceleratorEntity;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFMobEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class MachineSoulJoyEvents {
    private static final float TRIGGER_CHANCE = 0.35F;
    private static final float DOUBLE_SPEED_CHANCE = 0.75F;
    private static final int MIN_SECONDS = 1;
    private static final int MAX_SECONDS = 3;
    private static final int TICKS_PER_SECOND = 20;

    private MachineSoulJoyEvents() {}

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!player.hasEffect(SFMobEffects.MACHINE_SOUL_JOY.get())) {
            return;
        }

        BlockPos machinePos = player.getOnPos();
        if (!hasRecipeLogic(level, machinePos)) {
            return;
        }
        if (hasTimeAcceleration(level, machinePos)) {
            return;
        }

        RandomSource random = player.getRandom();
        if (random.nextFloat() >= TRIGGER_CHANCE) {
            return;
        }

        TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(serverLevel, machinePos,
                machinePos.getX() + 0.5D, machinePos.getY() + 0.5D, machinePos.getZ() + 0.5D);
        accelerator.setTimeRate(random.nextFloat() < DOUBLE_SPEED_CHANCE ? 1 : 2);
        accelerator.setRemainingTime((MIN_SECONDS + random.nextInt(MAX_SECONDS - MIN_SECONDS + 1))
                * TICKS_PER_SECOND);
        serverLevel.addFreshEntity(accelerator);
    }

    private static boolean hasRecipeLogic(Level level, BlockPos pos) {
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        return machine instanceof IRecipeLogicMachine recipeLogicMachine
                && recipeLogicMachine.isRecipeLogicAvailable()
                && recipeLogicMachine.getRecipeLogic() != null;
    }

    private static boolean hasTimeAcceleration(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).isEmpty();
    }
}
