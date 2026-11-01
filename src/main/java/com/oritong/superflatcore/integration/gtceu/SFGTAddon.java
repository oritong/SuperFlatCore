package com.oritong.superflatcore.integration.gtceu;

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
import com.oritong.superflatcore.registry.WindRotorPrefix;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
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
    }

    /**
     * Replace GTCEu's stock energy/dynamo hatch recipes with the pack variant.
     * Two ceramic plates are selected by the recipe tier; high tiers use
     * tungsten tetraboride ceramic until dedicated materials are introduced.
     */
    private static void addEnergyHatchRecipes(Consumer<FinishedRecipe> provider) {
        addDynamoShapedRecipes(provider);

        int baseTiers = Math.min(VN.length, Math.min(ENERGY_INPUT_HATCH.length, ENERGY_OUTPUT_HATCH.length));
        for (int tier = 0; tier < baseTiers; tier++) {
            if (ENERGY_INPUT_HATCH[tier] != null) {
                addEnergyHatchRecipe(provider, tier, ENERGY_INPUT_HATCH[tier]);
            }
            if (ENERGY_OUTPUT_HATCH[tier] != null) {
                addDynamoHatchRecipe(provider, tier, ENERGY_OUTPUT_HATCH[tier]);
            }
        }

        addAmpHatchRecipes(provider, ENERGY_INPUT_HATCH_4A, ENERGY_INPUT_HATCH, null,
                "energy_hatch_4a", GTCraftingComponents.WIRE_QUAD, 2, 100, false);
        addAmpHatchRecipes(provider, ENERGY_OUTPUT_HATCH_4A, ENERGY_OUTPUT_HATCH, null,
                "dynamo_hatch_4a", GTCraftingComponents.WIRE_QUAD, 2, 100, true);
        addAmpHatchRecipes(provider, ENERGY_INPUT_HATCH_16A, ENERGY_INPUT_HATCH_4A, TRANSFORMER,
                "energy_hatch_16a", GTCraftingComponents.WIRE_OCT, 4, 200, false);
        addAmpHatchRecipes(provider, ENERGY_OUTPUT_HATCH_16A, ENERGY_OUTPUT_HATCH_4A, TRANSFORMER,
                "dynamo_hatch_16a", GTCraftingComponents.WIRE_OCT, 4, 200, true);
        addAmpHatchRecipes(provider, SUBSTATION_ENERGY_INPUT_HATCH, ENERGY_INPUT_HATCH_16A, POWER_TRANSFORMER,
                "substation_energy_hatch", GTCraftingComponents.WIRE_HEX, 6, 400, false);
        addAmpHatchRecipes(provider, SUBSTATION_ENERGY_OUTPUT_HATCH, ENERGY_OUTPUT_HATCH_16A, POWER_TRANSFORMER,
                "substation_dynamo_hatch", GTCraftingComponents.WIRE_HEX, 6, 400, true);
    }

    private static void addDynamoShapedRecipes(Consumer<FinishedRecipe> provider) {
        ItemStack ulvPlate = ceramicPlate(ULV);
        ItemStack lvPlate = ceramicPlate(LV);
        ItemStack mvPlate = ceramicPlate(MV);
        if (ulvPlate.isEmpty() || lvPlate.isEmpty() || mvPlate.isEmpty()) {
            return;
        }

        VanillaRecipeHelper.addShapedRecipe(provider, true, "dynamo_hatch_ulv", ENERGY_OUTPUT_HATCH[ULV].asStack(),
                " V ", "SHS", "PP ",
                'S', new MaterialEntry(spring, Lead),
                'V', VOLTAGE_COIL_ULV.asStack(),
                'H', HULL[ULV].asStack(),
                'P', ulvPlate);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "dynamo_hatch_lv", ENERGY_OUTPUT_HATCH[LV].asStack(),
                " V ", "SHS", "PP ",
                'S', new MaterialEntry(spring, Tin),
                'V', VOLTAGE_COIL_LV.asStack(),
                'H', HULL[LV].asStack(),
                'P', lvPlate);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "dynamo_hatch_mv", ENERGY_OUTPUT_HATCH[MV].asStack(),
                " V ", "SHS", "PQP",
                'S', new MaterialEntry(spring, Copper),
                'V', VOLTAGE_COIL_MV.asStack(),
                'H', HULL[MV].asStack(),
                'P', mvPlate,
                'Q', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack());

    }

    private static void addEnergyHatchRecipe(Consumer<FinishedRecipe> provider, int tier,
                                             MachineDefinition output) {
        ItemStack ceramic = ceramicPlate(tier);
        if (ceramic.isEmpty()) return;

        String name = VN[tier].toLowerCase(Locale.ROOT);
        GTRecipeBuilder builder = tier >= LuV
                ? ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_hatch_" + name)
                : ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_" + name);
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
                ? ASSEMBLY_LINE_RECIPES.recipeBuilder("dynamo_hatch_" + name)
                : ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_" + name);
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
                    .inputFluids(SodiumPotassium, 1000);
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
                                            int duration, boolean dynamo) {
        int tiers = Math.min(VN.length, outputs.length);
        for (int tier = 0; tier < tiers; tier++) {
            MachineDefinition output = outputs[tier];
            if (output == null) continue;
            ItemStack ceramic = ceramicPlate(tier);
            if (ceramic.isEmpty()) continue;

            String name = VN[tier].toLowerCase(Locale.ROOT);
            GTRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder(idPrefix + "_" + name);
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
                    .addMaterialInfo(true)
                    .save(provider);
        }
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
