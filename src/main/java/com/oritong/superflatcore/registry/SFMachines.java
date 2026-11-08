package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.machine.ConfigurableAutoMaintenanceHatchPartMachine;
import com.oritong.superflatcore.machine.HeatExchangerMachine;
import com.oritong.superflatcore.machine.LargeCokeOvenMachine;
import com.oritong.superflatcore.machine.ManaFluidHatchPartMachine;
import com.oritong.superflatcore.machine.ManaOppressorMachine;
import com.oritong.superflatcore.machine.RadioactiveFluidHatchPartMachine;
import com.oritong.superflatcore.machine.WindGeneratorMachine;
import com.oritong.superflatcore.config.SFConfig;
import com.oritong.superflatcore.machine.WirelessEnergyHatchPartMachine;
import com.oritong.superflatcore.recipe.SFRecipeTypes;
import com.oritong.superflatcore.recipe.SFRecipeModifiers;

import mekanism.generators.common.content.blocktype.BlockShapes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.common.block.BotaniaBlocks;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.IV;
import static com.gregtechceu.gtceu.api.GTValues.MAX;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.GTValues.ZPM;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;

public final class SFMachines {
    private static final ResourceLocation GTCEU_ITEM_TAB = new ResourceLocation("gtceu", "item");
    private static final int ENERGY_64A = 64;

    // Force the void miner definition to initialize with the other machine definitions,
    // before creative-tab contents are built and Registrate's mod event bus is closed.
    public static final MachineDefinition LARGE_VOID_MINER = SFVoidMachines.LARGE_VOID_MINER;

    /** Registered after KubeJS has made the shared recipe type available. */
    public static MachineDefinition TWIST_SPACE_TECHNOLOGY;

    public static final MachineDefinition CONFIGURABLE_AUTO_MAINTENANCE_HATCH = SFRegistrate.REGISTRATE
            .machine("configurable_auto_maintenance_hatch", ConfigurableAutoMaintenanceHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel(GTCEu.id("block/machine/part/auto_maintenance_hatch"))
            .tier(IV)
            .register();

    public static final MachineDefinition CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH = SFRegistrate.REGISTRATE
            .machine("cleaning_configurable_auto_maintenance_hatch",
                    holder -> new ConfigurableAutoMaintenanceHatchPartMachine(holder, CleanroomType.CLEANROOM))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel(GTCEu.id("block/machine/part/cleaning_maintenance_hatch"))
            .tier(IV)
            .register();

    public static final MachineDefinition STERILE_CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH = SFRegistrate.REGISTRATE
            .machine("sterile_cleaning_configurable_auto_maintenance_hatch",
                    holder -> new ConfigurableAutoMaintenanceHatchPartMachine(holder, CleanroomType.STERILE_CLEANROOM))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .overlayTieredHullModel(GTCEu.id("block/machine/part/cleaning_maintenance_hatch"))
            .tier(ZPM)
            .register();

    public static final MachineDefinition CREATIVE_STERILE_CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH =
            SFRegistrate.REGISTRATE
                    .machine("creative_sterile_cleaning_configurable_auto_maintenance_hatch",
                            ConfigurableAutoMaintenanceHatchPartMachine::creativeSterile)
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.MAINTENANCE)
                    .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/cleaning_maintenance_hatch"))
                    .tier(MAX)
                    .register();

