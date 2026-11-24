package com.oritong.superflatcore.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeItemDecorationBridge {
    private static boolean logged;

    private ForgeItemDecorationBridge() {}

    public static void render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        ItemDecoratorHandler.of(stack).render(graphics, font, stack, x, y);
        if (!logged) {
            logged = true;
            LogUtils.getLogger().info("Restored Forge item decorator callback after Recount; first item: {}",
                    ForgeRegistries.ITEMS.getKey(stack.getItem()));
        }
    }
}
