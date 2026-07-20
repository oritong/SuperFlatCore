package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GTMachines.class, remap = false)
public class GTMachinesMixin {

    @Inject(method = "lambda$static$79", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$registerDualInputHatch(Integer tier, MachineBuilder<MachineDefinition, ?> builder,
                                                            CallbackInfoReturnable<MachineDefinition> cir) {
        cir.setReturnValue(superflatcore$registerDualHatch(tier, builder, true));
    }

    @Inject(method = "lambda$static$81", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$registerDualOutputHatch(Integer tier, MachineBuilder<MachineDefinition, ?> builder,
                                                             CallbackInfoReturnable<MachineDefinition> cir) {
        cir.setReturnValue(superflatcore$registerDualHatch(tier, builder, false));
    }

    private static MachineDefinition superflatcore$registerDualHatch(Integer tier,
                                                                     MachineBuilder<MachineDefinition, ?> builder,
                                                                     boolean input) {
        int tierIndex = tier;
        int slotRoot = tierIndex + 1;
        String ioName = input ? "Input" : "Output";
        String tooltip = input ? "gtceu.machine.dual_hatch.import.tooltip" : "gtceu.machine.dual_hatch.export.tooltip";
        return builder
                .langValue("%s Dual %s Hatch".formatted(GTValues.VNF[tierIndex], ioName))
                .rotationState(RotationState.ALL)
                .abilities(input ? GTMachineUtils.DUAL_INPUT_HATCH_ABILITIES : GTMachineUtils.DUAL_OUTPUT_HATCH_ABILITIES)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .overlayTieredHullModel(input ? "dual_input_hatch" : "dual_output_hatch")
                .tooltips(
                        Component.translatable(tooltip),
                        Component.translatable("gtceu.universal.tooltip.item_storage_capacity", slotRoot * slotRoot),
                        Component.translatable(
                                "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                slotRoot,
                                DualHatchPartMachine.getTankCapacity(
                                        DualHatchPartMachine.INITIAL_TANK_CAPACITY, tierIndex)),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .register();
    }
}
