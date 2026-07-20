package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.cover.AirIntakeCover;
import com.oritong.superflatcore.cover.SuperInfiniteWaterCover;

import net.minecraft.resources.ResourceLocation;

public final class SFCovers {
    public static final int BASIC_AIR_INTAKE_RATE = 30;
    public static final int ADVANCED_AIR_INTAKE_RATE = 150;
    public static final int ELITE_AIR_INTAKE_RATE = 500;
    public static final int ULTIMATE_AIR_INTAKE_RATE = 1_000;
    public static final int CREATIVE_AIR_INTAKE_RATE = Integer.MAX_VALUE;

    public static final ResourceLocation SUPER_INFINITE_WATER_ID = SuperFlatCore.id("super_infinite_water");
    public static final CoverDefinition SUPER_INFINITE_WATER = new CoverDefinition(
            SUPER_INFINITE_WATER_ID,
            SuperInfiniteWaterCover::new,
            () -> () -> new SimpleCoverRenderer(new ResourceLocation("gtceu", "block/cover/infinite_water")));

    public static final CoverDefinition BASIC_AIR_INTAKE = airIntake("basic", BASIC_AIR_INTAKE_RATE);
    public static final CoverDefinition ADVANCED_AIR_INTAKE = airIntake("advanced", ADVANCED_AIR_INTAKE_RATE);
    public static final CoverDefinition ELITE_AIR_INTAKE = airIntake("elite", ELITE_AIR_INTAKE_RATE);
    public static final CoverDefinition ULTIMATE_AIR_INTAKE = airIntake("ultimate", ULTIMATE_AIR_INTAKE_RATE);
    public static final CoverDefinition CREATIVE_AIR_INTAKE = airIntake("creative", CREATIVE_AIR_INTAKE_RATE);

    private SFCovers() {}

    public static void registerCovers(GTCEuAPI.RegisterEvent<ResourceLocation, CoverDefinition> event) {
        event.register(SUPER_INFINITE_WATER_ID, SUPER_INFINITE_WATER);
        event.register(BASIC_AIR_INTAKE.getId(), BASIC_AIR_INTAKE);
        event.register(ADVANCED_AIR_INTAKE.getId(), ADVANCED_AIR_INTAKE);
        event.register(ELITE_AIR_INTAKE.getId(), ELITE_AIR_INTAKE);
        event.register(ULTIMATE_AIR_INTAKE.getId(), ULTIMATE_AIR_INTAKE);
        event.register(CREATIVE_AIR_INTAKE.getId(), CREATIVE_AIR_INTAKE);
    }

    private static CoverDefinition airIntake(String tier, int transferRate) {
        ResourceLocation id = SuperFlatCore.id(tier + "_air_intake");
        return new CoverDefinition(
                id,
                (definition, coverable, side) -> new AirIntakeCover(definition, coverable, side, transferRate),
                () -> () -> new SimpleCoverRenderer(SuperFlatCore.id("block/cover/" + tier + "_air_intake_cover")));
    }
}
