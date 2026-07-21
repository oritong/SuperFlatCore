package com.oritong.superflatcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public final class DominionWandClientIntegration {
    private DominionWandClientIntegration() {
    }

    public static boolean isLookingAtAir(Player player) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null) {
            hitResult = player.pick(5.0D, 0.0F, false);
        }
        return hitResult.getType() == HitResult.Type.MISS;
    }

    public static void showSelectedItemName(ItemStack stack) {
        if (Minecraft.getInstance().gui instanceof ToolHighlightGuiAccessor accessor) {
            accessor.superflatcore$showSelectedItemName(stack);
        }
    }
}
