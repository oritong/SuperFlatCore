package com.oritong.superflatcore.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.ToIntFunction;
import java.util.function.Predicate;
import java.util.function.Function;

/** JSON rules shared by static GMC recipes, dynamic GT recipes and reloads. */
public final class GmcSmelteryRules {
    public static final int NON_BLAST_TEMPERATURE = 998;
    public static final int BLAST_TEMPERATURE = 1500;

    private GmcSmelteryRules() {}

    public static int temperature(boolean needsBlastFurnace) {
        return needsBlastFurnace ? BLAST_TEMPERATURE : NON_BLAST_TEMPERATURE;
    }

    public static int temperature(String materialName, boolean needsBlastFurnace) {
        return switch (materialName) {
            case "rose_gold" -> 500;
            case "steel" -> 950;
            case "wrought_iron" -> 800;
            default -> temperature(needsBlastFurnace);
        };
    }

    public static boolean canMelt(boolean needsBlastFurnace, long blastEUt) {
        // GT defaults an unspecified blast EU/t to MV (120); HV can supply at most 512 EU/t.
        return !needsBlastFurnace || blastEUt <= 512;
    }

    public static <T> T replaceFuelInput(T input, T empty, Function<T, JsonElement> serialize,
                                         Function<JsonElement, T> deserialize) {
        // Mantle's EMPTY has amount 0 and cannot be serialized. Also preserve identity for solid fuel.
        if (input == empty) return input;
        JsonElement original = serialize.apply(input);
        JsonElement replacement = replaceBlazingBloodFuel(original);
        return replacement == original ? input : deserialize.apply(replacement);
    }

    /** Replaces only the fuel ingredient, preserving amount and any other fluid alternatives. */
    public static JsonElement replaceBlazingBloodFuel(JsonElement input) {
        if (input.isJsonArray()) {
            com.google.gson.JsonArray replacement = new com.google.gson.JsonArray();
            boolean changed = false;
            for (JsonElement child : input.getAsJsonArray()) {
                JsonElement updated = replaceBlazingBloodFuel(child);
                replacement.add(updated);
                changed |= updated != child;
            }
            return changed ? replacement : input;
        }
        if (!input.isJsonObject()) return input;
        JsonObject object = input.getAsJsonObject();
        String fluid = string(object, "fluid");
        if (!fluid.equals("tconstruct:blazing_blood") && !fluid.equals("tconstruct:flowing_blazing_blood")
                && !string(object, "tag").equals("tconstruct:blazing_blood")) return input;
        JsonObject replacement = object.deepCopy();
        replacement.remove("tag");
        replacement.addProperty("fluid", "gtceu:blaze");
        return replacement;
    }

    public static boolean isMelting(JsonObject recipe) {
        String type = string(recipe, "type");
        return type.startsWith("tconstruct:") && type.endsWith("melting");
    }

    public static boolean isBlockedMelting(JsonObject recipe, Predicate<JsonElement> blockedFluid) {
        if (!isMelting(recipe)) return false;
        JsonElement result = recipe.get("result");
        if (result != null && blockedFluid.test(result)) return true;
        if (recipe.has("byproducts") && recipe.get("byproducts").isJsonArray()) {
            for (JsonElement byproduct : recipe.getAsJsonArray("byproducts")) {
                if (blockedFluid.test(byproduct)) return true;
            }
        }
        return false;
    }

    public static void adjustTemperature(JsonObject recipe, ToIntFunction<JsonElement> fluidTemperature) {
        String type = string(recipe, "type");
        if (recipe.has("temperature") && (isMelting(recipe)
                || type.equals("tconstruct:material_fluid"))) {
            JsonElement fluid = recipe.get(type.equals("tconstruct:material_fluid") ? "fluid" : "result");
            int temperature = fluid == null ? -1 : fluidTemperature.applyAsInt(fluid);
            if (temperature >= 0) {
                recipe.addProperty("temperature", temperature);
            }
        }
        // Forge conditional recipes may contain nested alternatives.
        if (recipe.has("recipes") && recipe.get("recipes").isJsonArray()) {
            for (JsonElement alternative : recipe.getAsJsonArray("recipes")) {
                if (alternative.isJsonObject()) {
                    JsonElement nested = alternative.getAsJsonObject().get("recipe");
                    if (nested != null && nested.isJsonObject()) adjustTemperature(nested.getAsJsonObject(), fluidTemperature);
                }
            }
        }
    }

    public static boolean containsIngredient(JsonElement ingredient, String tag, String item) {
        if (ingredient == null) return false;
        if (ingredient.isJsonPrimitive() && ingredient.getAsJsonPrimitive().isString()) {
            return item.equals(ingredient.getAsString());
        }
        if (ingredient.isJsonArray()) {
            for (JsonElement entry : ingredient.getAsJsonArray()) {
                if (containsIngredient(entry, tag, item)) return true;
            }
            return false;
        }
        if (!ingredient.isJsonObject()) return false;
        JsonObject object = ingredient.getAsJsonObject();
        // Only simple ingredients are equivalent to a normal material/cast ingredient.
        return !object.has("type") && (tag.equals(string(object, "tag")) || item.equals(string(object, "item")));
    }

    public static String string(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : "";
    }
}
