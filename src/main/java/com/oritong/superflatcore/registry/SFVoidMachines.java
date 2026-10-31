package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.frames;

public final class SFVoidMachines {
    public static final MachineDefinition LARGE_VOID_MINER = SFRegistrate.REGISTRATE
            .multiblock("large_void_miner", WorkableElectricMultiblockMachine::new)
            .langValue("\u5927\u578b\u865a\u7a7a\u91c7\u77ff\u673a")
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAAAAAAAAA", "AEEEEEEEEEA", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .aisle("AAAAAAAAAAA", "EAAAAAAAAAE", " E       E ", " E       E ", " E       E ", " E       E ", " EEEEEEEEE ", " EE     EE ", " E       E ", " E       E ", " EEEEEEEEE ", "           ", "           ", "           ", "           ")
                    .aisle("AAAAAAAAAAA", "EAAAAAAAAAE", "  EFFFFFE  ", "  EFFFFFE  ", "  EFFFFFE  ", "  EAAAAAE  ", " E       E ", "           ", "           ", "           ", " E   A   E ", "           ", "           ", "           ", "           ")
                    .aisle("AAAAAAAAAAA", "EAAGGGGGAAE", "AAF     FAA", " HF     FH ", "  F     F  ", "  A  G  A  ", " E       E ", "           ", "           ", "           ", " E   A   E ", "   EEEEE   ", "           ", "           ", "           ")
                    .aisle("AAAAHHHAAAA", "EAAGFFFGAAE", "DAF     FAD", " HF     FH ", "  F     F  ", "  A  G  A  ", " E       E ", "           ", "           ", "           ", " E   A   E ", "   EAAAE   ", "    EDE    ", "    EEE    ", "           ")
                    .aisle("AAAAHHHAAAA", "EAAGFHFGAAE", "AAF  G  FAA", " GF  H  FG ", "  F  H  F  ", "  AGGGGGA  ", " E   H   E ", "     H     ", "     H     ", "     H     ", " EAAAGAAAE ", "   EAHAE   ", "    AAA    ", "    EGE    ", "     D     ")
                    .aisle("AAAAHHHAAAA", "EAAGFFFGAAE", "DAF     FAD", " HF     FH ", "  F     F  ", "  A  G  A  ", " E       E ", "           ", "           ", "           ", " E   A   E ", "   EAAAE   ", "    EDE    ", "    EEE    ", "           ")
                    .aisle("AAAAAAAAAAA", "EAAGGGGGAAE", "AAF     FAA", " HF     FH ", "  F     F  ", "  A  G  A  ", " E       E ", "           ", "           ", "           ", " E   A   E ", "   EEEEE   ", "           ", "           ")
                    .aisle("AAAAAAAAAAA", "EAAAAAAAAAE", "  EFFFFFE  ", "  EFFFFFE  ", "  EFFFFFE  ", "  EAAAAAE  ", " E       E ", "           ", "           ", "           ", " E   A   E ", "           ", "           ", "           ", "           ")
                    .aisle("AAAAAAAAAAA", "EAAAAAAAAAE", " E       E ", " E       E ", " E       E ", " E       E ", " EEEEEEEEE ", " EE     EE ", " E       E ", " E       E ", " EEEEEEEEE ", "           ", "           ", "           ", "           ")
                    .aisle("AAABBBBBAAA", "A  BBBBB  A", "   BBCBB   ", "   DEEED   ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .where('A', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where('B', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.OUTPUT_ENERGY,
                                    PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS,
                                    PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                                    PartAbility.IMPORT_FLUIDS_1X, PartAbility.EXPORT_FLUIDS_1X,
                                    PartAbility.IMPORT_FLUIDS_4X, PartAbility.EXPORT_FLUIDS_4X,
                                    PartAbility.IMPORT_FLUIDS_9X, PartAbility.EXPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', controller(blocks(definition.get())))
                    .where('D', blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where('E', frames(GTMaterials.TungstenSteel))
                    .where('F', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('G', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                    .where('H', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where(' ', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/large_miner"))
            .register();

    private SFVoidMachines() {}
}
