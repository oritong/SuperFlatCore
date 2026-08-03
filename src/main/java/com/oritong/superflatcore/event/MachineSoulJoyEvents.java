package com.oritong.superflatcore.event;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFMobEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class MachineSoulJoyEvents {
    private static final float TRIGGER_CHANCE = 0.35F;
    private static final int MIN_SECONDS = 2;
    private static final int MAX_SECONDS = 4;
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
        MetaMachine machine = MetaMachine.getMachine(level, machinePos);
        if (!hasRecipeLogic(machine)) {
            return;
        }
        boolean generator = MachineSoulTimeEffects.isGenerator(machine);
        if (!generator && MachineSoulTimeEffects.hasTimeAcceleration(serverLevel, machinePos)) {
            return;
        }

        RandomSource random = player.getRandom();
        if (random.nextFloat() >= TRIGGER_CHANCE) {
            return;
        }

        int durationTicks = (MIN_SECONDS + random.nextInt(MAX_SECONDS - MIN_SECONDS + 1)) * TICKS_PER_SECOND;
        if (generator) {
            MachineSoulTimeEffects.clearDispleasureTimeAcceleration(serverLevel, machinePos);
            if (MachineSoulTimeEffects.hasTimeAcceleration(serverLevel, machinePos)) {
                return;
            }
            int tickInterval = random.nextFloat() < MachineSoulTimeEffects.TWO_X_CHANCE ? 2 : 4;
            MachineSoulDispleasureManager.apply(serverLevel, machinePos, tickInterval, durationTicks);
        } else {
            MachineSoulTimeEffects.applyTimeAcceleration(serverLevel, machinePos, random, durationTicks);
        }
    }

    private static boolean hasRecipeLogic(MetaMachine machine) {
        return machine instanceof IRecipeLogicMachine recipeLogicMachine
                && recipeLogicMachine.isRecipeLogicAvailable()
                && recipeLogicMachine.getRecipeLogic() != null;
    }
}
