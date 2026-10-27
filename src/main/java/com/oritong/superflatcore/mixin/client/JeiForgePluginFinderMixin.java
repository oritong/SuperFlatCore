package com.oritong.superflatcore.mixin.client;

import com.oritong.superflatcore.integration.jei.SFJeiPluginRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.forge.startup.ForgePluginFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

/**
 * Replaces JEI's Forge scan-data plugin discovery with SFC's pack whitelist.
 */
@Mixin(value = ForgePluginFinder.class, remap = false)
public abstract class JeiForgePluginFinderMixin {
    /**
     * @author SuperFlatCore
     * @reason The pack has a fixed plugin set; avoid scanning every Forge mod.
     */
    @Overwrite(remap = false)
    public static List<IModPlugin> getModPlugins() {
        return SFJeiPluginRegistry.createPlugins();
    }
}
