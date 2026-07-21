package com.oritong.superflatcore.mixin.client;

import com.oritong.superflatcore.client.ToolHighlightGuiAccessor;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Gui.class)
public abstract class GuiMixin implements ToolHighlightGuiAccessor {
    @Shadow
    protected int toolHighlightTimer;

    @Shadow
    protected ItemStack lastToolHighlight;

    @Override
    public void superflatcore$showSelectedItemName(ItemStack stack) {
        this.lastToolHighlight = stack.copy();
        this.toolHighlightTimer = 40;
    }
}
