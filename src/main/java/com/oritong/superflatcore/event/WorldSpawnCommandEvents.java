package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.WorldSeedItem;

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
        ItemStack original = player.getMainHandItem();
        InteractionResultHolder<ItemStack> result;
        // WorldSeedItem reads and consumes the held stack. Supply a temporary seed so the command
        // runs Botania's actual teleport, collision handling, particles and sound without consuming gear.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BotaniaItems.worldSeed));
        try {
            result = ((WorldSeedItem) BotaniaItems.worldSeed).m_7203_(
                    player.level(), player, InteractionHand.MAIN_HAND);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, original);
        }
        if (!result.getResult().consumesAction()) return 0;

        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable(
                "commands.superflatcore.spawn.success"), false);
        return 1;
    }
}
