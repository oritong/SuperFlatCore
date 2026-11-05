package com.oritong.superflatcore.blackhole.client;

import com.oritong.superflatcore.blackhole.BlackHoleBlockEntity;
import com.oritong.superflatcore.SuperFlatCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Connects real black-hole block entities to the skill's world compositor. */
@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT)
public final class BlackHoleClient {
    static Vec3 center;
    static int coreColor = 0;
    static long worldDraws;
    static String failure = "";
    private static ClientLevel owner;

    private BlackHoleClient() {}

    public static void refreshCenter(ClientLevel level, Camera camera) {
        if (level == null || camera == null) {
            stop();
            return;
        }
        if (owner != level) {
            owner = level;
            center = null;
            failure = "";
        }
        ChunkPos cameraChunk = new ChunkPos(BlockPos.containing(camera.getPosition()));
        int chunkRadius = 4;
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int chunkX = cameraChunk.x - chunkRadius; chunkX <= cameraChunk.x + chunkRadius; chunkX++) {
            for (int chunkZ = cameraChunk.z - chunkRadius; chunkZ <= cameraChunk.z + chunkRadius; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                    if (!(chunk.getBlockEntity(pos) instanceof BlackHoleBlockEntity)) {
                        continue;
                    }
                    double distance = pos.getCenter().distanceToSqr(camera.getPosition());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = pos.immutable();
                    }
                }
            }
        }
        center = nearest == null ? null : nearest.getCenter();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            stop();
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            refreshCenter(level, event.getCamera());
            BlackHoleWorld.captureEnvironment(event);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            BlackHoleWorld.captureCamera(event);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            BlackHoleWorld.render(event);
        }
    }

    public static void stop() {
        center = null;
        owner = null;
        BlackHoleWorld.release();
    }
}
