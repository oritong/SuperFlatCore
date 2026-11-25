package com.oritong.superflatcore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Adapted from AngelBlockRenewed by LaidBackSloth; see AngelBlockRenewed-MIT-LICENSE.txt. */
public final class AngelBlock extends Block {
    public AngelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (removed && !level.isClientSide && !player.isCreative()) {
            player.getInventory().placeItemBackInInventory(new ItemStack(this), true);
        }
        return removed;
    }
}
