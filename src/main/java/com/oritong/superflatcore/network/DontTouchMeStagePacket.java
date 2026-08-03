package com.oritong.superflatcore.network;

import com.oritong.superflatcore.client.DontTouchMeClientEffects;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DontTouchMeStagePacket(int stage) {
    public static void encode(DontTouchMeStagePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.stage);
    }

    public static DontTouchMeStagePacket decode(FriendlyByteBuf buffer) {
        return new DontTouchMeStagePacket(buffer.readVarInt());
    }

    public static void handle(DontTouchMeStagePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DontTouchMeClientEffects.applyStage(packet.stage)));
        context.setPacketHandled(true);
    }
}
