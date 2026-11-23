package com.oritong.superflatcore.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class WorldSpawnPosition {
    private WorldSpawnPosition() {}

    public static Optional<Vec3> find(ServerLevel level, ServerPlayer player) {
        var spawn = level.getSharedSpawnPos();
        var dimensions = player.getDimensions(Pose.STANDING);
        // Start at the configured feet height, including underground spawns. Searching the
        // surface heightmap would put the player on the starter house's roof instead.
        for (int y = Math.max(spawn.getY(), level.getMinBuildHeight());
             y + dimensions.height <= level.getMaxBuildHeight(); y++) {
            Vec3 position = new Vec3(spawn.getX(), y, spawn.getZ());
            var bounds = dimensions.makeBoundingBox(position);
            if (level.getWorldBorder().isWithinBounds(bounds)
                    && level.noCollision(player, bounds) && !level.containsAnyLiquid(bounds)) {
                return Optional.of(position);
            }
        }
        return Optional.empty();
    }
}
