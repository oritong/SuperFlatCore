package com.oritong.superflatcore.recipe;

import java.util.Set;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/** Recipe and viewer category IDs are intentionally separate: JEI often uses machine IDs. */
public final class PackRecipeRestrictions {
    private static final Set<String> IFEU_TYPES = Set.of(
            "dragon_generator", "dragon_star_generator", "infuser", "structure");
    private static final Set<String> MEKANISM_TYPES = Set.of(
            "dissolution", "washing", "combining", "centrifuging", "compressing", "evaporating");
    private static final Set<String> MEKANISM_CATEGORIES = Set.of(
            "boiler_casing", "chemical_dissolution_chamber", "chemical_washer", "combiner",
            "isotopic_centrifuge", "nutritional_liquifier", "osmium_compressor", "sps_casing",
            "thermal_evaporation_controller");
    private static final Set<String> TINKERS_CATEGORIES = Set.of("modifiers", "tool_recipes", "worktable");
    private static final Set<String> INDUSTRIAL_OUTPUTS = Set.of(
            "industrialforegoing:meat", "industrialforegoing:pink_slime", "industrialforegoing:dryrubber");

    private PackRecipeRestrictions() {}

    public static boolean removesRecipeType(String namespace, String path) {
        return switch (namespace) {
            case "ironfurnaces" -> path.startsWith("generator_");
            case "ifeu" -> IFEU_TYPES.contains(path);
            case "industrialforegoing" -> "bioreactor".equals(path);
            case "mekanism" -> MEKANISM_TYPES.contains(path);
            case "tconstruct" -> "modifier_worktable".equals(path);
            default -> false;
        };
    }

    public static boolean hidesCategory(String namespace, String path) {
        return switch (namespace) {
            // EMI's JEI bridge uses the RecipeType UID, not the category's translation key.
            case "ironfurnaces" -> path.startsWith("generator_");
            case "ifeu" -> IFEU_TYPES.contains(path);
            case "industrialforegoing" -> "bioreactor".equals(path);
            case "mekanism" -> MEKANISM_CATEGORIES.contains(path);
            case "mekanismgenerators" -> "fission".equals(path);
            case "tconstruct" -> TINKERS_CATEGORIES.contains(path);
            default -> false;
        };
    }

    public static boolean keepsIndustrialOutput(String id) {
        return INDUSTRIAL_OUTPUTS.contains(id);
    }

    /**
     * Energy and dynamo hatches are intentionally machine-only recipes. GTCEu
     * injects its generated crafting recipes through a dynamic pack, so filter
     * them again after the final recipe map has been assembled.
     */
    public static boolean removesManualEnergyHatchRecipe(RecipeType<?> type, ResourceLocation id) {
        if (type != RecipeType.CRAFTING) {
            return false;
        }
        if (!id.getNamespace().equals("gtceu") && !id.getNamespace().equals("superflatcore")) {
            return false;
        }
        String path = id.getPath();
        return path.startsWith("energy_hatch_")
                || path.startsWith("dynamo_hatch_")
                || path.startsWith("substation_energy_hatch")
                || path.startsWith("substation_dynamo_hatch");
    }

    /**
     * Removes every crafting recipe that produces a GTCEu energy or dynamo hatch.
     *
     * <p>The pack also adds crafting recipes through KubeJS, whose recipe IDs use
     * the kubejs namespace. Filtering only the GTCEu recipe IDs therefore leaves
     * those manual recipes available. Machine recipes are unaffected because this
     * check is limited to the vanilla crafting recipe type.</p>
     */
    public static boolean removesManualEnergyHatchRecipe(
            RecipeType<?> type, ResourceLocation id, Recipe<?> recipe) {
        if (removesManualEnergyHatchRecipe(type, id)) {
            return true;
        }
        if (type != RecipeType.CRAFTING) {
            return false;
        }

        ItemStack result = recipe.getResultItem(
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        if (result.isEmpty()) {
            return false;
        }
        ResourceLocation outputId = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (outputId == null
                || (!outputId.getNamespace().equals("gtceu")
                && !outputId.getNamespace().equals("superflatcore"))) {
            return false;
        }

        String path = outputId.getPath();
        return path.matches("^(?:[a-z0-9]+_)?energy_(input|output)_hatch(?:_(4a|16a))?$")
                || path.matches("^(?:[a-z0-9]+_)?substation_(input|output)_hatch_64a$");
    }

    /** GTNN registers four tiers of heavy alloy plates with five explosive variants each. */
    public static boolean removesGTNNHeavyAlloyPlateRecipe(ResourceLocation id) {
        return "gtnn".equals(id.getNamespace())
                && id.getPath().matches("implode_heavy_plate_t[1-4]_(tnt|powderbarrel|dynamite|itnt|bomb)");
    }
}