    public static final MachineDefinition[] LASER_INPUT_HATCH_262144 = registerLaserHatch(IO.IN, 262144,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_262144 = registerLaserHatch(IO.OUT, 262144,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_1048576 = registerLaserHatch(IO.IN, 1048576,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_1048576 = registerLaserHatch(IO.OUT, 1048576,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_4194304 = registerLaserHatch(IO.IN, 4194304,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_4194304 = registerLaserHatch(IO.OUT, 4194304,
            PartAbility.OUTPUT_LASER);

    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_64A = registerWirelessEnergyHatch64(IO.IN);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_64A = registerWirelessEnergyHatch64(IO.OUT);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_4A = registerPackEnergyHatch(
            IO.IN, 4, false, LV, GTValues.MV, HV);
    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_4A = registerPackEnergyHatch(
            IO.OUT, 4, false, LV, GTValues.MV, HV);
    public static final MachineDefinition[] ENERGY_INPUT_HATCH_16A = registerPackEnergyHatch(
            IO.IN, 16, false, GTValues.MV, HV);
    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_16A = registerPackEnergyHatch(
            IO.OUT, 16, false, GTValues.MV, HV);
    public static final MachineDefinition[] SUBSTATION_ENERGY_INPUT_HATCH = registerPackEnergyHatch(
            IO.IN, ENERGY_64A, true, HV);
    public static final MachineDefinition[] SUBSTATION_ENERGY_OUTPUT_HATCH = registerPackEnergyHatch(
            IO.OUT, ENERGY_64A, true, HV);

    public static final MachineDefinition EV_PARALLEL_HATCH = registerParallelHatch(
            "ev_parallel_hatch", GTValues.EV, "block/machines/parallel_hatch_mk1",
            "§5EV§r Parallel Control Hatch", 16, false);
    public static final MachineDefinition TRANSDIMENSIONAL_PARALLEL_HATCH = registerParallelHatch(
            "transdimensional_parallel_hatch", MAX, "block/machines/parallel_hatch_mk4",
            "Transdimensional Parallel Control Hatch", Integer.MAX_VALUE, true);

    public static final MachineDefinition[] WIRELESS_LASER_INPUT_HATCH_262144 = registerWirelessLaserHatch(IO.IN,
            262144, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] WIRELESS_LASER_OUTPUT_HATCH_262144 = registerWirelessLaserHatch(IO.OUT,
            262144, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] WIRELESS_LASER_INPUT_HATCH_1048576 = registerWirelessLaserHatch(IO.IN,
            1048576, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] WIRELESS_LASER_OUTPUT_HATCH_1048576 = registerWirelessLaserHatch(IO.OUT,
            1048576, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] WIRELESS_LASER_INPUT_HATCH_4194304 = registerWirelessLaserHatch(IO.IN,
            4194304, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] WIRELESS_LASER_OUTPUT_HATCH_4194304 = registerWirelessLaserHatch(IO.OUT,
            4194304, PartAbility.OUTPUT_LASER);

    public static final MachineDefinition RADIOACTIVE_FLUID_INPUT_HATCH = SFRegistrate.REGISTRATE
            .machine("radioactive_fluid_input_hatch", holder -> new RadioactiveFluidHatchPartMachine(holder, IO.IN))
            .langValue("Radioactive Material Input Hatch")
            .rotationState(RotationState.ALL)
            .tier(IV)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    FormattingUtil.formatNumbers(RadioactiveFluidHatchPartMachine.CAPACITY)))
            .overlayTieredHullModel(SuperFlatCore.id("block/machine/part/radioactive_fluid_input_hatch"))
            .register();

    public static final MachineDefinition RADIOACTIVE_FLUID_OUTPUT_HATCH = SFRegistrate.REGISTRATE
            .machine("radioactive_fluid_output_hatch", holder -> new RadioactiveFluidHatchPartMachine(holder, IO.OUT))
            .langValue("Radioactive Material Output Hatch")
            .rotationState(RotationState.ALL)
            .tier(IV)
            .abilities(PartAbility.EXPORT_FLUIDS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    FormattingUtil.formatNumbers(RadioactiveFluidHatchPartMachine.CAPACITY)))
            .overlayTieredHullModel(SuperFlatCore.id("block/machine/part/radioactive_fluid_output_hatch"))
            .register();

    public static final MachineDefinition MANA_FLUID_INPUT_HATCH = SFRegistrate.REGISTRATE
            .machine("mana_fluid_input_hatch", holder -> new ManaFluidHatchPartMachine(holder, IO.IN))
            .langValue("Mana Fluid Input Hatch")
            .rotationState(RotationState.ALL)
            .tier(IV)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    FormattingUtil.formatNumbers(ManaFluidHatchPartMachine.CAPACITY)))
            .overlayTieredHullModel(SuperFlatCore.id("block/machine/part/mana_fluid_input_hatch"))
            .register();

