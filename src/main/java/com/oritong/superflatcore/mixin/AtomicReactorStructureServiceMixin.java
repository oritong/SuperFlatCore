package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.atomic.AtomicReactorStructureIntegration;
import com.shiory.atomicembers.feature.reactor.ReactorVariant;
import com.shiory.atomicembers.feature.reactor.blockentity.ReactorInputBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.reactor.ReactorStructureService", remap = false)
public abstract class AtomicReactorStructureServiceMixin {
    @Inject(method = "validate", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$validateStandard(ServerLevel level, BlockPos master,
                                                       Direction facing, ReactorVariant variant,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (variant == ReactorVariant.APPRENTICE) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(AtomicReactorStructureIntegration.validateStandard(level, master, facing));
        }
    }

    @Inject(method = "formedStructureIntact", at = @At("RETURN"), cancellable = true, remap = false)
    private static void superflatcore$requireAuxiliaryTank(ServerLevel level, ReactorInputBlockEntity master,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        cir.setReturnValue(AtomicReactorStructureIntegration.auxiliaryTankIntact(
                level, master.getBlockPos(), master.structureFacing()));
    }

}
