package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.world.WorldSpawnPosition;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.handler.BotaniaSounds;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class WorldSpawnCommandEvents {
    private WorldSpawnCommandEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("spawn")
                .requires(source -> true)
                .executes(context -> teleportToWorldSpawn(context.getSource())));
    }

    private static int teleportToWorldSpawn(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel overworld = source.getServer().overworld();
        var target = WorldSpawnPosition.find(overworld, player);
        if (target.isEmpty()) {
            source.sendFailure(Component.translatable("commands.superflatcore.spawn.no_position"));
            return 0;
        }

        ServerLevel previousLevel = player.serverLevel();
        Vec3 previousPosition = player.position();
        Vec3 position = target.get();
        player.stopRiding();
        player.teleportTo(overworld, position.x, position.y, position.z, overworld.getSharedSpawnAngle(), 0);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;

        overworld.playSound(null, player.getX(), player.getY(), player.getZ(),
                BotaniaSounds.worldSeedTeleport, SoundSource.PLAYERS, 1, 1);
        overworld.sendParticles(SparkleParticleData.sparkle(1F, 0.25F, 1F, 0.25F, 10),
                player.getX(), player.getY() + player.getBbHeight() / 2, player.getZ(), 50,
                player.getBbWidth() / 8, player.getBbHeight() / 4, player.getBbWidth() / 8, 0);
        previousLevel.gameEvent(player, GameEvent.TELEPORT, previousPosition);

        source.sendSuccess(() -> Component.translatable(
                "commands.superflatcore.spawn.success"), false);
        return 1;
    }
}
