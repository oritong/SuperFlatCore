package com.oritong.superflatcore.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/** Runs without a game or extra test dependencies; fixtures follow GT 7.5.3's tool patterns. */
public final class ToolAssemblyPatternTest {
    public static void main(String[] args) {
        check("pickaxe", new String[]{"PII", "fSh", " S "},
                Map.of('P', "plate", 'I', "ingot", 'S', "stick"),
                Map.of("plate", 1, "ingot", 2, "stick", 2));
        check("hammer", new String[]{"II ", "IIS", "II "},
                Map.of('I', "ingot", 'S', "stick"), Map.of("ingot", 6, "stick", 1));
        check("wire cutter", new String[]{"PfP", "hPd", "STS"},
                Map.of('P', "plate", 'T', "screw", 'S', "rod"),
                Map.of("plate", 3, "screw", 1, "rod", 2));
        check("dyed crowbar", new String[]{"hDS", "DSD", "SDf"},
                Map.of('D', "dye", 'S', "rod"), Map.of("dye", 4, "rod", 3));
        check("electric tool", new String[]{"wHd", " U "},
                Map.of('H', "tool_head", 'U', "power_unit"), Map.of("tool_head", 1, "power_unit", 1));
        check("soft mallet", new String[]{"II ", "IIS", "II "},
                Map.of('I', "planks", 'S', "stick"), Map.of("planks", 6, "stick", 1));
        check("equivalent material keys", new String[]{"PII", " S "},
                Map.of('P', "same_material", 'I', "same_material", 'S', "stick"),
                Map.of("same_material", 3, "stick", 1));
        try {
            ToolAssemblyPattern.materialCounts(recipe(new String[]{"Z"}, Map.of()), symbol -> false);
            throw new AssertionError("Undefined material must fail rather than produce a cheaper recipe");
        } catch (IllegalArgumentException expected) {
            // Invalid source recipes must not silently lose materials.
        }
        JsonObject vanillaStick = new JsonObject();
        vanillaStick.addProperty("item", "minecraft:stick");
        JsonElement replacedStick = ToolAssemblyPattern.replaceVanillaStick(vanillaStick);
        if (!"gtceu:treated_wood_rod".equals(replacedStick.getAsJsonObject().get("item").getAsString())) {
            throw new AssertionError("Vanilla sticks must use GregTech treated wood rods");
        }
        System.out.println("Passed 8 tool assembly material-count checks and stick substitution check.");
    }

    private static void check(String name, String[] pattern, Map<Character, String> keys,
                              Map<String, Integer> expected) {
        Map<JsonElement, Integer> counts = ToolAssemblyPattern.materialCounts(recipe(pattern, keys),
                symbol -> "fhwd".indexOf(symbol) >= 0);
        if (counts.size() != expected.size()) {
            throw new AssertionError(name + ": unexpected inputs " + counts);
        }
        expected.forEach((ingredient, count) -> {
            JsonObject key = new JsonObject();
            key.addProperty("tag", "forge:" + ingredient);
            if (!count.equals(counts.get(key))) {
                throw new AssertionError(name + ": wrong count for " + ingredient + ": " + counts);
            }
        });
    }

    private static JsonObject recipe(String[] pattern, Map<Character, String> keys) {
        JsonObject recipe = new JsonObject();
        JsonArray lines = new JsonArray();
        for (String line : pattern) {
            lines.add(line);
        }
        JsonObject ingredients = new JsonObject();
        keys.forEach((symbol, material) -> {
            JsonObject key = new JsonObject();
            key.addProperty("tag", "forge:" + material);
            ingredients.add(symbol.toString(), key);
        });
        recipe.add("pattern", lines);
        recipe.add("key", ingredients);
        return recipe;
    }
}
