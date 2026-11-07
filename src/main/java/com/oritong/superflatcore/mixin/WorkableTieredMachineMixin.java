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
        // GTM's compressor recipe UI still creates a TankWidget for its fluid
        // capabilities. Keep one harmless placeholder tank so that the UI can
        // bind to index 0 without throwing, while the capacity hook below makes
        // the tank unusable in the actual machine.
        return superflatcore$isSmallCompressor() ? 1 : slots;
    }

    @ModifyArg(
            method = "createImportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 2,
            remap = false)
    private int superflatcore$disableCompressorInputTankCapacity(int capacity) {
        return superflatcore$isSmallCompressor() ? 0 : capacity;
    }

    @ModifyArg(
            method = "createExportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 1,
            remap = false)
    private int superflatcore$disableCompressorOutputTank(int slots) {
        return superflatcore$isSmallCompressor() ? 1 : slots;
    }

    @ModifyArg(
            method = "createExportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 2,
            remap = false)
    private int superflatcore$disableCompressorOutputTankCapacity(int capacity) {
        return superflatcore$isSmallCompressor() ? 0 : capacity;
    }

    private boolean superflatcore$isSmallCompressor() {
        return ((WorkableTieredMachine) (Object) this).getDefinition().getId().getPath().endsWith("_compressor");
    }
}
