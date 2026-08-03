package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.oritong.superflatcore.machine.ConfigurableAutoMaintenanceHatchPartMachine;
import com.oritong.superflatcore.machine.WirelessEnergyHatchPartMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.IV;
import static com.gregtechceu.gtceu.api.GTValues.MAX;
import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.GTValues.ZPM;

public final class SFMachines {
    private static final ResourceLocation GTCEU_ITEM_TAB = new ResourceLocation("gtceu", "item");
    private static final int ENERGY_64A = 64;

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

    private SFMachines() {}

    public static void init() {}

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        init();
    }

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEU_ITEM_TAB.equals(event.getTabKey().location())) {
            event.accept(CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(STERILE_CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            event.accept(CREATIVE_STERILE_CLEANING_CONFIGURABLE_AUTO_MAINTENANCE_HATCH.asStack());
            addTieredMachines(event, LASER_INPUT_HATCH_262144);
            addTieredMachines(event, LASER_OUTPUT_HATCH_262144);
            addTieredMachines(event, LASER_INPUT_HATCH_1048576);
            addTieredMachines(event, LASER_OUTPUT_HATCH_1048576);
            addTieredMachines(event, LASER_INPUT_HATCH_4194304);
            addTieredMachines(event, LASER_OUTPUT_HATCH_4194304);
            addTieredMachines(event, WIRELESS_ENERGY_INPUT_HATCH_64A);
            addTieredMachines(event, WIRELESS_ENERGY_OUTPUT_HATCH_64A);
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
