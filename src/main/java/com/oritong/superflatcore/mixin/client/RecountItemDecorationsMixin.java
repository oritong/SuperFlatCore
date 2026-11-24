package com.oritong.superflatcore.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Runs the config plugin's post-apply patch after Recount's higher-priority mixin.
 * An ordinary RETURN injection misses Recount's generated cancellation return.
 */
@Mixin(GuiGraphics.class)
public abstract class RecountItemDecorationsMixin {
}
