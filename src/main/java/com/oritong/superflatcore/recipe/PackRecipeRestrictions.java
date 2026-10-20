package com.oritong.superflatcore.recipe;

import java.util.Set;

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
            case "ironfurnaces" -> path.startsWith("category_generator_");
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
}
