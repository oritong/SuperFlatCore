package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import dev.arbor.gtnn.data.GTNNRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public final class SFPreciseAssemblyMachines {
    public static final MultiblockMachineDefinition AUTOMATIC_PRECISION_ASSEMBLER = SFRegistrate.REGISTRATE
            .multiblock("automatic_precision_assembler",
                    com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine::new)
            .langValue("Automatic Precision Assembler")
            .rotationState(RotationState.ALL)
            .recipeType(GTNNRecipeTypes.INSTANCE.getPRECISION_ASSEMBLY_RECIPES())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH)
            .appearanceBlock(SFPreciseAssemblyMachines::iridiumCasing)
            .pattern(definition -> {
                Block casing = iridiumCasing();
                // Aisles advance toward FRONT, so the controller belongs on the last aisle.
                return FactoryBlockPattern.start()
                        .aisle("CCCCCCCCC", "F       F", "F       F", "F       F", "CCCCCCCCC")
                        .aisle("CMMMMMMMC", "CGGGGGGGC", "CGGGGGGGC", "CGGGGGGGC", "CCCCCCCCC")
                        .aisle("CMMMMMMMC", "C-------C", "C-------C", "C-------C", "CCCCCCCCC")
                        .aisle("CMMMMMMMC", "CGGGGGGGC", "CGGGGGGGC", "CGGGGGGGC", "CCCCCCCCC")
                        .aisle("CCCC~CCCC", "F       F", "F       F", "F       F", "CCCCCCCCC")
                        .where('C', blocks(casing).setMinGlobalLimited(42)
                                .or(autoAbilities(definition.getRecipeTypes()))
                                .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(abilities(PartAbility.MUFFLER).setExactLimit(1))
                                .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                        .where('F', frames(GTMaterials.HastelloyC276))
                        .where('G', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                        .where('M', blocks(casing))
                        .where('-', any())
                        .where('~', controller(blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(new ResourceLocation("gtnn", "block/iridium_casing"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .register();

    private SFPreciseAssemblyMachines() {}

    private static Block iridiumCasing() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("gtnn", "iridium_casing"));
    }

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {}

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEu.id("item").equals(event.getTabKey().location())) {
            event.accept(AUTOMATIC_PRECISION_ASSEMBLER.asStack());
        }
    }
}
