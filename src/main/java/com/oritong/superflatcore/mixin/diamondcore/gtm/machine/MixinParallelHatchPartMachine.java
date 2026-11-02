package com.oritong.superflatcore.mixin.diamondcore.gtm.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParallelHatchPartMachine.class)
public class MixinParallelHatchPartMachine {

    @Mutable
    @Shadow(remap = false)
    @Final
    private int maxParallel;

    @Shadow(remap = false)
    private int currentParallel;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void onInit(IMachineBlockEntity holder, int tier, CallbackInfo ci) {
        this.maxParallel = tier == com.gregtechceu.gtceu.api.GTValues.MAX
                ? Integer.MAX_VALUE
                : (int) Math.pow(4, tier - 2);
        this.currentParallel = this.maxParallel;
    }

    @Inject(method = "getCurrentParallel", at = @At("HEAD"), remap = false, cancellable = true)
    private void onGetCurrentParallel(CallbackInfoReturnable<Integer> cir) {
        if (currentParallel == 0) cir.setReturnValue(maxParallel);
    }

    @Inject(method = "setCurrentParallel", at = @At("HEAD"), remap = false)
    private void onSetCurrentParallel(int parallelAmount, CallbackInfo ci) {
        if (currentParallel == 0) currentParallel = maxParallel;
    }

    @Inject(method = "canShared", at = @At("HEAD"), remap = false, cancellable = true)
    public void onCanShared(CallbackInfoReturnable<Boolean> cir) {
        if (((ParallelHatchPartMachine) (Object) this).getTier()
                == com.gregtechceu.gtceu.api.GTValues.MAX) {
            cir.setReturnValue(true);
        }
    }
}
