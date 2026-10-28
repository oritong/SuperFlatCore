package com.oritong.superflatcore.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntPredicate;

/** Counts consumed materials in a shaped recipe, excluding crafting-tool symbols. */
final class ToolAssemblyPattern {
    private ToolAssemblyPattern() {}

    static Map<JsonElement, Integer> materialCounts(JsonObject json, IntPredicate isTool) {
        JsonObject keys = json.getAsJsonObject("key");
        Map<JsonElement, Integer> counts = new LinkedHashMap<>();
        for (JsonElement line : json.getAsJsonArray("pattern")) {
            for (char symbol : line.getAsString().toCharArray()) {
                if (symbol == ' ' || isTool.test(symbol)) {
                    continue;
                }
                JsonElement ingredient = replaceVanillaStick(keys.get(String.valueOf(symbol)));
                if (ingredient == null) {
                    throw new IllegalArgumentException("Undefined tool recipe symbol: " + symbol);
                }
                counts.merge(ingredient, 1, Integer::sum);
            }
        }
        return counts;
    }

    /** Uses GregTech's treated wood rod wherever a generated tool recipe asks for a vanilla stick. */
    static JsonElement replaceVanillaStick(JsonElement ingredient) {
        if (ingredient == null) {
            return null;
        }
        if (ingredient.isJsonPrimitive() && ingredient.getAsJsonPrimitive().isString()
                && "minecraft:stick".equals(ingredient.getAsString())) {
            return new com.google.gson.JsonPrimitive("gtceu:treated_wood_rod");
        }
        if (ingredient.isJsonArray()) {
            var copy = ingredient.getAsJsonArray().deepCopy();
            for (int index = 0; index < copy.size(); index++) {
                copy.set(index, replaceVanillaStick(copy.get(index)));
            }
            return copy;
        }
        if (ingredient.isJsonObject()) {
            var copy = ingredient.getAsJsonObject().deepCopy();
            if (copy.has("item") && "minecraft:stick".equals(copy.get("item").getAsString())) {
                copy.addProperty("item", "gtceu:treated_wood_rod");
            }
            if (copy.has("items")) {
                copy.add("items", replaceVanillaStick(copy.get("items")));
            }
            return copy;
        }
        return ingredient.deepCopy();
    }
}
