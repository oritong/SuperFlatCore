package com.oritong.superflatcore.mixin.diamondcore.gtnn;

import com.oritong.superflatcore.compat.diamondcore.gtnn.GTNNBedrockOreMinerRestrictions;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.arbor.gtnn.common.machine.logic.GTNNBedrockOreMinerLogic", remap = false)
public abstract class MixinGTNNBedrockOreMinerLogic {

    @Shadow(remap = false)
    private List<?> veinOres;

    @Inject(
            method = "getOreMinerRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/arbor/gtnn/api/recipe/OresHelper;getRandomItem(Ljava/util/List;I)I",
                    shift = At.Shift.BEFORE),
            cancellable = true,
            remap = false)
    private void oritongsdiamondcore$removeBannedBedrockMinerOutputs(CallbackInfoReturnable<?> cir) {
        if (veinOres == null) {
            return;
        }

        veinOres.removeIf(GTNNBedrockOreMinerRestrictions::isBannedWeightedOre);
        if (veinOres.isEmpty()) {
            cir.setReturnValue(null);
        }
    }
}
