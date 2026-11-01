package com.oritong.superflatcore.integration.ezunclear;

import me.kall.ezunclear.data.PendingMeltdown;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Set;

public final class EzUnclearIntegration {
    private EzUnclearIntegration() {}

    public static boolean deferAccident(ServerLevel level, BlockPos position, Runnable accident) {
        List<Runnable> meltdowns = PendingMeltdown.MELT_DOWNS;
        Set<BlockPos> positions = PendingMeltdown.POSITIONS;
        synchronized (meltdowns) {
            if (positions.contains(position)) {
                return true;
            }
            level.players().forEach(player ->
                    player.displayClientMessage(Component.translatable("info.ezunclear"), false));
            meltdowns.add(accident);
            positions.add(position);
        }
        return true;
    }
}
