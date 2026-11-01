package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class ChatFlightEvents {
    private static final String FLIGHT_ENABLED_TAG = "superflatcore:chat_flight_enabled";

    private ChatFlightEvents() {}

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        switch (event.getRawText().trim()) {
            case "中国人能飞" -> setFlight(event.getPlayer(), true);
            case "曼巴out" -> setFlight(event.getPlayer(), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.getPersistentData().getBoolean(FLIGHT_ENABLED_TAG) && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().getBoolean(FLIGHT_ENABLED_TAG)) {
            event.getEntity().getPersistentData().putBoolean(FLIGHT_ENABLED_TAG, true);
        }
    }

    private static void setFlight(ServerPlayer player, boolean enabled) {
        if (enabled) {
            player.getPersistentData().putBoolean(FLIGHT_ENABLED_TAG, true);
        } else {
            player.getPersistentData().remove(FLIGHT_ENABLED_TAG);
        }

        Abilities abilities = player.getAbilities();
        abilities.mayfly = enabled || player.isCreative() || player.isSpectator();
        abilities.flying = enabled || player.isSpectator();
        player.fallDistance = 0;
        player.onUpdateAbilities();
        player.displayClientMessage(Component.translatable(enabled
                ? "message.superflatcore.chat_flight.enabled"
                : "message.superflatcore.chat_flight.disabled"), true);
    }
}
