package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    @Mutable
    @Shadow
    @Final
    public static int[] DUAL_HATCH_TIERS;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void superflatcore$expandDualHatchTiers(CallbackInfo ci) {
        DUAL_HATCH_TIERS = GTValues.tiersBetween(GTValues.LV, GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UHV);
    }
}
