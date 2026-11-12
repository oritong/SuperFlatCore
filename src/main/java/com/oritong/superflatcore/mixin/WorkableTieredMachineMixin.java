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
    private int superflatcore$keepFluidInputSlotAvailable(int slots) {
        // These recipe UIs bind a TankWidget even though the small machines
        // must not accept or store fluids.
        return superflatcore$hasZeroFluidCapacity() ? 1 : slots;
    }

    @ModifyArg(
            method = "createImportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 2,
            remap = false)
    private int superflatcore$disableSmallMachineInputTankCapacity(int capacity) {
        return superflatcore$hasZeroFluidCapacity() ? 0 : capacity;
    }

    @ModifyArg(
            method = "createExportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 1,
            remap = false)
    private int superflatcore$keepFluidOutputSlotAvailable(int slots) {
        return superflatcore$hasZeroFluidCapacity() && slots > 0 ? 1 : slots;
    }

    @ModifyArg(
            method = "createExportFluidHandler([Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;<init>(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;IILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
            index = 2,
            remap = false)
    private int superflatcore$disableSmallMachineOutputTankCapacity(int capacity) {
        return superflatcore$hasZeroFluidCapacity() ? 0 : capacity;
    }

    private boolean superflatcore$hasZeroFluidCapacity() {
        String machineId = ((WorkableTieredMachine) (Object) this).getDefinition().getId().getPath();
        return machineId.endsWith("_compressor") || machineId.endsWith("_laser_engraver");
    }
}
