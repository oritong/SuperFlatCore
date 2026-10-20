package com.oritong.superflatcore.mixin;

import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.Structure;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Structure.class, remap = false)
public abstract class MekanismFissionStructureMixin {
    @Shadow public abstract IMultiblock<?> getController();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void superflatcore$stopFissionStructureChecks(BlockEntity tile, boolean tryValidate, CallbackInfo ci) {
        if (tile instanceof TileEntityFissionReactorCasing
                || getController() instanceof TileEntityFissionReactorCasing) {
            ci.cancel();
        }
    }
}
