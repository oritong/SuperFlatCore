package com.oritong.superflatcore.integration.gtceu;

import com.hepdd.gtmthings.data.CustomItems;
import com.hepdd.gtmthings.data.WirelessMachines;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFRegistrate;
import com.oritong.superflatcore.registry.SFMachines;
import com.oritong.superflatcore.registry.WindRotorPrefix;

import mekanism.common.registries.MekanismItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES;

@GTAddon
public class SFGTAddon implements IGTAddon {
    @Override
    public GTRegistrate getRegistrate() {
        return SFRegistrate.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return SuperFlatCore.MODID;
    }

    @Override
    @SuppressWarnings("removal")
    public void registerTagPrefixes() {
        WindRotorPrefix.register();
        SFMineralProcessingMaterials.registerMilledPrefix();
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        SFMineralProcessingRecipes.addRecipes(provider);
        SFBlackHoleRecipes.addRecipes(provider);
        ItemStack steelRod = ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.Steel);
        if (steelRod.isEmpty()) {
            return;
        }
        for (Material material : GTMaterialItems.MATERIAL_ITEMS.columnKeySet()) {
            if (!WindRotorPrefix.canGenerateFor(material)) {
                continue;
            }
            ItemStack doublePlates = ChemicalHelper.get(TagPrefix.plateDouble, material, 4);
            ItemStack windRotor = ChemicalHelper.get(WindRotorPrefix.WIND_ROTOR, material);
            if (doublePlates.isEmpty() || windRotor.isEmpty()) {
                continue;
            }
            GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("wind_rotor_" + material.getName())
                    .inputItems(steelRod)
                    .inputItems(doublePlates)
                    .outputItems(windRotor)
                    .EUt(16)
                    .duration(10)
                    .save(provider);
        }

