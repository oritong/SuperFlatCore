package com.oritong.superflatcore.integration.ezunclear;

import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchFieldException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EzUnclearIntegration {
    private static final String MOD_ID = "ezunclear";
    private static final String EZ_UNCLEAR_CLASS = "me.kall.ezunclear.EzUnclear";
    private static final String PENDING_MELTDOWN_CLASS = "me.kall.ezunclear.data.PendingMeltdown";
    private static final Logger LOGGER = LogManager.getLogger(EzUnclearIntegration.class);

    private EzUnclearIntegration() {}

    /**
     * Adds an Atomic reactor accident to EzUnclear's confirmation queue.
     * Returning false deliberately preserves Atomic's normal behavior when
     * EzUnclear is not available or exposes an incompatible API.
     */
    public static boolean deferAccident(ServerLevel level, BlockPos position, Runnable accident) {
        if (!ModList.get().isLoaded(MOD_ID)) {
            LOGGER.debug("EzUnclear is not loaded; Atomic reactor accident at {} will run normally.", position);
            return false;
        }

        try {
            Class<?> ezUnclear = Class.forName(EZ_UNCLEAR_CLASS);
            try {
                ezUnclear.getMethod("broadcast", ServerLevel.class, Runnable.class, BlockPos.class)
                        .invoke(null, level, accident, position);
            } catch (NoSuchMethodException ignored) {
                queueLegacyPendingMeltdown(level, position, accident);
            }
            LOGGER.info("Queued Atomic reactor accident at {} for EzUnclear confirmation.", position);
            return true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException
                 | InvocationTargetException | SecurityException | LinkageError exception) {
            LOGGER.error("Could not queue Atomic reactor accident through EzUnclear; executing it normally.",
                    exception);
            return false;
        }
    }

    private static void queueLegacyPendingMeltdown(ServerLevel level, BlockPos position, Runnable accident)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> pendingMeltdown = Class.forName(PENDING_MELTDOWN_CLASS);
        Field meltdownField = pendingMeltdown.getField("MELT_DOWNS");
        Field positionsField = pendingMeltdown.getField("POSITIONS");
        @SuppressWarnings("unchecked")
        List<Runnable> meltdowns = (List<Runnable>) meltdownField.get(null);
        @SuppressWarnings("unchecked")
        Set<BlockPos> positions = (Set<BlockPos>) positionsField.get(null);
        synchronized (meltdowns) {
            if (positions.contains(position)) return;
            level.players().forEach(player ->
                    player.displayClientMessage(Component.translatable("info.ezunclear"), false));
            meltdowns.add(accident);
            positions.add(position);
        }
    }
}
