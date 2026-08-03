package com.oritong.superflatcore.event;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.haoict.tiab.common.entities.TimeAcceleratorEntity;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFMobEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class MachineSoulDispleasureEvents {
    private static final int CHECK_INTERVAL_TICKS = 5 * 20;
    private static final float HALF_SPEED_CHANCE = 0.75F;
    private static final int DURATION_TICKS = 30 * 20;

    private MachineSoulDispleasureEvents() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level() instanceof ServerLevel serverLevel
                    && player.hasEffect(SFMobEffects.MACHINE_SOUL_DISPLEASURE.get())) {
                applyToNearbyMachines(serverLevel, player);
            }
        }
    }

    private static void applyToNearbyMachines(ServerLevel level, ServerPlayer player) {
        ChunkPos center = player.chunkPosition();
        RandomSource random = player.getRandom();
        for (int chunkX = center.x - 1; chunkX <= center.x + 1; chunkX++) {
            for (int chunkZ = center.z - 1; chunkZ <= center.z + 1; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                    MetaMachine machine = MetaMachine.getMachine(level, pos);
                    if (!hasRecipeLogic(machine) || MachineSoulTimeEffects.hasTimeAcceleration(level, pos)) {
                        continue;
                    }
                    if (MachineSoulTimeEffects.isGenerator(machine)) {
                        MachineSoulTimeEffects.applyDispleasureTimeAcceleration(level, pos, random, DURATION_TICKS);
                    } else {
                        int tickInterval = random.nextFloat() < HALF_SPEED_CHANCE ? 2 : 4;
                        MachineSoulDispleasureManager.apply(level, pos, tickInterval, DURATION_TICKS);
                    }
                }
            }
        }
    }

    private static boolean hasRecipeLogic(MetaMachine machine) {
        return machine instanceof IRecipeLogicMachine recipeLogicMachine
                && recipeLogicMachine.isRecipeLogicAvailable()
                && recipeLogicMachine.getRecipeLogic() != null;
    }
}
