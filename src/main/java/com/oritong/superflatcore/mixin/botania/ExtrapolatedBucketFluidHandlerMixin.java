package com.oritong.superflatcore.mixin.botania;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// The pack's newer Forge fluid handler is absent from the Botania 448 compile reference.
@Pseudo
@Mixin(targets = "vazkii.botania.forge.CapabilityUtil$ExtrapolatedBucketFluidHandler", remap = false)
public abstract class ExtrapolatedBucketFluidHandlerMixin {
    @ModifyConstant(method = "fill", constant = @Constant(intValue = 1000), remap = false)
    private int superflatcore$increaseDeletionLimit(int original) {
        return Integer.MAX_VALUE;
    }
}