        addEnergyHatchRecipes(provider);
        addWirelessEnergyHatchRecipes(provider);
        addElectrolyzerRecipes(provider);
        addParallelHatchRecipes(provider);
    }

    private static void addParallelHatchRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, SuperFlatCore.id("ev_parallel_hatch"),
                SFMachines.EV_PARALLEL_HATCH.asStack(),
                "SZE", "ZHZ", "CZC",
                'S', SENSOR_EV.asStack(),
                'Z', CustomTags.IV_CIRCUITS,
                'E', EMITTER_EV.asStack(),
                'H', HULL[EV].asStack(),
                'C', new MaterialEntry(cableGtDouble, Aluminium));

    }

    /**
     * Replace GTCEu's stock energy/dynamo hatch recipes with the pack variant.
     * Two ceramic plates are selected by the recipe tier; high tiers use
     * tungsten tetraboride ceramic until dedicated materials are introduced.
     */
    private static void addEnergyHatchRecipes(Consumer<FinishedRecipe> provider) {
        int baseTiers = Math.min(VN.length, Math.min(ENERGY_INPUT_HATCH.length, ENERGY_OUTPUT_HATCH.length));
        for (int tier = 0; tier < baseTiers; tier++) {
            if (ENERGY_INPUT_HATCH[tier] != null) {
                addEnergyHatchRecipe(provider, tier, ENERGY_INPUT_HATCH[tier]);
            }
            if (ENERGY_OUTPUT_HATCH[tier] != null) {
                addDynamoHatchRecipe(provider, tier, ENERGY_OUTPUT_HATCH[tier]);
            }
        }

        MachineDefinition[] energyInput4A = addMissingPackHatches(ENERGY_INPUT_HATCH_4A,
                SFMachines.ENERGY_INPUT_HATCH_4A);
        MachineDefinition[] energyOutput4A = addMissingPackHatches(ENERGY_OUTPUT_HATCH_4A,
                SFMachines.ENERGY_OUTPUT_HATCH_4A);
        MachineDefinition[] energyInput16A = addMissingPackHatches(ENERGY_INPUT_HATCH_16A,
                SFMachines.ENERGY_INPUT_HATCH_16A);
        MachineDefinition[] energyOutput16A = addMissingPackHatches(ENERGY_OUTPUT_HATCH_16A,
                SFMachines.ENERGY_OUTPUT_HATCH_16A);
        MachineDefinition[] substationEnergyInput = addMissingPackHatches(SUBSTATION_ENERGY_INPUT_HATCH,
                SFMachines.SUBSTATION_ENERGY_INPUT_HATCH);
        MachineDefinition[] substationEnergyOutput = addMissingPackHatches(SUBSTATION_ENERGY_OUTPUT_HATCH,
                SFMachines.SUBSTATION_ENERGY_OUTPUT_HATCH);

        addAmpHatchRecipes(provider, energyInput4A, ENERGY_INPUT_HATCH, null,
                "energy_hatch_4a", GTCraftingComponents.WIRE_QUAD, 2, 4, 100, false);
        addAmpHatchRecipes(provider, energyOutput4A, ENERGY_OUTPUT_HATCH, null,
                "dynamo_hatch_4a", GTCraftingComponents.WIRE_QUAD, 2, 4, 100, true);
        addAmpHatchRecipes(provider, energyInput16A, energyInput4A, TRANSFORMER,
                "energy_hatch_16a", GTCraftingComponents.WIRE_OCT, 4, 8, 200, false);
        addAmpHatchRecipes(provider, energyOutput16A, energyOutput4A, TRANSFORMER,
                "dynamo_hatch_16a", GTCraftingComponents.WIRE_OCT, 4, 8, 200, true);
        addAmpHatchRecipes(provider, substationEnergyInput, energyInput16A, POWER_TRANSFORMER,
                "substation_energy_hatch", GTCraftingComponents.WIRE_HEX, 6, 16, 400, false);
        addAmpHatchRecipes(provider, substationEnergyOutput, energyOutput16A, POWER_TRANSFORMER,
                "substation_dynamo_hatch", GTCraftingComponents.WIRE_HEX, 6, 16, 400, true);
    }

    private static MachineDefinition[] addMissingPackHatches(MachineDefinition[] gtMachines,
                                                               MachineDefinition[] packMachines) {
        MachineDefinition[] merged = Arrays.copyOf(gtMachines, Math.max(gtMachines.length, packMachines.length));
        for (int tier = 0; tier < packMachines.length; tier++) {
            if (merged[tier] == null) {
                merged[tier] = packMachines[tier];
            }
        }
        return merged;
    }

    private static void addEnergyHatchRecipe(Consumer<FinishedRecipe> provider, int tier,
                                             MachineDefinition output) {
        ItemStack ceramic = ceramicPlate(tier);
        if (ceramic.isEmpty()) return;

        String name = VN[tier].toLowerCase(Locale.ROOT);
        GTRecipeBuilder builder = tier >= LuV
                ? ASSEMBLY_LINE_RECIPES.recipeBuilder(SuperFlatCore.id("energy_hatch_" + name))
                : ASSEMBLER_RECIPES.recipeBuilder(SuperFlatCore.id("energy_hatch_" + name));
        builder.inputItems(HULL[tier]);

        switch (tier) {
            case ULV -> builder.inputItems(cableGtSingle, RedAlloy, 4)
                    .inputItems(VOLTAGE_COIL_ULV);
            case LV -> builder.inputItems(cableGtSingle, Tin, 4)
                    .inputItems(VOLTAGE_COIL_LV);
            case MV -> builder.inputItems(cableGtSingle, Copper, 4)
                    .inputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                    .inputItems(VOLTAGE_COIL_MV);
            case HV -> builder.inputItems(cableGtSingle, Gold, 4)
                    .inputItems(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(VOLTAGE_COIL_HV)
                    .inputFluids(SodiumPotassium, 1000)
                    .cleanroom(CleanroomType.CLEANROOM);
            case EV -> builder.inputItems(cableGtSingle, Aluminium, 4)
                    .inputItems(POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(VOLTAGE_COIL_EV)
                    .inputFluids(SodiumPotassium, 2000);
            case IV -> builder.inputItems(cableGtSingle, Tungsten, 4)
                    .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(VOLTAGE_COIL_IV)
                    .inputFluids(SodiumPotassium, 3000);
            case LuV -> builder.inputItems(cableGtSingle, NiobiumTitanium, 4)
                    .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.LuV_CIRCUITS)
                    .inputItems(VOLTAGE_COIL_LuV, 2)
                    .inputFluids(SodiumPotassium, 6000)
                    .inputFluids(SolderingAlloy, 5 * L)
                    .scannerResearch(ENERGY_INPUT_HATCH[IV].asStack());
            case ZPM -> builder.inputItems(cableGtSingle, VanadiumGallium, 4)
                    .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.ZPM_CIRCUITS)
                    .inputItems(VOLTAGE_COIL_ZPM, 2)
                    .inputFluids(SodiumPotassium, 8000)
                    .inputFluids(SolderingAlloy, 10 * L)
                    .stationResearch(b -> b.researchStack(ENERGY_INPUT_HATCH[LuV].asStack()).CWUt(8));
            case UV -> builder.inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                    .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.UV_CIRCUITS)
                    .inputItems(VOLTAGE_COIL_UV, 2)
                    .inputFluids(SodiumPotassium, 10000)
                    .inputFluids(SolderingAlloy, 20 * L)
                    .stationResearch(b -> b.researchStack(ENERGY_INPUT_HATCH[ZPM].asStack()).CWUt(64).EUt(VA[ZPM]));
            case UHV -> builder.inputItems(cableGtSingle, Europium, 4)
                    .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.UHV_CIRCUITS)
                    .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                    .inputFluids(SodiumPotassium, 12000)
                    .inputFluids(SolderingAlloy, 40 * L)
                    .stationResearch(b -> b.researchStack(ENERGY_INPUT_HATCH[UV].asStack()).CWUt(128).EUt(VA[UV]));
            default -> {
                return;
            }
        }

        builder.inputItems(ceramic)
                .outputItems(output)
                .duration(tier >= LuV ? 400 + (tier - LuV) * 200 : 200)
                .EUt(VA[tier])
                .addMaterialInfo(true)
                .save(provider);
    }

    private static void addDynamoHatchRecipe(Consumer<FinishedRecipe> provider, int tier,
                                              MachineDefinition output) {
        ItemStack ceramic = ceramicPlate(tier);
        if (ceramic.isEmpty()) return;

        String name = VN[tier].toLowerCase(Locale.ROOT);
        GTRecipeBuilder builder = tier >= LuV
                ? ASSEMBLY_LINE_RECIPES.recipeBuilder(SuperFlatCore.id("dynamo_hatch_" + name))
                : ASSEMBLER_RECIPES.recipeBuilder(SuperFlatCore.id("dynamo_hatch_" + name));
        builder.inputItems(HULL[tier]);

        switch (tier) {
            case ULV -> builder.inputItems(spring, Lead, 2).inputItems(VOLTAGE_COIL_ULV);
            case LV -> builder.inputItems(spring, Tin, 2).inputItems(VOLTAGE_COIL_LV);
            case MV -> builder.inputItems(spring, Copper, 2)
                    .inputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                    .inputItems(VOLTAGE_COIL_MV);
            case HV -> builder.inputItems(spring, Gold, 2)
                    .inputItems(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(VOLTAGE_COIL_HV)
                    .inputFluids(SodiumPotassium, 1000)
                    .cleanroom(CleanroomType.CLEANROOM);
            case EV -> builder.inputItems(spring, Aluminium, 2)
                    .inputItems(POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(VOLTAGE_COIL_EV)
                    .inputFluids(SodiumPotassium, 2000);
            case IV -> builder.inputItems(spring, Tungsten, 2)
                    .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(VOLTAGE_COIL_IV)
                    .inputFluids(SodiumPotassium, 3000);
            case LuV -> builder.inputItems(spring, NiobiumTitanium, 4)
                    .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.LuV_CIRCUITS)
                    .inputItems(VOLTAGE_COIL_LuV, 2)
                    .inputFluids(SodiumPotassium, 6000)
                    .inputFluids(SolderingAlloy, 5 * L);
            case ZPM -> builder.inputItems(spring, VanadiumGallium, 4)
                    .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.ZPM_CIRCUITS)
                    .inputItems(VOLTAGE_COIL_ZPM, 2)
                    .inputFluids(SodiumPotassium, 8000)
                    .inputFluids(SolderingAlloy, 10 * L)
                    .stationResearch(b -> b.researchStack(ENERGY_OUTPUT_HATCH[LuV].asStack()).CWUt(8));
            case UV -> builder.inputItems(spring, YttriumBariumCuprate, 4)
                    .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.UV_CIRCUITS)
                    .inputItems(VOLTAGE_COIL_UV, 2)
                    .inputFluids(SodiumPotassium, 10000)
                    .inputFluids(SolderingAlloy, 20 * L)
                    .stationResearch(b -> b.researchStack(ENERGY_OUTPUT_HATCH[ZPM].asStack()).CWUt(64).EUt(VA[ZPM]));
            case UHV -> builder.inputItems(spring, Europium, 4)
                    .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .inputItems(CustomTags.UHV_CIRCUITS)
                    .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                    .inputFluids(SodiumPotassium, 12000)
                    .inputFluids(SolderingAlloy, 40 * L)
                    .stationResearch(b -> b.researchStack(ENERGY_OUTPUT_HATCH[UV].asStack()).CWUt(128).EUt(VA[UV]));
            default -> {
                return;
            }
        }

        builder.inputItems(ceramic)
                .outputItems(output)
                .duration(tier >= LuV ? 400 + (tier - LuV) * 200 : 200)
                .EUt(VA[tier])
                .addMaterialInfo(true)
                .save(provider);
    }

    private static void addAmpHatchRecipes(Consumer<FinishedRecipe> provider, MachineDefinition[] outputs,
                                            MachineDefinition[] previous, MachineDefinition[] transformers,
                                            String idPrefix, CraftingComponent wireComponent, int plateCount,
                                            int ceramicCount, int duration, boolean dynamo) {
        int tiers = Math.min(VN.length, outputs.length);
        for (int tier = 0; tier < tiers; tier++) {
            MachineDefinition output = outputs[tier];
            if (output == null) continue;
            ItemStack ceramic = ceramicPlate(tier);
            if (ceramic.isEmpty()) continue;
            ceramic.setCount(ceramicCount);

            String name = VN[tier].toLowerCase(Locale.ROOT);
            GTRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder(SuperFlatCore.id(idPrefix + "_" + name));
            if (previous[tier] == null) continue;
            builder.inputItems(previous[tier]);

            if (transformers != null) {
                MachineDefinition transformer;
                if (tier == (com.gregtechceu.gtceu.api.GTCEuAPI.isHighTier() ? MAX : UHV)) {
                    transformer = transformers[tier - 1];
                } else {
                    transformer = transformers[tier];
                }
                if (transformer == null) continue;
                builder.inputItems(transformer);
            }

            builder.inputItems(wireComponent.get(tier), 4)
                    .inputItems(GTCraftingComponents.PLATE.get(tier), plateCount)
                    .inputItems(ceramic)
                    .outputItems(output)
                    .duration(duration)
                    .EUt(dynamo && idPrefix.contains("4a") ? VA[Math.max(0, tier - 1)] : VA[tier])
                    .addMaterialInfo(true);
            if (tier == HV) {
                builder.cleanroom(CleanroomType.CLEANROOM);
            }
            builder.save(provider);
        }
    }

    /**
     * Replace GTMThings' wireless 4A/16A hatch recipes and add the pack's 64A
     * wireless hatches. These recipes intentionally remain assembler recipes.
     */
    private static void addWirelessEnergyHatchRecipes(Consumer<FinishedRecipe> provider) {
        addWirelessReceiver4ARecipes(provider);

        MachineDefinition[] energyInput4A = addMissingPackHatches(ENERGY_INPUT_HATCH_4A,
                SFMachines.ENERGY_INPUT_HATCH_4A);
        MachineDefinition[] energyOutput4A = addMissingPackHatches(ENERGY_OUTPUT_HATCH_4A,
                SFMachines.ENERGY_OUTPUT_HATCH_4A);
        MachineDefinition[] energyInput16A = addMissingPackHatches(ENERGY_INPUT_HATCH_16A,
                SFMachines.ENERGY_INPUT_HATCH_16A);
        MachineDefinition[] energyOutput16A = addMissingPackHatches(ENERGY_OUTPUT_HATCH_16A,
                SFMachines.ENERGY_OUTPUT_HATCH_16A);

        int maxTier = com.gregtechceu.gtceu.api.GTCEuAPI.isHighTier() ? OpV : UV;
        for (int tier : tiersBetween(LV, maxTier)) {
            addWirelessHatchRecipe(provider, tier, energyInput4A, WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4A,
                    1, "wireless_energy_input_hatch", "_4a");
            addWirelessHatchRecipe(provider, tier, energyOutput4A, WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4A,
                    1, "wireless_energy_output_hatch", "_4a");
            addWirelessHatchRecipe(provider, tier, energyInput16A, WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_16A,
                    4, "wireless_energy_input_hatch", "_16a");
            addWirelessHatchRecipe(provider, tier, energyOutput16A, WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_16A,
                    4, "wireless_energy_output_hatch", "_16a");
        }

        for (int tier : tiersBetween(HV, maxTier)) {
            addWirelessHatchRecipe(provider, tier, SFMachines.SUBSTATION_ENERGY_INPUT_HATCH,
                    SFMachines.WIRELESS_ENERGY_INPUT_HATCH_64A, 16,
                    "wireless_energy_input_hatch", "_64a", SuperFlatCore.MODID);
            addWirelessHatchRecipe(provider, tier, SFMachines.SUBSTATION_ENERGY_OUTPUT_HATCH,
                    SFMachines.WIRELESS_ENERGY_OUTPUT_HATCH_64A, 16,
                    "wireless_energy_output_hatch", "_64a", SuperFlatCore.MODID);
        }
    }

    /**
     * Replace the stock single-block electrolyzer recipes with the pack layout:
     * tiered glass and an electrolytic core on the top row, double wire on the
     * middle sides, and the original circuit/cable row at the bottom.
     */
    private static void addElectrolyzerRecipes(Consumer<FinishedRecipe> provider) {
        for (MachineDefinition machine : ELECTROLYZER) {
            if (machine == null) {
                continue;
            }
            int tier = machine.getTier();
            Object glass = GTCraftingComponents.GLASS.get(tier);
            Object circuit = GTCraftingComponents.CIRCUIT.get(tier);
            Object cable = GTCraftingComponents.CABLE.get(tier);
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    SuperFlatCore.id("electrolyzer_" + VN[tier].toLowerCase(Locale.ROOT)),
                    machine.asStack(),
                    "GEG", "IMI", "CWC",
                    'G', glass,
                    'C', circuit,
                    'W', cable,
                    'M', HULL[tier].asStack(),
                    'I', electrolyzerDoubleWire(tier),
                    'E', MekanismItems.ELECTROLYTIC_CORE.getItemStack());
        }
    }

    private static MaterialEntry electrolyzerDoubleWire(int tier) {
        Material material = switch (tier) {
            case ULV, LV -> Gold;
            case MV -> Silver;
            case HV -> Electrum;
            case EV -> Platinum;
            case IV, LuV, ZPM, UV, UHV -> Osmium;
            default -> Gold;
        };
        return new MaterialEntry(wireGtDouble, material);
    }

    private static void addWirelessHatchRecipe(Consumer<FinishedRecipe> provider, int tier,
                                                MachineDefinition[] wiredHatches,
                                                MachineDefinition[] wirelessHatches,
                                                int receiverCount, String idPrefix, String amperageSuffix) {
        addWirelessHatchRecipe(provider, tier, wiredHatches, wirelessHatches, receiverCount,
                idPrefix, amperageSuffix, SuperFlatCore.MODID);
    }

    private static void addWirelessHatchRecipe(Consumer<FinishedRecipe> provider, int tier,
                                                MachineDefinition[] wiredHatches,
                                                MachineDefinition[] wirelessHatches,
                                                int receiverCount, String idPrefix, String amperageSuffix,
                                                String namespace) {
        if (tier >= wiredHatches.length || tier >= wirelessHatches.length
                || wiredHatches[tier] == null || wirelessHatches[tier] == null) {
            return;
        }
        ItemStack receiver = wirelessReceiveCover4A(tier);
        if (receiver.isEmpty()) {
            return;
        }
        receiver.setCount(receiverCount);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(namespace,
                        idPrefix + "_" + VN[tier].toLowerCase(Locale.ROOT) + amperageSuffix))
                .inputItems(wiredHatches[tier])
                .inputItems(receiver)
                .inputItems(COVER_ENERGY_DETECTOR_ADVANCED)
                .inputFluids(SolderingAlloy, 144)
                .outputItems(wirelessHatches[tier])
                .duration(200)
                .EUt(VA[tier])
                .save(provider);
    }

    private static void addWirelessReceiver4ARecipes(Consumer<FinishedRecipe> provider) {
        int maxTier = com.gregtechceu.gtceu.api.GTCEuAPI.isHighTier() ? OpV : UV;
        for (int tier : tiersBetween(LV, maxTier)) {
            ItemStack oneAmp = wirelessReceiveCover(tier);
            ItemStack fourAmp = wirelessReceiveCover4A(tier);
            if (oneAmp.isEmpty() || fourAmp.isEmpty()) {
                continue;
            }
            GTRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(SuperFlatCore.MODID,
                    "wireless_energy_receive_cover_" + VN[tier].toLowerCase(Locale.ROOT) + "_4a"));
            builder.inputItems(oneAmp.copyWithCount(4));
            switch (tier) {
                case LV -> builder.inputItems(INDUCTOR, 4)
                        .inputItems(cableGtQuadruple, Tin, 4)
                        .inputItems(VOLTAGE_COIL_LV, 2);
                case MV -> builder.inputItems(INDUCTOR, 8)
                        .inputItems(cableGtQuadruple, Copper, 4)
                        .inputItems(VOLTAGE_COIL_MV, 2);
                case HV -> builder.inputItems(SMD_INDUCTOR, 4)
                        .inputItems(cableGtQuadruple, Gold, 4)
                        .inputItems(VOLTAGE_COIL_HV, 2);
                case EV -> builder.inputItems(SMD_INDUCTOR, 8)
                        .inputItems(cableGtQuadruple, Aluminium, 4)
                        .inputItems(VOLTAGE_COIL_EV, 2);
                case IV -> builder.inputItems(ADVANCED_SMD_INDUCTOR, 4)
                        .inputItems(cableGtQuadruple, Platinum, 4)
                        .inputItems(VOLTAGE_COIL_IV, 2);
                case LuV -> builder.inputItems(ADVANCED_SMD_INDUCTOR, 8)
                        .inputItems(cableGtQuadruple, NiobiumTitanium, 4)
                        .inputItems(VOLTAGE_COIL_LuV, 2);
                case ZPM -> builder.inputItems(ADVANCED_SMD_INDUCTOR, 16)
                        .inputItems(cableGtQuadruple, VanadiumGallium, 4)
                        .inputItems(VOLTAGE_COIL_ZPM, 2);
                case UV -> builder.inputItems(ADVANCED_SMD_INDUCTOR, 32)
                        .inputItems(cableGtQuadruple, YttriumBariumCuprate, 4)
                        .inputItems(VOLTAGE_COIL_UV, 2);
                case UHV -> builder.inputItems(ADVANCED_SMD_INDUCTOR, 64)
                        .inputItems(cableGtQuadruple, Trinium, 4)
                        .inputItems(VOLTAGE_COIL_UV, 2);
                default -> {
                    continue;
                }
            }
            builder.inputItems(plateDouble, BatteryAlloy, 2)
                    .inputFluids(SolderingAlloy, 144)
                    .outputItems(fourAmp)
                    .duration(200)
                    .EUt(VA[tier])
                    .save(provider);
        }
    }

    private static ItemStack wirelessReceiveCover(int tier) {
        return switch (tier) {
            case LV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV.asStack();
            case MV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV.asStack();
            case HV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV.asStack();
            case EV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV.asStack();
            case IV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV.asStack();
            case LuV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV.asStack();
            case ZPM -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM.asStack();
            case UV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV.asStack();
            case UHV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV.asStack();
            case UEV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV.asStack();
            case UIV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV.asStack();
            case UXV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV.asStack();
            case OpV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV.asStack();
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack wirelessReceiveCover4A(int tier) {
        return switch (tier) {
            case LV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A.asStack();
            case MV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV_4A.asStack();
            case HV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV_4A.asStack();
            case EV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV_4A.asStack();
            case IV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV_4A.asStack();
            case LuV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A.asStack();
            case ZPM -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A.asStack();
            case UV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV_4A.asStack();
            case UHV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A.asStack();
            case UEV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A.asStack();
            case UIV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A.asStack();
            case UXV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A.asStack();
            case OpV -> CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A.asStack();
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack ceramicPlate(int tier) {
        String name;
        if (tier <= LV) {
            name = "glass_plate";
        } else if (tier == MV) {
            name = "alumina_ceramic_plate";
        } else if (tier <= EV) {
            name = "titanium_nitride_ceramic_plate";
        } else {
            name = "tungsten_tetraboride_ceramic_plate";
        }
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("gtceu", name));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, 2);
    }

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {
        for (String name : new String[]{"ulv", "lv", "mv"}) {
            consumer.accept(GTCEu.id("dynamo_hatch_" + name));
            consumer.accept(GTCEu.id("energy_hatch_" + name));
        }

        removeTieredRecipes(consumer, ENERGY_INPUT_HATCH.length, "energy_hatch");
        removeTieredRecipes(consumer, ENERGY_OUTPUT_HATCH.length, "dynamo_hatch");
        removeTieredRecipes(consumer, ENERGY_INPUT_HATCH_4A.length, "energy_hatch_4a");
        removeTieredRecipes(consumer, ENERGY_OUTPUT_HATCH_4A.length, "dynamo_hatch_4a");
        removeTieredRecipes(consumer, ENERGY_INPUT_HATCH_16A.length, "energy_hatch_16a");
        removeTieredRecipes(consumer, ENERGY_OUTPUT_HATCH_16A.length, "dynamo_hatch_16a");
        removeTieredRecipes(consumer, SUBSTATION_ENERGY_INPUT_HATCH.length, "substation_energy_hatch");
        removeTieredRecipes(consumer, SUBSTATION_ENERGY_OUTPUT_HATCH.length, "substation_dynamo_hatch");

        for (MachineDefinition machine : ELECTROLYZER) {
            if (machine != null) {
                // GTCEu writes the original shaped machine recipe under the
                // `shaped/` recipe folder. Keep the legacy bare ID as well so
                // this removal remains safe across GTCEu data-pack versions.
                consumer.accept(GTCEu.id("shaped/" + machine.getName()));
                consumer.accept(GTCEu.id(machine.getName()));
            }
        }

        int maxTier = com.gregtechceu.gtceu.api.GTCEuAPI.isHighTier() ? OpV : UV;
        for (int tier : tiersBetween(LV, maxTier)) {
            String name = VN[tier].toLowerCase(Locale.ROOT);
            removeWirelessRecipe(consumer, "wireless_energy_input_hatch_" + name + "_4a");
            removeWirelessRecipe(consumer, "wireless_energy_output_hatch_" + name + "_4a");
            removeWirelessRecipe(consumer, "wireless_energy_input_hatch_" + name + "_16a");
            removeWirelessRecipe(consumer, "wireless_energy_output_hatch_" + name + "_16a");
        }
        int receiverMaxTier = com.gregtechceu.gtceu.api.GTCEuAPI.isHighTier() ? OpV : UV;
        for (int tier : tiersBetween(LV, receiverMaxTier)) {
            removeWirelessRecipe(consumer, "wireless_energy_receive_cover_" + VN[tier].toLowerCase(Locale.ROOT) + "_4a");
        }
    }

    private static void removeWirelessRecipe(Consumer<ResourceLocation> consumer, String path) {
        removeWirelessRecipe(consumer, path, "gtmthings");
    }

    private static void removeWirelessRecipe(Consumer<ResourceLocation> consumer, String path, String namespace) {
        consumer.accept(new ResourceLocation(namespace, "assembler/" + path));
    }

    private static void removeTieredRecipes(Consumer<ResourceLocation> consumer, int length, String prefix) {
        for (int tier = 0; tier < Math.min(length, VN.length); tier++) {
            String name = VN[tier].toLowerCase(Locale.ROOT);
            String recipeType = prefix.startsWith("substation") || prefix.contains("4a") || prefix.contains("16a")
                    ? "assembler/" : tier >= LuV ? "assembly_line/" : "assembler/";
            consumer.accept(GTCEu.id(recipeType + prefix + "_" + name));
        }
    }
}
