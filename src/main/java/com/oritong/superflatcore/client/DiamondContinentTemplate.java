package com.oritong.superflatcore.client;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.world.DiamondContinentWorld;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class DiamondContinentTemplate {
    private static final ResourceKey<WorldPreset> PRESET = ResourceKey.create(
            Registries.WORLD_PRESET, new ResourceLocation(SuperFlatCore.MODID, "diamond_continent"));
    private static final String SEED = "-4879535413869980491";
    private static final long DAY_TIME = 6625;

    // Ten modified terrain chunks around the spawn, plus the item frame and points of interest.
    private static final List<String> FILES = List.of(
            "region/r.-1.-1.mca", "region/r.-1.0.mca", "region/r.0.-1.mca", "region/r.0.0.mca",
            "entities/r.0.-1.mca", "poi/r.0.-1.mca", "poi/r.0.0.mca");

    private DiamondContinentTemplate() {}

    public static void setDefaults(WorldCreationUiState state) {
        var preset = state.getSettings().worldgenLoadContext()
                .registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(PRESET);
        state.setWorldType(new WorldCreationUiState.WorldTypeEntry(preset));
        state.setSeed(SEED);
        state.setGenerateStructures(false);
        state.setBonusChest(false);
        state.setDifficulty(Difficulty.NORMAL);
        state.setAllowCheats(true);

        GameRules rules = state.getGameRules();
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, null);
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, null);
        rules.getRule(GameRules.RULE_WATER_SOURCE_CONVERSION).set(false, null);
    }

    public static void install(LevelStorageSource.LevelStorageAccess access, ServerLevelData data) throws IOException {
        Path directory = access.getLevelPath(LevelResource.ROOT);
        List<Path> createdFiles = new ArrayList<>();
        try {
            for (String file : FILES) {
                Path target = directory.resolve(file);
                Files.createDirectories(target.getParent());
                try (InputStream source = DiamondContinentTemplate.class.getResourceAsStream(
                        "/world_template/superflatcore/" + file)) {
                    if (source == null) {
                        throw new FileNotFoundException(file);
                    }
                    try (OutputStream destination = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                        createdFiles.add(target);
                        source.transferTo(destination);
                    }
                }
            }
        } catch (IOException error) {
            for (Path target : createdFiles) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException cleanupError) {
                    error.addSuppressed(cleanupError);
                }
            }
            throw error;
        }

        data.setSpawn(DiamondContinentWorld.SPAWN, 0);
        data.setDayTime(DAY_TIME);
        // The template already has a spawn. Skip vanilla's spawn search and bonus chest placement.
        data.setInitialized(true);
    }
}
