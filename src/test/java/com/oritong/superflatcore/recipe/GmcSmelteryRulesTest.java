package com.oritong.superflatcore.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Regression cases for GMC's static recipes, material recipes and conditional recipes. */
public final class GmcSmelteryRulesTest {
    public static void main(String[] args) {
        JsonObject roseGold = json("""
                {"type":"tconstruct:melting","ingredient":{"tag":"forge:ingots/rose_gold"},
                 "result":{"fluid":"gtceu:rose_gold","amount":144},"temperature":550,"time":52}
                """);
        adjust(roseGold);
        require(roseGold.get("temperature").getAsInt() == 998, "Rose gold must melt at 998 C");
        require(roseGold.getAsJsonObject("result").get("amount").getAsInt() == 144, "Melting amount changed");
        require(roseGold.get("time").getAsInt() == 52, "Existing melting time changed");
        String normalized = roseGold.toString();
        adjust(roseGold);
        require(normalized.equals(roseGold.toString()), "Temperature normalization is not idempotent");

        JsonObject steel = json("""
                {"type":"tconstruct:damagable_melting","result":{"fluid":"gtceu:steel","amount":432},"temperature":1200}
                """);
        adjust(steel);
        require(steel.get("temperature").getAsInt() == 1500, "Blast-furnace material must melt at 1500 C");

        JsonObject parts = json("""
                {"type":"tconstruct:material_melting","result":{"fluid":"gtceu:rose_gold","amount":144},"temperature":550}
                """);
        adjust(parts);
        require(parts.get("temperature").getAsInt() == 998, "Tool-part melting was missed");
        JsonObject material = json("""
                {"type":"tconstruct:material_fluid","fluid":{"tag":"forge:steel","amount":144},"temperature":1200}
                """);
        adjust(material);
        require(material.get("temperature").getAsInt() == 1500, "Tagged material-fluid recipe was missed");

        JsonObject conditional = json("""
                {"type":"forge:conditional","recipes":[{"conditions":[],"recipe":
                  {"type":"tconstruct:ore_melting","result":{"fluid":"gtceu:rose_gold","amount":144},"temperature":550}}]}
                """);
        adjust(conditional);
        require(conditional.getAsJsonArray("recipes").get(0).getAsJsonObject().getAsJsonObject("recipe")
                .get("temperature").getAsInt() == 998, "Conditional melting was missed");

        for (String source : new String[]{
                "{\"type\":\"gtceu:electric_blast_furnace\",\"temperature\":2700,\"result\":{\"fluid\":\"gtceu:steel\"}}",
                "{\"type\":\"tconstruct:melting\",\"temperature\":750,\"result\":{\"fluid\":\"tconstruct:molten_glass\"}}",
                "{\"type\":\"tconstruct:alloy\",\"temperature\":600,\"result\":{\"fluid\":\"gtceu:rose_gold\"}}"}) {
            JsonObject unchanged = json(source);
            String before = unchanged.toString();
            adjust(unchanged);
            require(before.equals(unchanged.toString()), "An unrelated recipe was modified");
        }

        require(matches("{\"tag\":\"forge:rods/rose_gold\"}"), "Existing tagged rod output was not detected");
        require(matches("\"gtceu:rose_gold_rod\""), "Existing string output was not detected");
        require(matches("[{\"item\":\"minecraft:stick\"},{\"item\":\"gtceu:rose_gold_rod\"}]"), "Alternative ingredient was missed");
        require(!matches("{\"tag\":\"forge:plates/rose_gold\"}"), "A plate incorrectly suppresses the missing rod cast");
        require(!matches("{\"type\":\"forge:nbt\",\"item\":\"gtceu:rose_gold_rod\",\"nbt\":{}}"), "An NBT-specific recipe suppresses a generic cast");

        require(GmcSmelteryRules.canMelt(false, 1920), "Non-blast material was incorrectly gated");
        require(GmcSmelteryRules.canMelt(true, -1), "GT default MV blast recipe was blocked");
        require(GmcSmelteryRules.canMelt(true, 480), "HV blast material was blocked");
        require(GmcSmelteryRules.canMelt(true, 512), "HV voltage boundary was blocked");
        require(!GmcSmelteryRules.canMelt(true, 513), "Above-HV voltage boundary was allowed");
        require(!GmcSmelteryRules.canMelt(true, 1920), "EV blast material was allowed");
        require(!GmcSmelteryRules.canMelt(true, 7680), "IV blast material was allowed");
        for (String type : new String[]{"melting", "ore_melting", "damagable_melting", "material_melting"}) {
            JsonObject highTier = json("{\"type\":\"tconstruct:" + type + "\",\"result\":{\"fluid\":\"gtceu:tungsten_steel\"}}");
            require(blocked(highTier), "EV+ " + type + " recipe was not blocked");
        }
        JsonObject byproduct = json("""
                {"type":"tconstruct:ore_melting","result":{"fluid":"gtceu:rose_gold","amount":144},
                 "byproducts":[{"fluid":"gtceu:tungsten_steel","amount":16}]}
                """);
        require(blocked(byproduct), "An EV+ byproduct bypasses the gate");
        require(!blocked(roseGold), "An allowed melting recipe was blocked");
        require(!blocked(json("{\"type\":\"gtceu:electric_blast_furnace\",\"result\":{\"fluid\":\"gtceu:tungsten_steel\"}}")),
                "GregTech's own blast furnace recipe was blocked");
        require(!blocked(json("{\"type\":\"tconstruct:casting_table\",\"fluid\":{\"fluid\":\"gtceu:tungsten_steel\"}}")),
                "A casting recipe was incorrectly blocked");
        System.out.println("Passed GMC 998/1500 C, static/tool/conditional recipes, missing rod detection and HV/EV/IV melting gate checks.");
    }

    private static void adjust(JsonObject recipe) {
        GmcSmelteryRules.adjustTemperature(recipe, fluid -> {
            if (!fluid.isJsonObject()) return -1;
            JsonObject object = fluid.getAsJsonObject();
            if (GmcSmelteryRules.string(object, "fluid").equals("gtceu:rose_gold")) return GmcSmelteryRules.temperature(false);
            if (GmcSmelteryRules.string(object, "fluid").equals("gtceu:steel")
                    || GmcSmelteryRules.string(object, "tag").equals("forge:steel")) return GmcSmelteryRules.temperature(true);
            return -1;
        });
    }

    private static boolean matches(String json) {
        JsonElement ingredient = JsonParser.parseString(json);
        return GmcSmelteryRules.containsIngredient(ingredient, "forge:rods/rose_gold", "gtceu:rose_gold_rod");
    }

    private static boolean blocked(JsonObject recipe) {
        return GmcSmelteryRules.isBlockedMelting(recipe, output -> output.isJsonObject()
                && GmcSmelteryRules.string(output.getAsJsonObject(), "fluid").equals("gtceu:tungsten_steel"));
    }

    private static JsonObject json(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
