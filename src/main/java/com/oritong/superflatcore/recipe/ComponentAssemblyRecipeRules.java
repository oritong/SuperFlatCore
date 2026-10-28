package com.oritong.superflatcore.recipe;

import java.util.List;
import java.util.Set;

/** Output selection and integer-unit scaling, independent of registry and tag reload order. */
public final class ComponentAssemblyRecipeRules {
    private static final List<String> TIERS = List.of(
            "ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv", "opv", "max");
    private static final Set<String> COMPONENTS = Set.of(
            "electric_motor", "electric_piston", "electric_pump", "conveyor_module",
            "robot_arm", "field_generator", "emitter", "sensor");

    private ComponentAssemblyRecipeRules() {}

    public static boolean isComponent(String namespace, String path) {
        return getComponentTier(namespace, path) >= 0;
    }

    public static int getComponentTier(String namespace, String path) {
        if (!"gtceu".equals(namespace)) {
            return -1;
        }
        int separator = path.indexOf('_');
        if (separator <= 0 || !COMPONENTS.contains(path.substring(separator + 1))) {
            return -1;
        }
        return TIERS.indexOf(path.substring(0, separator));
    }

    public static boolean isMufflerTierSufficient(int mufflerTier, int componentTier) {
        return mufflerTier >= 0 && mufflerTier >= componentTier;
    }

    public static int scaleDuration(int ticks) {
        // Derived recipes must expose the same duration as their source assembler recipes.
        // The machine applies its runtime penalty separately, so this value must not be
        // scaled while the recipe is copied into the recipe viewer.
        return Math.max(1, ticks);
    }

    public static long scaleVoltage(long voltage) {
        if (voltage <= 0) {
            return voltage;
        }
        // Exact 9/10 scaling without overflowing a long or losing precision through a double.
        return Math.max(1, voltage / 10 * 9 + (voltage % 10 * 9 + 5) / 10);
    }
}
