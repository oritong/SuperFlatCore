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
        require(roseGold.get("temperature").getAsInt() == 500, "Rose gold must melt at 500 C");
        require(roseGold.getAsJsonObject("result").get("amount").getAsInt() == 144, "Melting amount changed");
        require(roseGold.get("time").getAsInt() == 52, "Existing melting time changed");
        String normalized = roseGold.toString();
        adjust(roseGold);
        require(normalized.equals(roseGold.toString()), "Temperature normalization is not idempotent");

        JsonObject steel = json("""
                {"type":"tconstruct:damagable_melting","result":{"fluid":"gtceu:steel","amount":432},"temperature":1200}
                """);
        adjust(steel);
        require(steel.get("temperature").getAsInt() == 950, "Steel must melt at 950 C");

        JsonObject parts = json("""
                {"type":"tconstruct:material_melting","result":{"fluid":"gtceu:rose_gold","amount":144},"temperature":550}
                """);
        adjust(parts);
        require(parts.get("temperature").getAsInt() == 500, "Rose gold tool-part override was missed");
        JsonObject material = json("""
                {"type":"tconstruct:material_fluid","fluid":{"tag":"forge:steel","amount":144},"temperature":1200}
                """);
        adjust(material);
        require(material.get("temperature").getAsInt() == 950, "Tagged steel material-fluid recipe was missed");

        JsonObject conditional = json("""
                {"type":"forge:conditional","recipes":[{"conditions":[],"recipe":
                  {"type":"tconstruct:ore_melting","result":{"fluid":"gtceu:rose_gold","amount":144},"temperature":550}}]}
                """);
        adjust(conditional);
        require(conditional.getAsJsonArray("recipes").get(0).getAsJsonObject().getAsJsonObject("recipe")
                .get("temperature").getAsInt() == 500, "Conditional melting was missed");

        // The final recipe-object pass uses this same policy, without relying on a JSON loading hook.
        require(GmcSmelteryRules.temperature("rose_gold", false) == 500, "Direct rose gold part policy is incorrect");
        require(GmcSmelteryRules.temperature("steel", true) == 950, "Direct steel part policy is incorrect");
        require(GmcSmelteryRules.temperature("bronze", false) == 998, "Normal non-blast policy changed");
        require(GmcSmelteryRules.temperature("aluminium", true) == 1500, "Normal blast policy changed");
        JsonObject steelPart = json("""
                {"type":"tconstruct:material_melting","result":{"fluid":"gtceu:steel","amount":144},"temperature":1200}
                """);
        adjust(steelPart);
        require(steelPart.get("temperature").getAsInt() == 950, "Steel tool-part override was missed");
        JsonObject wroughtIronPart = json("""
                {"type":"tconstruct:material_melting","result":{"fluid":"gtceu:wrought_iron","amount":144},"temperature":998}
                """);
        adjust(wroughtIronPart);
        require(wroughtIronPart.get("temperature").getAsInt() == 800, "Wrought iron tool-part override was missed");

        JsonObject blazeFuel = json("""
                {"type":"tconstruct:melting_fuel","duration":150,"fluid":{"amount":50,"fluid":"tconstruct:blazing_blood"},
                 "rate":15,"temperature":1500}
                """);
        blazeFuel.add("fluid", GmcSmelteryRules.replaceBlazingBloodFuel(blazeFuel.get("fluid")));
        require(blazeFuel.getAsJsonObject("fluid").get("fluid").getAsString().equals("gtceu:blaze"), "Blazing blood remains a fuel");
        require(blazeFuel.getAsJsonObject("fluid").get("amount").getAsInt() == 50, "Fuel consumption changed");
        require(blazeFuel.get("duration").getAsInt() == 150 && blazeFuel.get("rate").getAsInt() == 15
                && blazeFuel.get("temperature").getAsInt() == 1500, "Fuel properties changed");
        JsonElement lava = JsonParser.parseString("{\"fluid\":\"minecraft:lava\",\"amount\":50}");
        require(GmcSmelteryRules.replaceBlazingBloodFuel(lava) == lava, "Lava fuel changed");
        JsonObject taggedFuel = GmcSmelteryRules.replaceBlazingBloodFuel(
                JsonParser.parseString("{\"tag\":\"tconstruct:blazing_blood\",\"amount\":25}")).getAsJsonObject();
        require(!taggedFuel.has("tag") && taggedFuel.get("fluid").getAsString().equals("gtceu:blaze")
                && taggedFuel.get("amount").getAsInt() == 25, "Tagged blazing blood fuel was not replaced");
        JsonElement alternatives = JsonParser.parseString("[{\"fluid\":\"tconstruct:blazing_blood\",\"amount\":50},"
                + "{\"fluid\":\"minecraft:lava\",\"amount\":50}]");
        String rewritten = GmcSmelteryRules.replaceBlazingBloodFuel(alternatives).toString();
        require(rewritten.contains("gtceu:blaze") && rewritten.contains("minecraft:lava")
                && !rewritten.contains("tconstruct:blazing_blood"), "Fuel alternatives were not preserved");

        // The real Mantle EMPTY fails serialization because amount=0. This path also initializes the fuel lookup.
        Object emptyFuel = new Object();
        require(GmcSmelteryRules.replaceFuelInput(emptyFuel, emptyFuel,
                input -> { throw new AssertionError("Serialized the zero-sized empty fuel sentinel"); },
                input -> { throw new AssertionError("Deserialized the empty fuel sentinel"); }) == emptyFuel,
                "Empty fuel identity was lost");
        JsonElement emptyMarker = JsonParser.parseString("{\"fluid\":\"minecraft:empty\",\"amount\":0}");
        require(GmcSmelteryRules.replaceFuelInput(lava, emptyMarker, input -> input,
                input -> { throw new AssertionError("Recreated an unchanged fuel ingredient"); }) == lava,
                "Unrelated fuel ingredient identity changed");
        JsonElement bloodInput = JsonParser.parseString("{\"fluid\":\"tconstruct:blazing_blood\",\"amount\":50}");
        JsonElement blazeInput = GmcSmelteryRules.replaceFuelInput(bloodInput, emptyMarker, input -> input, input -> input);
        require(blazeInput.getAsJsonObject().get("fluid").getAsString().equals("gtceu:blaze")
                && blazeInput.getAsJsonObject().get("amount").getAsInt() == 50,
                "Guarded fuel path failed to replace blazing blood");

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
        System.out.println("Passed GMC material temperatures, melting gates, GT blaze fuel replacement and non-serializable empty fuel regression checks.");
    }

    private static void adjust(JsonObject recipe) {
        GmcSmelteryRules.adjustTemperature(recipe, fluid -> {
            if (!fluid.isJsonObject()) return -1;
            JsonObject object = fluid.getAsJsonObject();
            if (GmcSmelteryRules.string(object, "fluid").equals("gtceu:rose_gold")) return GmcSmelteryRules.temperature("rose_gold", false);
            if (GmcSmelteryRules.string(object, "fluid").equals("gtceu:wrought_iron")) return GmcSmelteryRules.temperature("wrought_iron", false);
            if (GmcSmelteryRules.string(object, "fluid").equals("gtceu:steel")
                    || GmcSmelteryRules.string(object, "tag").equals("forge:steel")) return GmcSmelteryRules.temperature("steel", true);
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
