package com.oritong.superflatcore.integration.moreburners;

import com.rekindled.embers.blockentity.MiniBoilerBlockEntity;
import net.dragonegg.moreburners.config.CommonConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Locale;

public final class MoreBurnersIntegration {
    private static final ResourceLocation EMBER_BURNER_ID = new ResourceLocation("moreburners", "ember_burner");
    private MoreBurnersIntegration() {}

    public static double getConfiguredEmberCost() {
        return Math.max(0.0, CommonConfig.EMBER_BURNER_EMBER_COST.get());
    }

    public static double getCycleEmberCost() {
        return getConfiguredEmberCost() * 100.0;
    }

    public static void heatMiniBoiler(Level level, BlockPos pos, MiniBoilerBlockEntity boiler) {
        if (level.isClientSide()) {
            return;
        }
        BlockPos burnerPos = pos.below();
        BlockState burnerState = level.getBlockState(burnerPos);
        if (!EMBER_BURNER_ID.equals(BuiltInRegistries.BLOCK.getKey(burnerState.getBlock()))) {
            return;
        }
        BlockEntity burner = level.getBlockEntity(burnerPos);
        if (!(burner instanceof MoreBurnersEmberBurnerAccess access)
                || !access.superflatcore$isCycleWorking()) {
            return;
        }
        double heat = getHeatLevel(burnerState);
        if (heat > 0.0) {
            boiler.boil(heat);
        }
    }

    private static double getHeatLevel(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (!"blaze".equals(property.getName())) {
                continue;
            }
            String value = state.getValue(property).toString().toLowerCase(Locale.ROOT);
            return switch (value) {
                case "fading", "kindled" -> 1.0;
                case "seething" -> 2.0;
                default -> 0.0;
            };
        }
        return 0.0;
    }
}
