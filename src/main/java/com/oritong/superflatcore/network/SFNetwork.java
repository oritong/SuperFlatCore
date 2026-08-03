package com.oritong.superflatcore.network;

import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class SFNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SuperFlatCore.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private SFNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(packetId++, DontTouchMeStagePacket.class,
                DontTouchMeStagePacket::encode,
                DontTouchMeStagePacket::decode,
                DontTouchMeStagePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendDontTouchMeStage(ServerPlayer player, int stage) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DontTouchMeStagePacket(stage));
    }
}
