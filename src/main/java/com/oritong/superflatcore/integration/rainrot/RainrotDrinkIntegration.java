package com.oritong.superflatcore.integration.rainrot;

import com.oritong.superflatcore.mixin.ItemFoodPropertiesAccessor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class RainrotDrinkIntegration {
    private static final ResourceLocation[] FIVE_PEBBSI_IDS = {
            new ResourceLocation("rainrot", "five_pebbsi_classic"),
            new ResourceLocation("rainrot", "five_pebbsi_rubicon"),
            new ResourceLocation("rainrot", "five_pebbsi_crystal")
    };
    private static final FoodProperties ALWAYS_EAT_FIVE_PEBBSI = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(1.0F)
            .alwaysEat()
            .build();

    private RainrotDrinkIntegration() {}

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (ResourceLocation id : FIVE_PEBBSI_IDS) {
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item != null) {
                    ((ItemFoodPropertiesAccessor) item).superflatcore$setFoodProperties(ALWAYS_EAT_FIVE_PEBBSI);
                }
            }
        });
    }
}
