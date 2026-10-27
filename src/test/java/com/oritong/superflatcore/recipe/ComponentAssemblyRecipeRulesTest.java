package com.oritong.superflatcore.recipe;

public final class ComponentAssemblyRecipeRulesTest {
    private static int checks;

    public static void main(String[] args) {
        for (String component : new String[] {"electric_motor", "electric_piston", "electric_pump", "conveyor_module",
                "robot_arm", "field_generator", "emitter", "sensor"}) {
            expect(ComponentAssemblyRecipeRules.isComponent("gtceu", "lv_" + component), "LV " + component);
            expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "lv_" + component) == 1,
                    "GTNN fallback entries resolve to LV for " + component);
            expect(ComponentAssemblyRecipeRules.isComponent("gtceu", "opv_" + component), "OpV " + component);
            int mvTier = ComponentAssemblyRecipeRules.getComponentTier("gtceu", "mv_" + component);
            expect(mvTier == 2, "MV output tier for " + component);
            expect(!ComponentAssemblyRecipeRules.isMufflerTierSufficient(1, mvTier),
                    "LV muffler rejects MV " + component + " even with an LV-power recipe");
            expect(ComponentAssemblyRecipeRules.isMufflerTierSufficient(2, mvTier),
                    "MV muffler accepts MV " + component + " regardless of recipe EU/t");
            expect(ComponentAssemblyRecipeRules.isMufflerTierSufficient(3, mvTier),
                    "HV muffler accepts lower-tier " + component);
        }
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "ulv_electric_motor") == 0, "ULV tier index");
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "lv_electric_motor") == 1, "LV tier index");
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "luv_electric_motor") == 6, "LuV tier index");
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "opv_electric_motor") == 13, "OpV tier index");
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "max_electric_motor") == 14, "MAX tier index");
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "lv_machine_hull") == -1, "Non-component tier");
        expect(ComponentAssemblyRecipeRules.getComponentTier("gtceu", "fake_electric_motor") == -1, "Invalid tier");
        expect(ComponentAssemblyRecipeRules.getComponentTier("minecraft", "mv_sensor") == -1, "Foreign namespace tier");
        expect(ComponentAssemblyRecipeRules.isMufflerTierSufficient(1, -1), "Other recipes retain normal power checks");
        expect(!ComponentAssemblyRecipeRules.isMufflerTierSufficient(-1, -1), "Missing muffler cannot run recipes");
        expect(!ComponentAssemblyRecipeRules.isComponent("gtceu", "lv_machine_hull"), "Exclude machine hulls");
        expect(!ComponentAssemblyRecipeRules.isComponent("gtceu", "lv_fluid_regulator"), "Exclude fluid regulators");
        expect(!ComponentAssemblyRecipeRules.isComponent("gtceu", "lv_electric_motor_extra"), "Exact component names");
        expect(!ComponentAssemblyRecipeRules.isComponent("gtceu", "electric_motor_lv"), "Registry name ordering");
        expect(!ComponentAssemblyRecipeRules.isComponent("gtceu", "wrapped_lv_electric_motor"), "Exclude wrapped items");
        expect(!ComponentAssemblyRecipeRules.isComponent("minecraft", "lv_electric_motor"), "GT namespace only");
        expect(!ComponentAssemblyRecipeRules.isComponent("gtceu", "electric_motor"), "Tier required");
        expect(ComponentAssemblyRecipeRules.scaleDuration(100) == 125, "5 seconds becomes 6.25 seconds");
        expect(ComponentAssemblyRecipeRules.scaleDuration(10) == 13, "Half tick rounds up");
        expect(ComponentAssemblyRecipeRules.scaleDuration(1) == 1, "Positive minimum duration");
        expect(ComponentAssemblyRecipeRules.scaleDuration(Integer.MAX_VALUE) == Integer.MAX_VALUE, "Duration saturates");
        expect(ComponentAssemblyRecipeRules.scaleVoltage(32) == 29, "LV power rounds to 29 EU/t");
        expect(ComponentAssemblyRecipeRules.scaleVoltage(120) == 108, "Exact 10 percent power discount");
        expect(ComponentAssemblyRecipeRules.scaleVoltage(5) == 5, "Half EU rounds up");
        expect(ComponentAssemblyRecipeRules.scaleVoltage(1) == 1, "Positive minimum EU/t");
        expect(ComponentAssemblyRecipeRules.scaleVoltage(0) == 0, "Zero EU stays zero");
        expect(ComponentAssemblyRecipeRules.scaleVoltage(Long.MAX_VALUE) == 8301034833169298226L,
                "Long voltage arithmetic retains precision without overflow");
        System.out.println("Passed " + checks + " component assembly selection and scaling checks.");
    }

    private static void expect(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
        checks++;
    }
}
