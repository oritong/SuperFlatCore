package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Consumer;

public final class SFMineralProcessingRecipes {
    private static final List<IsaMillOre> ISA_MILL_ORES = List.of(
            new IsaMillOre("almandine", "raw_almandine"),
            new IsaMillOre("chalcopyrite", "raw_chalcopyrite"),
            new IsaMillOre("grossular", "raw_grossular"),
            new IsaMillOre("monazite", "raw_monazite"),
            new IsaMillOre("nickel", "raw_nickel"),
            new IsaMillOre("pentlandite", "raw_pentlandite"),
            new IsaMillOre("platinum", "raw_platinum"),
            new IsaMillOre("pyrope", "raw_pyrope"),
            new IsaMillOre("redstone", "raw_redstone"),
            new IsaMillOre("spessartine", "raw_spessartine"),
            new IsaMillOre("sphalerite", "raw_sphalerite"),
            new IsaMillOre("enriched_naquadah", "raw_enriched_naquadah"));

    private static final List<FlotationRecipe> FLOTATION_RECIPES = List.of(
            new FlotationRecipe("almandine", "sodium_ethylxanthate", 32, 13_500, GTValues.IV),
            new FlotationRecipe("chalcopyrite", "potassium_ethylxanthate", 32, 9_000, GTValues.IV),
            new FlotationRecipe("grossular", "potassium_ethylxanthate", 32, 21_000, GTValues.LuV),
            new FlotationRecipe("monazite", "sodium_ethylxanthate", 32, 22_500, GTValues.LuV),
            new FlotationRecipe("nickel", "potassium_ethylxanthate", 32, 18_750, GTValues.IV),
            new FlotationRecipe("platinum", "sodium_ethylxanthate", 32, 26_250, GTValues.LuV),
            new FlotationRecipe("pyrope", "sodium_ethylxanthate", 32, 6_000, GTValues.IV),
            new FlotationRecipe("redstone", "sodium_ethylxanthate", 32, 9_750, GTValues.IV),
            new FlotationRecipe("spessartine", "potassium_ethylxanthate", 32, 26_250, GTValues.LuV),
            new FlotationRecipe("sphalerite", "sodium_ethylxanthate", 32, 10_500, GTValues.LuV),
            new FlotationRecipe("pentlandite", "potassium_ethylxanthate", 32, 10_500, GTValues.LuV),
            new FlotationRecipe("enriched_naquadah", "potassium_ethylxanthate", 64, 105_000, GTValues.LuV, 7_200));

