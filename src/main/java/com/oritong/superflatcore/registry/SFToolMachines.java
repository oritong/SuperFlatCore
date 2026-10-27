package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.machine.ToolWeldingAssemblerMachine;
import com.oritong.superflatcore.recipe.SFToolRecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public final class SFToolMachines {
    public static final MachineDefinition[] TOOL_WELDING_ASSEMBLER = GTMachineUtils.registerTieredMachines(
            SFRegistrate.REGISTRATE, "tool_welding_assembler", ToolWeldingAssemblerMachine::new,
            (tier, builder) -> builder
                    .langValue(GTValues.VN[tier] + " Tool Welding Assembler")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(SFToolRecipeTypes.TOOL_WELDING_ASSEMBLER_RECIPES)
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                            SuperFlatCore.id("tool_welding_assembler"),
                            SFToolRecipeTypes.TOOL_WELDING_ASSEMBLER_RECIPES))
                    .workableTieredHullModel(GTCEu.id("block/machines/assembler"))
                    .tooltips(Component.translatable("superflatcore.machine.tool_welding_assembler.tooltip"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            SFToolRecipeTypes.TOOL_WELDING_ASSEMBLER_RECIPES,
                            GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier), true))
                    .register(),
            GTValues.tiersBetween(GTValues.LV, GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UHV));

    private SFToolMachines() {}

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {}

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEu.id("item").equals(event.getTabKey().location())) {
            for (MachineDefinition machine : TOOL_WELDING_ASSEMBLER) {
                if (machine != null) {
                    event.accept(machine.asStack());
                }
            }
        }
    }
}