    public static final MachineDefinition MANA_FLUID_OUTPUT_HATCH = SFRegistrate.REGISTRATE
            .machine("mana_fluid_output_hatch", holder -> new ManaFluidHatchPartMachine(holder, IO.OUT))
            .langValue("Mana Fluid Output Hatch")
            .rotationState(RotationState.ALL)
            .tier(IV)
            .abilities(PartAbility.EXPORT_FLUIDS)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    FormattingUtil.formatNumbers(ManaFluidHatchPartMachine.CAPACITY)))
            .overlayTieredHullModel(SuperFlatCore.id("block/machine/part/mana_fluid_output_hatch"))
            .register();

    public static final MachineDefinition LARGE_COKE_OVEN = SFRegistrate.REGISTRATE
            .multiblock("large_coke_oven", LargeCokeOvenMachine::new)
            .langValue("Large Coke Oven")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.COKE_OVEN_RECIPES)
            .recipeModifier(LargeCokeOvenMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_COKE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("   AAAAA   ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .aisle("  AAAAAAA  ", "   BBBBB   ", "   BBBBB   ", "   AAAAA   ", "   AAAAA   ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .aisle(" AAAAAAAAA ", "  B  D  B  ", "  B     B  ", "  A     A  ", "  A     A  ", "   AAAAA   ", "     A     ", "     A     ", "     A     ", "           ", "           ")
                    .aisle("AAAAAAAAAAA", " B   D   B ", " B       B ", " A       A ", " A       A ", "  AAAAAAA  ", "    ADA    ", "    A A    ", "    A A    ", "    AAA    ", "           ")
                    .aisle("AAAAAAAAAAA", " B   D   B ", " B       B ", " A       A ", " A       A ", "  AA   AA  ", "   A D A   ", "   A   A   ", "   A   A   ", "   AAAAA   ", "    AAA    ")
                    .aisle("AAAAAAAAAAA", " BDDDDDDDB ", " B   D   B ", " A   D   A ", " A   D   A ", "  AA D AA  ", "  ADDDDDA  ", "  A  D  A  ", "  A  D  A  ", "   AADAA   ", "    AEA    ")
                    .aisle("AAAAAAAAAAA", " B   D   B ", " B       B ", " A       A ", " A       A ", "  AA   AA  ", "   A D A   ", "   A   A   ", "   A   A   ", "   AAAAA   ", "    AAA    ")
                    .aisle("AAAAAAAAAAA", " B   D   B ", " B       B ", " A       A ", " A       A ", "  AAAAAAA  ", "    ADA    ", "    A A    ", "    A A    ", "    AAA    ", "           ")
                    .aisle(" AAAAAAAAA ", "  B  D  B  ", "  B     B  ", "  A     A  ", "  A     A  ", "   AAAAA   ", "     A     ", "     A     ", "     A     ", "           ", "           ")
                    .aisle("  AAAAAAA  ", "   BBCBB   ", "   BBBBB   ", "   AAAAA   ", "   AAAAA   ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .aisle("   AAAAA   ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .where('C', controller(blocks(definition.get())))
                    .where('B', blocks(GTBlocks.CASING_COKE_BRICKS.get())
                            .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true)))
                    .where('A', blocks(GTBlocks.CASING_COKE_BRICKS.get()))
                    .where('D', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                    .where('E', abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where(' ', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_coke_bricks"),
                    GTCEu.id("block/multiblock/coke_oven"))
            .register();

    public static final MachineDefinition HEAT_EXCHANGER = SFRegistrate.REGISTRATE
            .multiblock("heat_exchanger", HeatExchangerMachine::new)
            .langValue("Heat Exchanger")
            .tooltips(Component.translatable("gtceu.machine.heat_exchanger.tooltip.0"))
            .tooltips(Component.translatable("gtceu.machine.heat_exchanger.tooltip.1"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.heat_exchanger")))
            .rotationState(RotationState.ALL)
            .recipeType(SFRecipeTypes.HEAT_EXCHANGER_RECIPES)
            .recipeModifier(HeatExchangerMachine::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" AAA ", " AAA ", " AAA ", " AAA ", " AAA ", " AAA ")
                    .aisle("AAAAA", "BCCCB", "BCCCB", "BCCCB", "BCCCB", "AAAAA")
                    .aisle("AAAAA", "BDDDB", "BDCDB", "BDCDB", "BDDDB", "AAAAA")
                    .aisle("AAAAA", "BCCCB", "BCCCB", "BCCCB", "BCCCB", "AAAAA")
                    .aisle("AAAAA", "BDDDB", "BDCDB", "BDCDB", "BDDDB", "AAAAA")
                    .aisle("AAAAA", "BCCCB", "BCCCB", "BCCCB", "BCCCB", "AAAAA")
                    .aisle("AAAAA", "BDDDB", "BDCDB", "BDCDB", "BDDDB", "AAAAA")
                    .aisle("AAAAA", "BCCCB", "BCCCB", "BCCCB", "BCCCB", "AAAAA")
                    .aisle(" ASA ", " AAA ", " AAA ", " AAA ", " AAA ", " AAA ")
                    .where('S', controller(blocks(definition.get())))
                    .where('A', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()).setMinGlobalLimited(98)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(blocks(RADIOACTIVE_FLUID_INPUT_HATCH.get()))
                            .or(blocks(RADIOACTIVE_FLUID_OUTPUT_HATCH.get())))
                    .where('C', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('B', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('D', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.HSSG)))
                    .where(' ', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static final MachineDefinition MANA_OPPRESSOR = SFRegistrate.REGISTRATE
            .multiblock("mana_oppressor", ManaOppressorMachine::new)
            .langValue("Mana Oppressor")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(SFRecipeTypes.MANA_INFUSION_RECIPES, SFRecipeTypes.MANA_OPPRESSION_RECIPES)
            .recipeModifiers(true, SFRecipeModifiers.MULTIPLERECIPE, ManaOppressorMachine::recipeModifier)
            .appearanceBlock(() -> BotaniaBlocks.livingrockBrick)
            .workableCasingModel(new ResourceLocation("botania", "block/livingrock_bricks"),
                    GTCEu.id("block/multiblock/gcym/large_mixer"))
            .pattern(definition -> {
                Block moltenDawnstone = ForgeRegistries.BLOCKS.getValue(
                        new ResourceLocation("embers", "molten_dawnstone_block"));
                var hatchAbilities = autoAbilities(definition.getRecipeTypes(), false, false,
                        true, true, true, true).or(autoAbilities(true, false, false))
                        .or(Predicates.custom(
                                state -> ManaOppressorMachine.isOppressionMode(state)
                                        && com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(
                                                state.getWorld(), state.getPos())
                                                instanceof com.gregtechceu.gtceu.api.capability.IParallelHatch,
                                () -> new com.lowdragmc.lowdraglib.utils.BlockInfo[0])
                                .setMaxGlobalLimited(1));
                var specialBase = Predicates.custom(
                        state -> ManaOppressorMachine.isValidBaseBlock(
                                state.getBlockState(), state.getWorld()),
                        () -> {
                            Block otherstone = ForgeRegistries.BLOCKS.getValue(
                                    new ResourceLocation("occultism", "otherstone"));
                            if (otherstone == null) {
                                return new com.lowdragmc.lowdraglib.utils.BlockInfo[] {
                                        new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.alchemyCatalyst),
                                        new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.conjurationCatalyst),
                                        new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.manaDiamondBlock),
                                        new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.manaVoid) };
                            }
                            return new com.lowdragmc.lowdraglib.utils.BlockInfo[] {
                                    new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.alchemyCatalyst),
                                    new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.conjurationCatalyst),
                                    new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.manaDiamondBlock),
                                    new com.lowdragmc.lowdraglib.utils.BlockInfo(BotaniaBlocks.manaVoid),
                                    new com.lowdragmc.lowdraglib.utils.BlockInfo(otherstone) };
                        });
                return FactoryBlockPattern.start()
                        .aisle("AI HA", "ADADA", "ADDDA", "ADADA", "AK JA", "     ")
                        .aisle("CAAAB", "DAAAD", "D   D", "D   D", "GAAAF", " JAK ")
                        .aisle(" ALA ", "AAMAA", "D M D", "A M A", " AMA ", " ANA ")
                        .aisle("HAAAI", "DAAAD", "D   D", "D   D", "JAAAK", " GAF ")
                        .aisle("AB CA", "ADEDA", "ADDDA", "ADADA", "AF GA", "     ")
                        .where('E', controller(blocks(definition.get())))
                        .where('A', blocks(BotaniaBlocks.livingrockBrick)
                                .or(blocks(MANA_FLUID_INPUT_HATCH.get()).setMinGlobalLimited(1))
                                .or(hatchAbilities))
                        .where('D', blocks(BotaniaBlocks.manaGlass))
                        .where('L', specialBase)
                        .where('M', blocks(moltenDawnstone))
                        .where('N', blocks(moltenDawnstone))
                        .where('B', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('C', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('F', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('G', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('H', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('I', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('J', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where('K', blocks(BotaniaBlocks.livingrockBrickWall).or(hatchAbilities))
                        .where(' ', Predicates.any())
                        .build();
            })
            .register();

    public static final MachineDefinition SUPERCRITICAL_STEAM_TURBINE = registerSupercriticalSteamTurbine();

    public static final MachineDefinition WIND_GENERATOR = SFRegistrate.REGISTRATE
            .machine("wind_generator", holder -> new WindGeneratorMachine(holder, LV))
            .langValue("Wind Generator")
            .rotationState(RotationState.NON_Y_AXIS)
            .shape(BlockShapes.WIND_GENERATOR[0])
            .tier(LV)
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createBasicMachineModel(
                    new ResourceLocation("mekanismgenerators", "block/wind_generator")))
            .tooltips(Component.translatable("gtceu.universal.tooltip.base_production_eut",
                    SFConfig.WIND_GENERATOR_BASE_PRODUCTION.get()))
            .tooltips(Component.translatable("tooltip.superflatcore.wind_generator.height_output"))
            .tooltips(Component.translatable("tooltip.superflatcore.wind_generator.rotor_wear",
                    SFConfig.WIND_GENERATOR_ACTIVE_ROTOR_WEAR.get(),
                    SFConfig.WIND_GENERATOR_FULL_ROTOR_WEAR.get()))
            .hasBER(true)
            .register();

    private static MachineDefinition registerSupercriticalSteamTurbine() {
        return SFRegistrate.REGISTRATE.multiblock("supercritical_steam_turbine",
                        holder -> new LargeTurbineMachine(holder, IV))
                .rotationState(RotationState.ALL)
                .recipeType(SFRecipeTypes.SUPERCRITICAL_STEAM_TURBINE_FUELS)
                .generator(true)
                .recipeModifier(LargeTurbineMachine::recipeModifier, true)
                .appearanceBlock(SFBlocks.SUPERCRITICAL_TURBINE_CASING)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("CCCCC", "CHHHC", "CCCCC")
                        .aisle("CHHHC", "RGGGR", "CHHHC")
                        .aisle("CCCCC", "CHSHC", "CCCCC")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('G', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                        .where('C', blocks(SFBlocks.SUPERCRITICAL_TURBINE_CASING.get()))
                        .where('R', abilities(PartAbility.ROTOR_HOLDER).setExactLimit(1)
                                .or(abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1)))
                        .where('H', blocks(SFBlocks.SUPERCRITICAL_TURBINE_CASING.get())
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .where(' ', Predicates.any())
                        .build())
                .recoveryItems(() -> new ItemLike[] {
                        GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingModel(SuperFlatCore.id("block/supercritical_turbine_casing"),
                        GTCEu.id("block/multiblock/generator/large_plasma_turbine"))
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", GTValues.V[IV] * 2),
                        Component.translatable("gtceu.multiblock.turbine.efficiency_tooltip", GTValues.VNF[IV]))
                .register();
    }

    private static MachineDefinition registerTwistSpaceTechnology(GTRecipeType recipeType) {
        return SFRegistrate.REGISTRATE.multiblock("twist_space_technology",
                        CoilWorkableElectricMultiblockMachine::new)
                .langValue("深度化学扭曲仪")
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(recipeType)
                .recipeModifiers(SFRecipeModifiers.COIL_TEMPERATURE_PARALLEL,
                        GTRecipeModifiers.OC_PERFECT_SUBTICK)
                .beforeWorking((machine, recipe) -> machine instanceof CoilWorkableElectricMultiblockMachine coilMachine
                        && recipe.data.getInt("ebf_temp") <= coilMachine.getCoilType().getCoilTemperature())
                .appearanceBlock(GTBlocks.CASING_PTFE_INERT)
                .pattern(definition -> FactoryBlockPattern.start()
                                .aisle("   AAAAA   ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "   AAAAA   ")
                                .aisle("  ACCCCCA  ", "     C     ", "    C      ", "           ", "           ", "      C    ", "     C     ", "    C      ", "           ", "           ", "      C    ", "     C     ", "    C      ", "           ", "           ", "      C    ", "     C     ", "    C      ", "           ", "           ", "      C    ", "     C     ", "    C      ", "           ", "           ", "      C    ", "     C     ", "  ACCCCCA  ")
                                .aisle(" ACCCCCCCA ", "     D     ", "           ", "   C       ", "       C   ", "           ", "           ", "           ", "   C       ", "       C   ", "           ", "           ", "           ", "   C       ", "       C   ", "           ", "           ", "           ", "   C       ", "       C   ", "           ", "           ", "           ", "   C       ", "       C   ", "           ", "     D     ", " ACCCCCCCA ")
                                .aisle("ACCCCCCCCCA", "     D     ", "           ", "        C  ", "  C        ", "           ", "           ", "           ", "        C  ", "  C        ", "           ", "           ", "           ", "        C  ", "  C        ", "           ", "           ", "           ", "        C  ", "  C        ", "           ", "           ", "           ", "        C  ", "  C        ", "           ", "     D     ", "ACCCCCCCCCA")
                                .aisle("ACCCCCCCCCA", "     D     ", "     D   C ", "     D     ", "     D     ", " C   D     ", "     D     ", "     D   C ", "     D     ", "     D     ", " C   D     ", "     D     ", "     D   C ", "     D     ", "     D     ", " C   D     ", "     D     ", "     D   C ", "     D     ", "     D     ", " C   D     ", "     D     ", "     D   C ", "     D     ", "     D     ", " C   D     ", "     D     ", "ACCCCCCCCCA")
                                .aisle("ACCCCECCCCA", " CDDDFDDDC ", "    DFD    ", "    DFD    ", "    DFD    ", "    DFD    ", " C  DFD  C ", "    DFD    ", "    DFD    ", "    DFD    ", "    DFD    ", " C  DFD  C ", "    DFD    ", "    DFD    ", "    DFD    ", "    DFD    ", " C  DFD  C ", "    DFD    ", "    DFD    ", "    DFD    ", "    DFD    ", " C  DFD  C ", "    DFD    ", "    DFD    ", "    DFD    ", "    DFD    ", " CDDDFDDDC ", "ACCCCECCCCA")
                                .aisle("ACCCCCCCCCA", "     D     ", " C   D     ", "     D     ", "     D     ", "     D   C ", "     D     ", " C   D     ", "     D     ", "     D     ", "     D   C ", "     D     ", " C   D     ", "     D     ", "     D     ", "     D   C ", "     D     ", " C   D     ", "     D     ", "     D     ", "     D   C ", "     D     ", " C   D     ", "     D     ", "     D     ", "     D   C ", "     D     ", "ACCCCCCCCCA")
                                .aisle("ACCCCCCCCCA", "     D     ", "           ", "  C        ", "        C  ", "           ", "           ", "           ", "  C        ", "        C  ", "           ", "           ", "           ", "  C        ", "        C  ", "           ", "           ", "           ", "  C        ", "        C  ", "           ", "           ", "           ", "  C        ", "        C  ", "           ", "     D     ", "ACCCCCCCCCA")
                                .aisle(" ACCCCCCCA ", "     D     ", "           ", "       C   ", "   C       ", "           ", "           ", "           ", "       C   ", "   C       ", "           ", "           ", "           ", "       C   ", "   C       ", "           ", "           ", "           ", "       C   ", "   C       ", "           ", "           ", "           ", "       C   ", "   C       ", "           ", "     D     ", " ACCCCCCCA ")
                                .aisle("  ACCCCCA  ", "     C     ", "      C    ", "           ", "           ", "    C      ", "     C     ", "      C    ", "           ", "           ", "    C      ", "     C     ", "      C    ", "           ", "           ", "    C      ", "     C     ", "      C    ", "           ", "           ", "    C      ", "     C     ", "      C    ", "           ", "           ", "    C      ", "     C     ", "  ACCCCCA  ")
                                .aisle("   AABAA   ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "   AAAAA   ")
                        .where("B", controller(blocks(definition.get())))
                        .where("A", blocks(GTBlocks.CASING_PTFE_INERT.get())
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                        .where("C", blocks(GTBlocks.CASING_PTFE_INERT.get()))
                        .where("D", blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where("E", blocks(GTBlocks.CASING_PTFE_INERT.get())
                                .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2)))
                        .where("F", Predicates.heatingCoils())
                        .where(" ", Predicates.any())
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                        GTCEu.id("block/multiblock/large_chemical_reactor"))
                .register();
    }

    private SFMachines() {}

    public static void init() {}

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTRecipeType twistSpaceTechnology = GTRegistries.RECIPE_TYPES
                .get(new ResourceLocation("gtceu", "twist_space_technology"));
        if (twistSpaceTechnology == null) {
            throw new IllegalStateException("The KubeJS recipe type gtceu:twist_space_technology was not registered");
        }
        if (TWIST_SPACE_TECHNOLOGY == null) {
            TWIST_SPACE_TECHNOLOGY = registerTwistSpaceTechnology(twistSpaceTechnology);
        }
        init();
    }

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEU_ITEM_TAB.equals(event.getTabKey().location())) {
            event.accept(CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(STERILE_CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(CREATIVE_STERILE_CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(RADIOACTIVE_FLUID_INPUT_HATCH.asStack());
            event.accept(RADIOACTIVE_FLUID_OUTPUT_HATCH.asStack());
            event.accept(MANA_FLUID_INPUT_HATCH.asStack());
            event.accept(MANA_FLUID_OUTPUT_HATCH.asStack());
            event.accept(LARGE_COKE_OVEN.asStack());
            event.accept(LARGE_VOID_MINER.asStack());
            event.accept(HEAT_EXCHANGER.asStack());
            event.accept(MANA_OPPRESSOR.asStack());
            event.accept(SUPERCRITICAL_STEAM_TURBINE.asStack());
            if (TWIST_SPACE_TECHNOLOGY != null) {
                event.accept(TWIST_SPACE_TECHNOLOGY.asStack());
            }
            event.accept(WIND_GENERATOR.asStack());
            event.accept(SFBlocks.SUPERCRITICAL_TURBINE_CASING.asStack());
            event.accept(SFBlocks.RADIOACTIVE_FLUID_TANK.asStack());
            SFBlocks.ELEMENTAL_CORUNDUM_CLUSTERS.forEach(cluster -> event.accept(cluster.asStack()));
            addTieredMachines(event, LASER_INPUT_HATCH_262144);
            addTieredMachines(event, LASER_OUTPUT_HATCH_262144);
            addTieredMachines(event, LASER_INPUT_HATCH_1048576);
            addTieredMachines(event, LASER_OUTPUT_HATCH_1048576);
            addTieredMachines(event, LASER_INPUT_HATCH_4194304);
            addTieredMachines(event, LASER_OUTPUT_HATCH_4194304);
            addTieredMachines(event, WIRELESS_ENERGY_INPUT_HATCH_64A);
            addTieredMachines(event, WIRELESS_ENERGY_OUTPUT_HATCH_64A);
            addTieredMachines(event, ENERGY_INPUT_HATCH_4A);
            addTieredMachines(event, ENERGY_OUTPUT_HATCH_4A);
            addTieredMachines(event, ENERGY_INPUT_HATCH_16A);
            addTieredMachines(event, ENERGY_OUTPUT_HATCH_16A);
            addTieredMachines(event, SUBSTATION_ENERGY_INPUT_HATCH);
            addTieredMachines(event, SUBSTATION_ENERGY_OUTPUT_HATCH);
            event.accept(EV_PARALLEL_HATCH.asStack());
            event.accept(TRANSDIMENSIONAL_PARALLEL_HATCH.asStack());
            addTieredMachines(event, WIRELESS_LASER_INPUT_HATCH_262144);
            addTieredMachines(event, WIRELESS_LASER_OUTPUT_HATCH_262144);
            addTieredMachines(event, WIRELESS_LASER_INPUT_HATCH_1048576);
            addTieredMachines(event, WIRELESS_LASER_OUTPUT_HATCH_1048576);
            addTieredMachines(event, WIRELESS_LASER_INPUT_HATCH_4194304);
            addTieredMachines(event, WIRELESS_LASER_OUTPUT_HATCH_4194304);
        }
    }

    private static MachineDefinition[] registerWirelessEnergyHatch64(IO io) {
        String energyName = io == IO.IN ? "input" : "output";
        return GTMachineUtils.registerTieredMachines(SFRegistrate.REGISTRATE,
                ENERGY_64A + "a_wireless_energy_" + energyName + "_hatch",
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, ENERGY_64A),
                (tier, builder) -> buildWirelessEnergyHatch64(tier, builder, io, energyName),
                GTValues.tiersBetween(HV, maxElectricTier()));
    }

    private static MachineDefinition registerParallelHatch(String name, int tier, String overlay,
                                                            String displayName, int parallelLimit,
                                                            boolean allowStructureSharing) {
        return SFRegistrate.REGISTRATE.machine(name,
                        holder -> new ParallelHatchPartMachine(holder, tier))
                .langValue(displayName)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.PARALLEL_HATCH)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(GTMachineModels.createWorkableTieredHullMachineModel(GTCEu.id(overlay))
                        .andThen((ctx, provider, model) -> model.addReplaceableTextures("bottom", "top", "side")))
                .tooltips(Component.translatable("superflatcore.machine.parallel_hatch.tooltip", parallelLimit),
                        Component.translatable(allowStructureSharing
                                ? "gtceu.part_sharing.enabled" : "gtceu.part_sharing.disabled"))
                .tier(tier)
                .register();
    }

    private static MachineDefinition[] registerPackEnergyHatch(IO io, int amperage, boolean substation,
                                                                int... tiers) {
        boolean input = io == IO.IN;
        String direction = input ? "input" : "output";
        String name = substation
                ? "substation_" + direction + "_hatch_" + amperage + "a"
                : "energy_" + direction + "_hatch_" + amperage + "a";
        String overlay = "energy_" + direction + "_hatch_" + amperage + "a";
        PartAbility ability = substation
                ? (input ? PartAbility.SUBSTATION_INPUT_ENERGY : PartAbility.SUBSTATION_OUTPUT_ENERGY)
                : (input ? PartAbility.INPUT_ENERGY : PartAbility.OUTPUT_ENERGY);

        return GTMachineUtils.registerTieredMachines(SFRegistrate.REGISTRATE, name,
                (holder, tier) -> new EnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + " " + amperage + "A "
                                + (substation ? "Substation " : "")
                                + (input ? "Energy Hatch" : "Dynamo Hatch"))
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_" + (input ? "in" : "out"),
                                        FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_" + (input ? "in" : "out"),
                                        amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil.formatNumbers(
                                                EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))),
                                Component.translatable(substation
                                        ? "gtceu.machine.substation_hatch." + direction + ".tooltip"
                                        : "gtceu.machine.energy_hatch." + direction + "_hi_amp.tooltip"))
                        .overlayTieredHullModel(GTCEu.id("block/machine/part/" + overlay))
                        .register(), tiers);
    }

    private static MachineDefinition buildWirelessEnergyHatch64(Integer tier,
                                                                MachineBuilder<MachineDefinition, ?> builder,
                                                                IO io,
                                                                String energyName) {
        boolean input = io == IO.IN;
        return builder
                .langValue(GTValues.VNF[tier] + " " + ENERGY_64A + "A Wireless "
                        + (input ? "Energy Hatch" : "Dynamo Hatch"))
                .rotationState(RotationState.ALL)
                .abilities(input ? PartAbility.INPUT_ENERGY : PartAbility.OUTPUT_ENERGY)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .tooltips(
                        Component.translatable("gtmthings.machine.energy_hatch." + energyName + ".tooltip"),
                        Component.translatable("gtmthings.machine.wireless_energy_hatch." + energyName + ".tooltip"),
                        Component.translatable("gtceu.universal.tooltip.voltage_" + (input ? "in" : "out"),
                                FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                        Component.translatable("gtceu.universal.tooltip.amperage_" + (input ? "in" : "out"),
                                ENERGY_64A),
                        Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(
                                        EnergyHatchPartMachine.getHatchEnergyCapacity(tier, ENERGY_64A))))
                .overlayTieredHullModel(gtmThingsId("block/machine/part/wireless_energy_hatch_64a"))
                .register();
    }

    private static MachineDefinition[] registerLaserHatch(IO io, int amperage, PartAbility ability) {
        String laserName = io == IO.IN ? "target" : "source";
        return GTMachineUtils.registerTieredMachines(SFRegistrate.REGISTRATE,
                amperage + "a_laser_" + laserName + "_hatch",
                (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage),
                (tier, builder) -> buildLaserHatch(tier, builder, io, amperage, ability, laserName),
                GTValues.tiersBetween(IV, maxElectricTier()));
    }

    private static MachineDefinition buildLaserHatch(Integer tier,
                                                     MachineBuilder<MachineDefinition, ?> builder,
                                                     IO io,
                                                     int amperage,
                                                     PartAbility ability,
                                                     String laserName) {
        return builder
                .langValue(GTValues.VNF[tier] + " " + FormattingUtil.formatNumbers(amperage)
                        + "A Laser " + FormattingUtil.toEnglishName(laserName) + " Hatch")
                .rotationState(RotationState.ALL)
                .tooltips(
                        Component.translatable("gtceu.machine.laser_hatch." + laserName + ".tooltip"),
                        Component.translatable("gtceu.machine.laser_hatch.both.tooltip"),
                        Component.translatable("gtceu.universal.tooltip.voltage_" + (io == IO.IN ? "in" : "out"),
                                FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                        Component.translatable("gtceu.universal.tooltip.amperage_in", amperage),
                        Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(
                                        EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .abilities(ability)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .overlayTieredHullModel(GTCEu.id("block/machine/part/laser_" + laserName + "_hatch"))
                .register();
    }

    private static MachineDefinition[] registerWirelessLaserHatch(IO io, int amperage, PartAbility ability) {
        String laserName = io == IO.IN ? "target" : "source";
        return GTMachineUtils.registerTieredMachines(SFRegistrate.REGISTRATE,
                amperage + "a_wireless_laser_" + laserName + "_hatch",
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> buildWirelessLaserHatch(tier, builder, io, amperage, ability, laserName),
                GTValues.tiersBetween(IV, maxElectricTier()));
    }

    private static MachineDefinition buildWirelessLaserHatch(Integer tier,
                                                             MachineBuilder<MachineDefinition, ?> builder,
                                                             IO io,
                                                             int amperage,
                                                             PartAbility ability,
                                                             String laserName) {
        return builder
                .langValue(GTValues.VNF[tier] + " " + FormattingUtil.formatNumbers(amperage)
                        + "A Wireless Laser " + FormattingUtil.toEnglishName(laserName) + " Hatch")
                .rotationState(RotationState.ALL)
                .abilities(ability)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .tooltips(
                        Component.translatable("gtmthings.machine.energy_hatch." + laserName + ".tooltip"),
                        Component.translatable("gtmthings.machine.wireless_energy_hatch." + laserName + ".tooltip"),
                        Component.translatable("gtceu.universal.tooltip.voltage_" + (io == IO.IN ? "in" : "out"),
                                FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                        Component.translatable("gtceu.universal.tooltip.amperage_in", amperage),
                        Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(
                                        EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))))
                .overlayTieredHullModel(gtmThingsId("block/machine/part/wireless_laser_hatch"))
                .register();
    }

    private static int maxElectricTier() {
        return GTCEuAPI.isHighTier() ? MAX : UHV;
    }

    private static ResourceLocation gtmThingsId(String path) {
        return new ResourceLocation("gtmthings", path);
    }

    private static void addTieredMachines(BuildCreativeModeTabContentsEvent event, MachineDefinition[] machines) {
        for (MachineDefinition machine : machines) {
            if (machine != null) {
                event.accept(machine.asStack());
            }
        }
    }

}