    private static final List<DehydratorRecipe> DEHYDRATOR_RECIPES = List.of(
            new DehydratorRecipe("almandine", GTValues.IV, outputs(
                    out("aluminium_dust", 64), out("aluminium_dust", 64), out("manganese_dust", 64),
                    out("manganese_dust", 24), out("yttrium_dust", 24), out("ytterbium_dust", 16))),
            new DehydratorRecipe("chalcopyrite", GTValues.IV, outputs(
                    out("copper_dust", 64), out("copper_dust", 64), out("iron_dust", 64),
                    out("iron_dust", 48), out("cadmium_dust", 48), out("indium_dust", 32))),
            new DehydratorRecipe("grossular", GTValues.LuV, outputs(
                    out("calcium_dust", 64), out("calcium_dust", 64), out("aluminium_dust", 64),
                    out("aluminium_dust", 64), out("tungsten_dust", 64), out("thallium_dust", 16))),
            new DehydratorRecipe("monazite", GTValues.ZPM, outputs(
                    out("erbium_dust", 64), out("neodymium_dust", 64), out("thorium_dust", 48),
                    out("lanthanum_dust", 32), out("lutetium_dust", 16), out("europium_dust", 8))),
            new DehydratorRecipe("nickel", GTValues.IV, outputs(
                    out("nickel_dust", 64), out("nickel_dust", 64), out("cobalt_dust", 64),
                    out("cobalt_dust", 64), out("iron_dust", 32), out("rhodium_dust", 32))),
            new DehydratorRecipe("platinum", GTValues.LuV, outputs(
                    out("platinum_dust", 64), out("nickel_dust", 48), out("iridium_dust", 32),
                    out("osmium_dust", 32), out("palladium_dust", 32), out("cobalt_dust", 32))),
            new DehydratorRecipe("pyrope", GTValues.EV, outputs(
                    out("magnesium_dust", 64), out("magnesium_dust", 64), out("aluminium_dust", 64),
                    out("manganese_dust", 64), out("boron_dust", 64), out("silicon_dust", 48))),
            new DehydratorRecipe("redstone", GTValues.IV, outputs(
                    out("minecraft:redstone", 64), out("minecraft:redstone", 64), out("manganese_dust", 64),
                    out("manganese_dust", 64), out("yttrium_dust", 32), out("ytterbium_dust", 16))),
            new DehydratorRecipe("spessartine", GTValues.LuV, outputs(
                    out("manganese_dust", 64), out("manganese_dust", 64), out("aluminium_dust", 64),
                    out("aluminium_dust", 32), out("palladium_dust", 32), out("strontium_dust", 16))),
            new DehydratorRecipe("sphalerite", GTValues.LuV, outputs(
                    out("zinc_dust", 64), out("zinc_dust", 64), out("iron_dust", 64),
                    out("iron_dust", 32), out("indium_dust", 64), out("gallium_dust", 64))),
            new DehydratorRecipe("pentlandite", GTValues.LuV, outputs(
                    out("iron_dust", 64), out("iron_dust", 64), out("nickel_dust", 64),
                    out("nickel_dust", 64), out("bismuth_dust", 64), out("ruthenium_dust", 48))),
            new DehydratorRecipe("enriched_naquadah", GTValues.ZPM, outputs(
                    out("enriched_naquadah_dust", 64), out("enriched_naquadah_dust", 64),
                    out("naquadah_dust", 64), out("naquadah_dust", 32), out("naquadria_dust", 64),
                    out("trinium_dust", 32))));

