package com.oritong.superflatcore.integration.industrial;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class IndustrialForegoingAugments {
    private static final String AUGMENT_NBT = "TitaniumAugment";

    private IndustrialForegoingAugments() {
    }

    public static void initializeMissingData(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() != null && stack.getTag().contains(AUGMENT_NBT, Tag.TAG_COMPOUND)) {
            return;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return;
        }
        if ("ifeu".equals(itemId.getNamespace())) {
            initializeExtraUpgrade(stack, itemId.getPath());
            return;
        }
        if (!"industrialforegoing".equals(itemId.getNamespace())) {
            return;
        }

        CompoundTag augment = new CompoundTag();
        switch (itemId.getPath()) {
            case "speed_addon_1" -> augment.putFloat("Speed", 2);
            case "speed_addon_2" -> augment.putFloat("Speed", 3);
            case "efficiency_addon_1" -> augment.putFloat("Efficiency", 1 - 0.1f);
            case "efficiency_addon_2" -> augment.putFloat("Efficiency", 1 - 2 * 0.1f);
            case "processing_addon_1" -> augment.putFloat("Processing", 2);
            case "processing_addon_2" -> augment.putFloat("Processing", 3);
            default -> {
                String path = itemId.getPath();
                if (!path.startsWith("range_addon")) {
                    return;
                }
                int tier;
                try {
                    tier = Integer.parseInt(path.substring("range_addon".length()));
                } catch (NumberFormatException exception) {
                    return;
                }
                if (tier < 0 || tier > 11) {
                    return;
                }
                augment.putFloat("Range", tier);
            }
        }
        stack.getOrCreateTag().put(AUGMENT_NBT, augment);
    }

    private static void initializeExtraUpgrade(ItemStack stack, String path) {
        CompoundTag augment = new CompoundTag();
        switch (path) {
            case "speed_addon_3" -> augment.putFloat("Speed", 4);
            case "speed_addon_4" -> augment.putFloat("Speed", 5);
            case "speed_addon_5" -> augment.putFloat("Speed", 6);
            case "speed_addon_6" -> augment.putFloat("Speed", 7);
            case "efficiency_addon_3" -> augment.putFloat("Efficiency", 1 - 3 * 0.1f);
            case "efficiency_addon_4" -> augment.putFloat("Efficiency", 1 - 4 * 0.1f);
            case "efficiency_addon_5" -> augment.putFloat("Efficiency", 1 - 5 * 0.1f);
            case "efficiency_addon_6" -> augment.putFloat("Efficiency", 1 - 6 * 0.1f);
            case "processing_addon_3" -> augment.putFloat("Processing", 4);
            case "processing_addon_4" -> augment.putFloat("Processing", 5);
            case "processing_addon_5" -> augment.putFloat("Processing", 6);
            case "processing_addon_6" -> augment.putFloat("Processing", 7);
            default -> {
                return;
            }
        }
        stack.getOrCreateTag().put(AUGMENT_NBT, augment);
    }
}
