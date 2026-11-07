package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = WorkableTieredMachine.class, remap = false)
public abstract class WorkableTieredMachineMixin {
    @ModifyArg(
            method = "createImportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 1,
            remap = false)
    private int superflatcore$disableCompressorInputTank(int slots) {
        return superflatcore$isSmallCompressor() ? 0 : slots;
    }

    @ModifyArg(
            method = "createExportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 1,
            remap = false)
    private int superflatcore$disableCompressorOutputTank(int slots) {
        return superflatcore$isSmallCompressor() ? 0 : slots;
    }

    private boolean superflatcore$isSmallCompressor() {
        return ((WorkableTieredMachine) (Object) this).getDefinition().getId().getPath().endsWith("_compressor");
    }
}
