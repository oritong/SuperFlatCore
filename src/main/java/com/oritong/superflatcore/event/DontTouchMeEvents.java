package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.DontTouchMeItem;
import com.oritong.superflatcore.network.SFNetwork;
import com.oritong.superflatcore.registry.SFItems;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class DontTouchMeEvents {
    private DontTouchMeEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resetInventory(player, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        long gameTime = player.level().getGameTime();
        boolean changed = false;
        for (ItemStack stack : player.getInventory().items) {
            changed |= resetExpiredStack(stack, gameTime);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            changed |= resetExpiredStack(stack, gameTime);
        }

        if (changed) {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            SFNetwork.sendDontTouchMeStage(player, 0);
        }
    }

    private static void resetInventory(ServerPlayer player, boolean notifyClient) {
        boolean changed = false;
        for (ItemStack stack : player.getInventory().items) {
            changed |= resetStack(stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            changed |= resetStack(stack);
        }

        if (changed) {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
        }
        if (notifyClient) {
            SFNetwork.sendDontTouchMeStage(player, 0);
        }
    }

    private static boolean resetExpiredStack(ItemStack stack, long gameTime) {
        if (!isDontTouchMe(stack) || DontTouchMeItem.getStage(stack) == 0) {
            return false;
        }
        if (gameTime - DontTouchMeItem.getLastUseTick(stack) <= DontTouchMeItem.RESET_TICKS) {
            return false;
        }

        DontTouchMeItem.resetStage(stack);
        return true;
    }

    private static boolean resetStack(ItemStack stack) {
        if (!isDontTouchMe(stack) || DontTouchMeItem.getStage(stack) == 0) {
            return false;
        }
        DontTouchMeItem.resetStage(stack);
        return true;
    }

    private static boolean isDontTouchMe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(SFItems.DONT_TOUCH_ME.get());
    }
}
