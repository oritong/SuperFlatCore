package com.oritong.superflatcore.integration.botania;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraftforge.registries.ForgeRegistries;

import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TerraPlateStructure {
    private static final String RESOURCE = "/data/superflatcore/structures/terra_plate.json";

    private TerraPlateStructure() {}

    public static IMultiblock createMultiblock() {
        InputStream stream = Objects.requireNonNull(TerraPlateStructure.class.getResourceAsStream(RESOURCE),
                "Missing Terra Plate structure");
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject structure = JsonParser.parseReader(reader).getAsJsonObject();
            BlockPos anchor = readPosition(structure.getAsJsonArray("anchor"));
            List<IStateMatcher> palette = new ArrayList<>();
            for (JsonElement element : structure.getAsJsonArray("palette")) {
                palette.add(createMatcher(element.getAsJsonObject()));
            }

            Map<BlockPos, IStateMatcher> blocks = new LinkedHashMap<>();
            for (JsonElement element : structure.getAsJsonArray("blocks")) {
                JsonObject block = element.getAsJsonObject();
                blocks.put(readPosition(block.getAsJsonArray("pos")), palette.get(block.get("state").getAsInt()));
            }
            return PatchouliAPI.get().makeSparseMultiblock(blocks)
                    .offset(anchor.getX(), anchor.getY(), anchor.getZ())
                    .setId(SuperFlatCore.id("terra_plate"));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read Terra Plate structure", exception);
        }
    }

    private static IStateMatcher createMatcher(JsonObject entry) {
        ResourceLocation id = new ResourceLocation(entry.get("Name").getAsString());
        Block block = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(id), "Missing structure block: " + id);
        BlockState state = block.defaultBlockState();
        List<Property<?>> properties = new ArrayList<>();
        if (entry.has("Properties")) {
            for (Map.Entry<String, JsonElement> entryProperty : entry.getAsJsonObject("Properties").entrySet()) {
                String name = entryProperty.getKey();
                Property<?> property = Objects.requireNonNull(block.getStateDefinition().getProperty(name),
                        "Missing structure property: " + id + "[" + name + "]");
                state = setProperty(state, property, entryProperty.getValue().getAsString());
                if (!name.equals("waterlogged") && !id.getPath().startsWith("chalk_glyph_")
                        && !id.getPath().equals("mana_glass_pane")) {
                    properties.add(property);
                }
            }
        }
        return PatchouliAPI.get().propertyMatcher(state, properties.toArray(Property<?>[]::new));
    }

    private static <Value extends Comparable<Value>> BlockState setProperty(
            BlockState state, Property<Value> property, String value) {
        return state.setValue(property, property.getValue(value)
                .orElseThrow(() -> new IllegalArgumentException("Invalid structure property value: " + value)));
    }

    private static BlockPos readPosition(JsonArray position) {
        return new BlockPos(position.get(0).getAsInt(), position.get(1).getAsInt(), position.get(2).getAsInt());
    }
}
