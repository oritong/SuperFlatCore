package com.oritong.superflatcore.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public final class DiamondContinentWorld {
    public static final BlockPos SPAWN = new BlockPos(2, -6, -2);

    private DiamondContinentWorld() {}

    public static boolean hasTemplateTerrain(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD
                || !(level.getChunkSource().getGenerator() instanceof FlatLevelSource flat)) {
            return false;
        }
        var layers = flat.settings().getLayersInfo();
        return layers.size() == 3
                && layers.get(0).getHeight() == 1 && layers.get(0).getBlockState().is(Blocks.BEDROCK)
                && layers.get(1).getHeight() == 31 && layers.get(1).getBlockState().is(Blocks.DEEPSLATE_DIAMOND_ORE)
                && layers.get(2).getHeight() == 32 && layers.get(2).getBlockState().is(Blocks.DIAMOND_ORE);
    }
}
