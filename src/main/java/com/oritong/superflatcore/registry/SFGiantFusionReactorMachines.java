package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.air;
import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.frames;

/** The UV compressed fusion reactor controller ported from GTNH/GTLCore. */
public final class SFGiantFusionReactorMachines {
    public static final MultiblockMachineDefinition GIANT_FUSION_REACTOR_CONTROL_COMPUTER =
            SFRegistrate.REGISTRATE.multiblock("giant_fusion_reactor_control_computer",
                            holder -> new FusionReactorMachine(holder, GTValues.UV) {
                                @Override
                                public long getMaxVoltage() {
                                    return super.getOverclockVoltage();
                                }
                            })
                    .rotationState(RotationState.ALL)
                    .langValue("巨型核聚变反应堆控制电脑")
                    .recipeType(GTRecipeTypes.FUSION_RECIPES)
                    .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, FusionReactorMachine::recipeModifier)
                    .appearanceBlock(GTBlocks.FUSION_CASING_MK3)
                    .pattern(definition -> {
                        var casing = blocks(GTBlocks.FUSION_CASING_MK3.get());
                        return SFCompressedFusionReactorStructure.SLAUGHTERHOUSE_3
                                .where('S', controller(blocks(definition.get())))
                                .where('B', blocks(GTBlocks.FUSION_GLASS.get()))
                                .where('C', casing)
                                .where('P', casing.or(abilities(PartAbility.PARALLEL_HATCH)
                                        .setMaxGlobalLimited(1).setPreviewCount(1)))
                                .where('I', casing.or(abilities(PartAbility.IMPORT_FLUIDS))
                                        .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(16)))
                                .where('F', frames(GTMaterials.Naquadria))
                                .where('H', blocks(SFBlocks.ADVANCED_COMPRESSED_FUSION_COIL.get()))
                                .where('E', casing.or(blocks(PartAbility.INPUT_ENERGY
                                                .getBlockRange(GTValues.UV, 14).toArray(Block[]::new)))
                                        .or(blocks(PartAbility.INPUT_LASER
                                                .getBlockRange(GTValues.UV, 14).toArray(Block[]::new))
                                                .setPreviewCount(16)))
                                .where('#', air())
                                .where(' ', any())
                                .build();
                    })
                    .workableCasingModel(GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                    .register();

    private SFGiantFusionReactorMachines() {}

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {}

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEu.id("item").equals(event.getTabKey().location())) {
            event.accept(GIANT_FUSION_REACTOR_CONTROL_COMPUTER.asStack());
            event.accept(SFBlocks.ADVANCED_COMPRESSED_FUSION_COIL.asStack());
            event.accept(GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Naquadria).asStack());
        }
    }
}
