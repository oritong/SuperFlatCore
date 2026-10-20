package com.oritong.superflatcore.mixin;

import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.generators.common.MekanismGenerators;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FormationProtocol.class, remap = false)
public abstract class MekanismFissionFormationMixin {
    @Shadow @Final private MultiblockManager<?> manager;

    @Inject(method = "doUpdate", at = @At("HEAD"), cancellable = true)
    private void superflatcore$disableFissionFormation(
            CallbackInfoReturnable<FormationProtocol.FormationResult> cir) {
        if (manager == MekanismGenerators.fissionReactorManager) {
            cir.setReturnValue(FormationProtocol.FormationResult.FAIL);
        }
    }
}
