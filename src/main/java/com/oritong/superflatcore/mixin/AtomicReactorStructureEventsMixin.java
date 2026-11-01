package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.mekanism.MekanismRadiationIntegration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.reactor.ReactorStructureEvents", remap = false)
public abstract class AtomicReactorStructureEventsMixin {
    @Inject(method = "disassembleContaining", at = @At("HEAD"), remap = false)
    private static void superflatcore$radiateBeforeDisassembly(ServerLevel level, BlockPos pos,
                                                                CallbackInfoReturnable<Boolean> cir) {
        MekanismRadiationIntegration.radiateReactorAt(level, pos);
    }
}
