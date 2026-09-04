package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class WorldSpawnCommandEvents {
    private WorldSpawnCommandEvents() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("spawn")
                .requires(source -> true)
                .executes(context -> teleportToWorldSpawn(context.getSource())));
    }

    private static int teleportToWorldSpawn(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable(
                    "commands.superflatcore.spawn.unavailable"));
            return 0;
        }

        BlockPos spawnPos = PlayerRespawnLogic.getSpawnPosInChunk(
                overworld, new ChunkPos(overworld.getSharedSpawnPos()));
        if (spawnPos == null) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable(
                    "commands.superflatcore.spawn.no_position"));
            return 0;
        }

        Vec3 target = Vec3.atBottomCenterOf(spawnPos);
        if (!overworld.noCollision(player, player.getDimensions(Pose.STANDING).makeBoundingBox(target))) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable(
                    "commands.superflatcore.spawn.no_position"));
            return 0;
        }

        player.teleportTo(overworld, target.x, target.y, target.z,
                overworld.getSharedSpawnAngle(), 0.0F);

        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "commands.superflatcore.spawn.success"), false);
        return 1;
    }
}
