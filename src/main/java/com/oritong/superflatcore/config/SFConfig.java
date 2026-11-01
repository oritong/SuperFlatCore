package com.oritong.superflatcore.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class SFConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue WIND_GENERATOR_BASE_PRODUCTION = BUILDER
            .comment("Base wind generator output in EU/t. Changes apply to newly placed generators.")
            .defineInRange("windGeneratorBaseProduction", 60, 1, 32_768);

    public static final ForgeConfigSpec.IntValue WIND_GENERATOR_ACTIVE_ROTOR_WEAR = BUILDER
            .comment("Wind rotor durability lost per second while generating.")
            .defineInRange("windGeneratorActiveRotorWear", 6, 0, 32_768);

    public static final ForgeConfigSpec.IntValue WIND_GENERATOR_FULL_ROTOR_WEAR = BUILDER
            .comment("Wind rotor durability lost per second while the energy buffer is full.")
            .defineInRange("windGeneratorFullRotorWear", 2, 0, 32_768);

    private static final ForgeConfigSpec SPEC = BUILDER.build();

    private SFConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
