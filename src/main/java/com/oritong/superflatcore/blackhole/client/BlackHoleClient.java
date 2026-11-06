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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Connects real black-hole block entities to the skill's world compositor. */
public final class BlackHoleClient {
    private static final Logger LOGGER = LogManager.getLogger("SuperFlatCore/BlackHole");
    static Vec3 center;
    static int coreColor = 0;
    static long worldDraws;
    static String failure = "";
    private static ClientLevel owner;
    private static long lastScanGameTime = Long.MIN_VALUE;
    private static int lastScanChunkX = Integer.MIN_VALUE;
    private static int lastScanChunkZ = Integer.MIN_VALUE;

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
            lastScanGameTime = Long.MIN_VALUE;
            lastScanChunkX = Integer.MIN_VALUE;
            lastScanChunkZ = Integer.MIN_VALUE;
        }
        ChunkPos cameraChunk = new ChunkPos(BlockPos.containing(camera.getPosition()));
        long gameTime = level.getGameTime();
        // Rendering has several stages per frame. Scanning nine by nine chunks
        // at every stage was the main source of client-side CPU spikes near a
        // black hole. A scan is sufficient once per game tick, or when the
        // camera enters another chunk.
        // A client block entity can arrive after the first render stage of a
        // tick.  Do not cache an empty result for that tick: retrying while
        // no center is known lets a newly placed/synchronised black hole
        // become visible immediately without adding extra scans once one is
        // found.
        if (center != null
                && lastScanGameTime == gameTime
                && lastScanChunkX == cameraChunk.x
                && lastScanChunkZ == cameraChunk.z) {
            return;
        }
        lastScanGameTime = gameTime;
        lastScanChunkX = cameraChunk.x;
        lastScanChunkZ = cameraChunk.z;
        int chunkRadius = 4;
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int chunkX = cameraChunk.x - chunkRadius; chunkX <= cameraChunk.x + chunkRadius; chunkX++) {
            for (int chunkZ = cameraChunk.z - chunkRadius; chunkZ <= cameraChunk.z + chunkRadius; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof BlackHoleBlockEntity)) {
                        continue;
                    }
                    BlockPos pos = entry.getKey();
                    double distance = pos.getCenter().distanceToSqr(camera.getPosition());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = pos.immutable();
                    }
                }
            }
        }
        center = nearest == null ? null : nearest.getCenter();
        if (center != null) {
            LOGGER.debug("[BLACK-HOLE] Center found at {} (camera chunk {}, {})", center, cameraChunk.x, cameraChunk.z);
        }
    }

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
        lastScanGameTime = Long.MIN_VALUE;
        lastScanChunkX = Integer.MIN_VALUE;
        lastScanChunkZ = Integer.MIN_VALUE;
        BlackHoleWorld.release();
    }
}
