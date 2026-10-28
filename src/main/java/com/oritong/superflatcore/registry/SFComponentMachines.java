package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.oritong.superflatcore.machine.SteelComponentAssemblyLineMachine;
import com.oritong.superflatcore.recipe.SFComponentRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public final class SFComponentMachines {
    public static final MultiblockMachineDefinition STEEL_COMPONENT_ASSEMBLY_LINE = SFRegistrate.REGISTRATE
            .multiblock("steel_component_assembly_line", SteelComponentAssemblyLineMachine::new)
            .langValue("Steel Component Assembly Line")
            .rotationState(RotationState.ALL)
            .recipeType(SFComponentRecipeTypes.COMPONENT_ASSEMBLY_LINE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT,
                    SteelComponentAssemblyLineMachine::parallelModifier,
                    SteelComponentAssemblyLineMachine::durationModifier)
            .alwaysTryModifyRecipe(true)
            .appearanceBlock(GTBlocks.CASING_INVAR_HEATPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("EEE", "EIE", "EEE")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "GEH")
                    .aisle("EEB", "EFD", "EEE")
                    .aisle("AAB", "CAD", "AAA")
                    .where('E', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('I', abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1).setPreviewCount(1))
                    .where('B', abilities(PartAbility.IMPORT_FLUIDS_1X).setExactLimit(1).setPreviewCount(1)
                            .or(machines(GTMachines.ITEM_IMPORT_BUS[GTValues.ULV])
                                    .setExactLimit(9).setPreviewCount(9)))
                    .where('D', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('H', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('F', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .where('G', frames(GTMaterials.Steel))
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('A', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1).setPreviewCount(1))
                            .or(abilities(PartAbility.MUFFLER).setExactLimit(1).setPreviewCount(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1).setPreviewCount(1)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .register();

    private SFComponentMachines() {}

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {}

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEu.id("item").equals(event.getTabKey().location())) {
            event.accept(STEEL_COMPONENT_ASSEMBLY_LINE.asStack());
        }
    }
}