    private SFMineralProcessingRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        addXanthateRecipes(provider);
        addTurpentineRecipes(provider);
        addIsaMillRecipes(provider, requireRecipeType("isa_mill"));
        addFlotationRecipes(provider, requireRecipeType("flotation_cell_regulator"));
        addDehydratorRecipes(provider, requireRecipeType("dehydrator"));
    }

    private static void addIsaMillRecipes(Consumer<FinishedRecipe> provider, GTRecipeType recipeType) {
        for (IsaMillOre ore : ISA_MILL_ORES) {
            addIsaMillRecipe(provider, recipeType, ore, "raw_soapstone", Ingredient.of(item("gtceu:" + ore.rawItem)),
                    50, 48, 2_400, 1, "kubejs:soapstone_grinding_ball", 200);
            addIsaMillRecipe(provider, recipeType, ore, "raw_aluminium", Ingredient.of(item("gtceu:" + ore.rawItem)),
                    50, 36, 1_200, 2, "kubejs:aluminium_grinding_ball", 100);
            addIsaMillRecipe(provider, recipeType, ore, "ore_soapstone", oreTag(ore.name),
                    100, 96, 4_800, 1, "kubejs:soapstone_grinding_ball", 200);
            addIsaMillRecipe(provider, recipeType, ore, "ore_aluminium", oreTag(ore.name),
                    100, 72, 2_400, 2, "kubejs:aluminium_grinding_ball", 100);
        }
    }

    private static void addIsaMillRecipe(Consumer<FinishedRecipe> provider, GTRecipeType recipeType,
                                         IsaMillOre ore, String variant, Ingredient input,
                                         int distilledWater, int outputAmount, int duration, int grindball,
                                         String grindballItem, int grindballChance) {
        recipeType.recipeBuilder(SuperFlatCore.id("isa_mill/milled_" + ore.name + "_" + variant))
                .chancedInput(Ingredient.of(item(grindballItem)), grindballChance, 0)
                .inputItems(input, 16)
                .inputFluids(fluidStack("gtceu:distilled_water", distilledWater))
                .outputItems(stack("gtceu:milled_" + ore.name, outputAmount))
                .EUt(GTValues.VA[GTValues.EV])
                .duration(duration)
                .addData("grindball", grindball)
                .save(provider);
    }

    private static void addXanthateRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(SuperFlatCore.id("mixer/sodium_ethylate"))
                .inputItems(stack("gtceu:sodium_dust", 1))
                .inputFluids(fluidStack("gtceu:ethanol", 1_000))
                .outputItems(stack("gtceu:sodium_ethylate_dust", 9))
                .outputFluids(fluidStack("gtceu:hydrogen", 1_000))
                .EUt(GTValues.VA[GTValues.MV])
                .duration(100)
                .save(provider);

        GTRecipeTypes.LARGE_CHEMICAL_RECIPES.recipeBuilder(SuperFlatCore.id("large_chemical_reactor/potassium_ethylate"))
                .circuitMeta(2)
                .inputItems(stack("gtceu:potash_dust", 3), stack("gtceu:quicklime_dust", 5))
                .inputFluids(fluidStack("gtceu:carbon_dioxide", 1_000), fluidStack("minecraft:water", 5_000))
                .outputItems(stack("gtceu:potassium_ethylate_dust", 6),
                        stack("gtceu:high_purity_calcium_carbonate_dust", 5))
                .EUt(GTValues.VA[GTValues.MV])
                .duration(100)
                .save(provider);

        GCYMRecipeTypes.ALLOY_BLAST_RECIPES.recipeBuilder(SuperFlatCore.id("alloy_blast_smelter/carbon_disulfide"))
                .circuitMeta(8)
                .inputItems(stack("gtceu:carbon_dust", 1), stack("gtceu:sulfur_dust", 2))
                .outputFluids(fluidStack("gtceu:carbon_disulfide", 1_000))
                .EUt(GTValues.VA[GTValues.MV])
                .duration(350)
                .blastFurnaceTemp(1_200)
                .save(provider);

        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(SuperFlatCore.id("chemical_reactor/sodium_ethylxanthate"))
                .inputItems(stack("gtceu:sodium_ethylate_dust", 9))
                .inputFluids(fluidStack("gtceu:ethanol", 1_000), fluidStack("gtceu:carbon_disulfide", 1_000))
                .outputItems(stack("gtceu:sodium_ethylxanthate_dust", 12))
                .outputFluids(fluidStack("minecraft:water", 1_000))
                .EUt(GTValues.VA[GTValues.HV])
                .duration(40)
                .save(provider);

        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(SuperFlatCore.id("chemical_reactor/potassium_ethylxanthate"))
                .inputItems(stack("gtceu:potassium_ethylate_dust", 3))
                .inputFluids(fluidStack("gtceu:carbon_disulfide", 1_000))
                .outputItems(stack("gtceu:potassium_ethylxanthate_dust", 12))
                .EUt(GTValues.VA[GTValues.HV])
                .duration(40)
                .save(provider);
    }

    private static void addTurpentineRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder(SuperFlatCore.id("leached_turpentine"))
                .inputItems(stack("embers:ember_grit", 2))
                .inputFluids(fluidStack("gtceu:naphtha", 1_000))
                .outputFluids(fluidStack("gtceu:leached_turpentine", 1_000))
                .EUt(GTValues.VA[GTValues.HV])
                .duration(80)
                .save(provider);

        GTRecipeTypes.CRACKING_RECIPES.recipeBuilder(SuperFlatCore.id("cracker/steam_cracked_turpentine"))
                .circuitMeta(1)
                .inputFluids(fluidStack("gtceu:leached_turpentine", 1_000), fluidStack("gtceu:steam", 1_000))
                .outputFluids(fluidStack("gtceu:steam_cracked_turpentine", 1_000))
                .EUt(GTValues.VA[GTValues.HV])
                .duration(200)
                .save(provider);

        GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder(SuperFlatCore.id("distillation/turpentine"))
                .inputFluids(fluidStack("gtceu:steam_cracked_turpentine", 1_000))
                .outputFluids(fluidStack("gtceu:turpentine", 1_000), fluidStack("gtceu:naphtha", 900))
                .EUt(GTValues.VA[GTValues.EV])
                .duration(400)
                .save(provider);
    }

    private static void addFlotationRecipes(Consumer<FinishedRecipe> provider, GTRecipeType recipeType) {
        for (FlotationRecipe recipe : FLOTATION_RECIPES) {
            int duration = recipe.duration;
            recipeType.recipeBuilder(SuperFlatCore.id("flotation_cell_regulator/" + recipe.ore + "_front"))
                    .inputItems(stack("gtceu:" + recipe.xanthate + "_dust", recipe.xanthateAmount))
                    .inputItems(stack("gtceu:milled_" + recipe.ore, 64))
                    .inputFluids(fluidStack("gtceu:turpentine", recipe.turpentine))
                    .outputFluids(fluidStack("gtceu:" + recipe.ore + "_front", 1_000))
                    .EUt(GTValues.VA[recipe.tier])
                    .duration(duration)
                    .save(provider);
        }
    }

    private static void addDehydratorRecipes(Consumer<FinishedRecipe> provider, GTRecipeType recipeType) {
        for (int index = 0; index < DEHYDRATOR_RECIPES.size(); index++) {
            DehydratorRecipe recipe = DEHYDRATOR_RECIPES.get(index);
            ItemStack[] outputs = recipe.outputs.stream()
                    .map(output -> stack(output.item, output.amount))
                    .toArray(ItemStack[]::new);

            recipeType.recipeBuilder(SuperFlatCore.id("dehydrator/" + recipe.ore + "_front"))
                    .circuitMeta(index + 1)
                    .inputFluids(fluidStack("gtceu:" + recipe.ore + "_front", 4_000))
                    .outputItems(outputs)
                    .outputFluids(fluidStack("gtceu:red_mud", 200))
                    .EUt(GTValues.VA[recipe.tier])
                    .duration(2_400)
                    .save(provider);
        }
    }

    private static GTRecipeType requireRecipeType(String name) {
        ResourceLocation id = GTCEu.id(name);
        RecipeType<?> recipeType = ForgeRegistries.RECIPE_TYPES.getValue(id);
        if (recipeType instanceof GTRecipeType gtRecipeType) {
            return gtRecipeType;
        }
        throw new IllegalStateException("Missing GTCEu recipe type " + id);
    }

    private static Ingredient oreTag(String ore) {
        return Ingredient.of(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/" + ore)));
    }

    static Item item(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(parseId(id));
        if (item == null || item == Items.AIR) {
            throw new IllegalStateException("Missing item " + id);
        }
        return item;
    }

    private static ItemStack stack(String id, int amount) {
        return new ItemStack(item(id), amount);
    }

    private static FluidStack fluidStack(String id, int amount) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(parseId(id));
        if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            throw new IllegalStateException("Missing fluid " + id);
        }
        return new FluidStack(fluid, amount);
    }

    private static ResourceLocation parseId(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid registry ID " + id);
        }
        return location;
    }

    private static List<ItemOutput> outputs(ItemOutput... outputs) {
        return List.of(outputs);
    }

    private static ItemOutput out(String item, int amount) {
        return new ItemOutput(item.startsWith("minecraft:") ? item : "gtceu:" + item, amount);
    }

    private record IsaMillOre(String name, String rawItem) {}

    private record FlotationRecipe(String ore, String xanthate, int xanthateAmount, int turpentine, int tier,
                                   int duration) {
        private FlotationRecipe(String ore, String xanthate, int xanthateAmount, int turpentine, int tier) {
            this(ore, xanthate, xanthateAmount, turpentine, tier, 3_600);
        }
    }

    private record ItemOutput(String item, int amount) {}

    private record DehydratorRecipe(String ore, int tier, List<ItemOutput> outputs) {}
}
