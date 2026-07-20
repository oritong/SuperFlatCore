package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DualHatchPartMachine.class, remap = false)
public class DualHatchPartMachineMixin extends ItemBusPartMachine {

    public DualHatchPartMachineMixin(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io, args);
    }

    @Inject(method = "getInventorySize", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$getInventorySize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(super.getInventorySize());
    }

    @Inject(method = "getTankCapacity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$getTankCapacity(int initialCapacity, int tier,
                                                     CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier));
    }
}
