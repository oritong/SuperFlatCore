package com.oritong.superflatcore.mixin;

import mekanism.api.RelativeSide;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import org.spongepowered.asm.mixin.Mixin;

/** Expands both energy capability access and automatic emission to every face. */
@Mixin(value = TileEntityHeatGenerator.class, remap = false)
public abstract class MekanismHeatGeneratorMixin {
    // Overrides the inherited generator method, as Mekanism's other generators do.
    protected RelativeSide[] getEnergySides() {
        return RelativeSide.values();
    }
}
