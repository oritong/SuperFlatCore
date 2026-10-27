package com.oritong.superflatcore.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFToolMachines;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

/** Redirects the generated recipes, keeping GT's material checks, counts and output NBT. */
public final class ToolAssemblyRecipes {
    public static final String CHARGE_INGREDIENT = "superflatcore:tool_charge_ingredient";
    public static final String UPGRADE_INGREDIENT = "superflatcore:tool_upgrade_ingredient";

    private ToolAssemblyRecipes() {}

    public static Consumer<FinishedRecipe> wrap(Consumer<FinishedRecipe> provider) {
        return provider instanceof ToolRecipeConsumer ? provider : new ToolRecipeConsumer(provider);
    }

    public static void registerMachineRecipes(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, SFToolMachines.TOOL_WELDING_ASSEMBLER,
                "ABC", "DED", "FGF",
                'A', CONVEYOR, 'B', ROBOT_ARM, 'C', PUMP,
                'D', EMITTER, 'E', HULL, 'F', CABLE,
                'G', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Steel));
    }

    private record ToolRecipeConsumer(Consumer<FinishedRecipe> provider) implements Consumer<FinishedRecipe> {
        @Override
        public void accept(FinishedRecipe source) {
            JsonObject json = new JsonObject();
            source.serializeRecipeData(json);
            if (!json.has("result") || !json.get("result").isJsonObject()) {
                provider.accept(source);
                return;
            }
            ItemStack result = CraftingHelper.getItemStack(json.getAsJsonObject("result"), true);
            if (!(result.getItem() instanceof IGTTool tool)) {
                provider.accept(source);
                return;
            }
            boolean shaped = json.has("pattern") && json.has("key");
            boolean upgrade = json.has("base") && json.has("addition") && json.has("template");
            if (!shaped && !upgrade) {
                provider.accept(source);
                return;
            }

            int tier = tool.isElectric() ? tool.getElectricTier() : GTValues.ULV;
            int harvestLevel = tool.getTotalHarvestLevel(result);
            ResourceLocation recipeId = SuperFlatCore.id(
                    source.getId().getNamespace() + "/" + source.getId().getPath());
            GTRecipeBuilder builder = SFToolRecipeTypes.TOOL_WELDING_ASSEMBLER_RECIPES
                    .recipeBuilder(recipeId)
                    .outputItems(result)
                    // GT stores integer EU/t, so round 80% of the tool tier's nominal voltage.
                    .EUt(Math.round(GTValues.V[tier] * 0.8))
                    .duration(100);

            if (shaped) {
                addShapedInputs(builder, json);
            } else {
                // A consumed tool being upgraded is an input, not a crafting-tool catalyst.
                builder.inputItems(Ingredient.fromJson(json.get("base")), 1)
                        .inputItems(Ingredient.fromJson(json.get("addition")), 1)
                        .inputItems(Ingredient.fromJson(json.get("template")), 1)
                        .addData(UPGRADE_INGREDIENT, json.get("base").toString());
            }
            if (json.has("chargeIngredient")) {
                builder.addData(CHARGE_INGREDIENT, json.get("chargeIngredient").toString());
            }
            if (!tool.isElectric()) {
                builder.circuitMeta(manualCircuit(tool.getToolType()));
            } else {
                builder.inputFluids(GTMaterials.Polyethylene.getFluid(288));
            }
            // Save to the original provider so converted recipes are never wrapped a second time.
            if (harvestLevel >= Tiers.DIAMOND.getLevel()) {
                builder.copy(recipeId.withSuffix("_tin"))
                        .inputFluids(GTMaterials.Tin.getFluid(576))
                        .save(provider);
                builder.copy(recipeId.withSuffix("_soldering_alloy"))
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                        .save(provider);
            } else {
                builder.save(provider);
            }
        }
    }

    private static void addShapedInputs(GTRecipeBuilder builder, JsonObject json) {
        ToolAssemblyPattern.materialCounts(json, symbol -> ToolHelper.getToolSymbols().contains((char) symbol))
                .forEach((ingredient, count) -> builder.inputItems(Ingredient.fromJson(ingredient), count));
    }

    private static int manualCircuit(GTToolType type) {
        // Stable across material iteration order; avoids sword/file, axe/hoe, etc. collisions.
        List<GTToolType> types = GTToolType.getTypes().values().stream()
                .filter(tool -> tool.electricTier < 0)
                .sorted(Comparator.comparing(tool -> tool.name))
                .toList();
        int circuit = types.indexOf(type) + 1;
        if (circuit < 1 || circuit > 32) {
            throw new IllegalStateException("No tool assembly circuit for " + type.name);
        }
        return circuit;
    }
}
