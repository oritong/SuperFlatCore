package com.oritong.superflatcore.compat.diamondcore.gtnn;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GTNNBedrockOreMinerRestrictions {

    private static final Set<ResourceLocation> BANNED_OUTPUTS = Set.of(
            new ResourceLocation("gtceu", "raw_bauxite"),
            new ResourceLocation("gtceu", "raw_coal"));

    private GTNNBedrockOreMinerRestrictions() {
    }

    public static boolean isBanned(Item item) {
        return item != null && BANNED_OUTPUTS.contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isBanned(ItemStack stack) {
        return !stack.isEmpty() && isBanned(stack.getItem());
    }

    public static boolean isBannedWeightedOre(Object weightedOre) {
        try {
            Object output = weightedOre.getClass().getMethod("getSecond").invoke(weightedOre);
            return output instanceof ItemStack stack && isBanned(stack);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
