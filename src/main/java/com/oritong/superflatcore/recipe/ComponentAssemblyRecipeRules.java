package com.oritong.superflatcore.recipe;

import java.util.Set;

/** Output selection and integer-unit scaling, independent of registry and tag reload order. */
public final class ComponentAssemblyRecipeRules {
    private static final Set<String> TIERS = Set.of(
            "ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv", "opv", "max");
    private static final Set<String> COMPONENTS = Set.of(
            "electric_motor", "electric_piston", "electric_pump", "conveyor_module",
            "robot_arm", "field_generator", "emitter", "sensor");

    private ComponentAssemblyRecipeRules() {}

    public static boolean isComponent(String namespace, String path) {
        if (!"gtceu".equals(namespace)) {
            return false;
        }
        int separator = path.indexOf('_');
        return separator > 0 && TIERS.contains(path.substring(0, separator))
                && COMPONENTS.contains(path.substring(separator + 1));
    }

    public static int scaleDuration(int ticks) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, (ticks * 5L + 2) / 4));
    }

    public static long scaleVoltage(long voltage) {
        if (voltage <= 0) {
            return voltage;
        }
        // Exact 9/10 scaling without overflowing a long or losing precision through a double.
        return Math.max(1, voltage / 10 * 9 + (voltage % 10 * 9 + 5) / 10);
    }
}
