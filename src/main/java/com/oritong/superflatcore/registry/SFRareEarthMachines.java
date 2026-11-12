package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.oritong.superflatcore.recipe.SFRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public final class SFRareEarthMachines {
    public static final MultiblockMachineDefinition RARE_EARTH_CENTRIFUGAL = SFRegistrate.REGISTRATE
            .multiblock("rare_earth_centrifugal", WorkableElectricMultiblockMachine::new)
            .langValue("Rare Earth Centrifuge")
            .rotationState(RotationState.ALL)
            .recipeType(SFRecipeTypes.RARE_EARTH_CENTRIFUGAL_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(GTBlocks.CASING_HSSE_STURDY)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" bbb ", "bbbbb", "bbbbb", "bbbbb", " bbb ")
                    .aisle("bbbbb", "bcccb", "bcccb", "bcccb", "bbbbb")
                    .aisle("bbbbb", "bcdcb", "bcdcb", "bcdcb", "bbbbb")
                    .aisle("bbbbb", "bcccb", "bcccb", "bcccb", "bbbbb")
                    .aisle(" bbb ", "bbbbb", "bbabb", "bbbbb", " bbb ")
                    .where('a', controller(blocks(definition.get())))
                    .where('b', blocks(GTBlocks.CASING_HSSE_STURDY.get()).setMinGlobalLimited(80)
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('c', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                    .where('d', blocks(Blocks.CRYING_OBSIDIAN))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"),
                    GTCEu.id("block/multiblock/gcym/large_centrifuge"))
            .register();

    private SFRareEarthMachines() {}

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {}

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEu.id("item").equals(event.getTabKey().location())) {
            event.accept(RARE_EARTH_CENTRIFUGAL.asStack());
        }
    }
}
