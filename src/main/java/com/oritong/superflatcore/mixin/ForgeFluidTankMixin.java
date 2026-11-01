package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.radioactive.IRadioactiveFluidStorage;
import com.oritong.superflatcore.integration.radioactive.RadioactiveFluidPolicy;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidTank.class, remap = false)
public abstract class ForgeFluidTankMixin {
    @Inject(method = "fill", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$rejectRadioactive(FluidStack resource, FluidAction action,
                                                  CallbackInfoReturnable<Integer> cir) {
        if (RadioactiveFluidPolicy.isRadioactive(resource)
                && !((Object) this instanceof IRadioactiveFluidStorage)) {
            cir.setReturnValue(0);
        }
    }
}
