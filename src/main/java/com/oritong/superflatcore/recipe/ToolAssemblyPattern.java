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
                JsonElement ingredient = keys.get(String.valueOf(symbol));
                if (ingredient == null) {
                    throw new IllegalArgumentException("Undefined tool recipe symbol: " + symbol);
                }
                counts.merge(ingredient, 1, Integer::sum);
            }
        }
        return counts;
    }
}
