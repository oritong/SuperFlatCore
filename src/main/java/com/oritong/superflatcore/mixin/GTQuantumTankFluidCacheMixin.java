package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.radioactive.RadioactiveFluidPolicy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine$FluidCache", remap = false)
public abstract class GTQuantumTankFluidCacheMixin {
    @Inject(method = "fill", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$rejectRadioactive(FluidStack stack, FluidAction action,
                                                  CallbackInfoReturnable<Integer> cir) {
        if (RadioactiveFluidPolicy.isRadioactive(stack)) cir.setReturnValue(0);
    }

    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$rejectRadioactiveValid(int tank, FluidStack stack,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (RadioactiveFluidPolicy.isRadioactive(stack)) cir.setReturnValue(false);
    }
}
