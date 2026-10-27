package com.oritong.superflatcore.recipe;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.oritong.superflatcore.SuperFlatCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class SFToolRecipeTypes {
    public static final GTRecipeType TOOL_WELDING_ASSEMBLER_RECIPES = register()
            .setMaxIOSize(9, 1, 2, 0)
            .setSlotOverlay(false, false, GuiTextures.TOOL_SLOT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ASSEMBLER, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    private SFToolRecipeTypes() {}

    private static GTRecipeType register() {
        ResourceLocation id = SuperFlatCore.id("tool_welding_assembler");
        GTRecipeType type = new GTRecipeType(id, GTRecipeTypes.ELECTRIC);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, id, type);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, id, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(id, type);
        return type;
    }

    public static void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        // Initialize before the recipe type registry is frozen.
    }
}
