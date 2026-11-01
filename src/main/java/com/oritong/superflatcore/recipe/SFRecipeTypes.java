package com.oritong.superflatcore.recipe;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;

import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemStackHandler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.api.recipe.ManaInfusionRecipe;

import java.util.ArrayList;
import java.util.List;

import com.oritong.superflatcore.SuperFlatCore;

public final class SFRecipeTypes {
    public static final GTRecipeType HEAT_EXCHANGER_RECIPES = register(
                    "heat_exchanger", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 0, 2, 2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.COOLING);

    public static final GTRecipeType SUPERCRITICAL_STEAM_TURBINE_FUELS = register(
                    "supercritical_steam_turbine", GTRecipeTypes.GENERATOR)
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    public static final GTRecipeType MANA_INFUSION_RECIPES = registerManaInfusionType()
            .setMaxIOSize(1, 1, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .addDataInfo(data -> net.minecraft.network.chat.Component.translatable(
                    "recipe.superflatcore.mana_required", data.getLong("superflatcore:mana")).getString())
            .addDataInfo(data -> Component.translatable("recipe.superflatcore.mana_infusion_base").getString())
            .setUiBuilder(SFRecipeTypes::addManaInfusionBaseWidget);

    public static final GTRecipeType MANA_OPPRESSION_RECIPES = register(
                    "mana_oppression", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 4, 3, 2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .addDataInfo(data -> net.minecraft.network.chat.Component.translatable(
                    "recipe.superflatcore.mana_required", data.getLong("superflatcore:mana")).getString());

    private SFRecipeTypes() {}

    private static GTRecipeType registerManaInfusionType() {
        GTRecipeType recipeType = new GTRecipeType(SuperFlatCore.id("mana_infusions"), GTRecipeTypes.MULTIBLOCK,
                BotaniaRecipeTypes.MANA_INFUSION_TYPE) {
            @Override
            public GTRecipe toGTrecipe(ResourceLocation id, Recipe<?> recipe) {
                GTRecipe converted = super.toGTrecipe(id, recipe);
                if (recipe instanceof ManaInfusionRecipe infusionRecipe) {
                    converted.data.putLong("superflatcore:mana", infusionRecipe.getManaToConsume());
                    List<ItemStack> bases = infusionRecipe.getRecipeCatalyst() == null
                            ? List.of(new ItemStack(BotaniaBlocks.manaDiamondBlock))
                            : infusionRecipe.getRecipeCatalyst().getDisplayedStacks();
                    ListTag baseStacks = new ListTag();
                    for (ItemStack base : bases) {
                        if (!base.isEmpty()) {
                            baseStacks.add(base.save(new CompoundTag()));
                        }
                    }
                    converted.data.put("superflatcore:mana_infusion_base", baseStacks);
                }
                return converted;
            }
        };
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    private static void addManaInfusionBaseWidget(GTRecipe recipe, WidgetGroup group) {
        ListTag serializedBases = recipe.data.getList("superflatcore:mana_infusion_base", Tag.TAG_COMPOUND);
        List<ItemStack> bases = new ArrayList<>(serializedBases.size());
        for (int i = 0; i < serializedBases.size(); i++) {
            ItemStack base = ItemStack.of(serializedBases.getCompound(i));
            if (!base.isEmpty()) {
                bases.add(base);
            }
        }
        if (bases.isEmpty()) {
            bases.add(new ItemStack(BotaniaBlocks.manaDiamondBlock));
        }

        int x = 4;
        int y = 22;
        CycleItemStackHandler handler = new CycleItemStackHandler(List.of(bases));
        group.addWidget(new SlotWidget(handler, 0, x, y)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setIngredientIO(IngredientIO.CATALYST)
                .setCanTakeItems(false)
                .setCanPutItems(false)
                .setDrawHoverTips(true));
    }

    public static void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        // Class loading registers SFC's recipe types before GTCEu freezes the registry.
    }

    public static GTRecipeType register(String name, String group, net.minecraft.world.item.crafting.RecipeType<?>... proxyRecipes) {
        GTRecipeType recipeType = new GTRecipeType(SuperFlatCore.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }
}
