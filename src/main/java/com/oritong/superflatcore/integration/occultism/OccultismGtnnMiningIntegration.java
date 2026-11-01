package com.oritong.superflatcore.integration.occultism;

import dev.arbor.gtnn.api.recipe.OresHelper;
import kotlin.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class OccultismGtnnMiningIntegration {
    private static final Set<ResourceLocation> BANNED_OUTPUTS = Set.of(
            new ResourceLocation("gtceu", "raw_bauxite"),
            new ResourceLocation("gtceu", "raw_coal"));

    private OccultismGtnnMiningIntegration() {}

    public static void handleMine(Level level, LazyOptional<ItemStackHandler> inputOptional,
                                  LazyOptional<ItemStackHandler> outputOptional, int rollsPerOperation) {
        if (level == null || level.isClientSide) {
            return;
        }

        ItemStackHandler input = inputOptional.orElse(null);
        ItemStackHandler output = outputOptional.orElse(null);
        if (input == null || output == null) {
            return;
        }

        List<WeightedOre> ores = getDimensionOres(level);
        int totalWeight = 0;
        for (WeightedOre ore : ores) {
            totalWeight = Math.addExact(totalWeight, ore.weight());
        }
        if (totalWeight <= 0) {
            return;
        }

        for (int roll = 0; roll < Math.max(1, rollsPerOperation); roll++) {
            int selection = level.random.nextInt(totalWeight);
            for (WeightedOre ore : ores) {
                selection -= ore.weight();
                if (selection < 0) {
                    if (!ore.stack().isEmpty()) {
                        ItemHandlerHelper.insertItemStacked(output, ore.stack().copy(), false);
                    }
                    break;
                }
            }
        }

        ItemStack spirit = input.getStackInSlot(0);
        if (spirit.hurt(1, level.random, null)) {
            spirit.shrink(1);
            spirit.setDamageValue(0);
        }
    }

    private static List<WeightedOre> getDimensionOres(Level level) {
        List<WeightedOre> ores = new ArrayList<>();
        for (Pair<Set<ResourceKey<Level>>, Pair<Integer, ItemStack>> entry : OresHelper.INSTANCE.getORES_WEIGHTED()) {
            if (!entry.getFirst().contains(level.dimension())) {
                continue;
            }

            Pair<Integer, ItemStack> weightedOutput = entry.getSecond();
            int weight = weightedOutput.getFirst();
            ItemStack stack = weightedOutput.getSecond();
            if (weight > 0 && stack != null && !isBanned(stack)) {
                ores.add(new WeightedOre(weight, stack.copy()));
            }
        }
        return ores;
    }

    private static boolean isBanned(ItemStack stack) {
        return !stack.isEmpty() && BANNED_OUTPUTS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private record WeightedOre(int weight, ItemStack stack) {}
}
