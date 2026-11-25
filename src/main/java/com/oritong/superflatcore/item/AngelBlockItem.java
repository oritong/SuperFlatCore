package com.oritong.superflatcore.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Adapted from AngelBlockRenewed by LaidBackSloth; see AngelBlockRenewed-MIT-LICENSE.txt. */
public final class AngelBlockItem extends BlockItem {
    public AngelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 target = player.getEyePosition().add(player.getLookAngle().scale(4.5));
        BlockPos pos = BlockPos.containing(target);
        if (!level.isInWorldBounds(pos) || !level.getBlockState(pos).canBeReplaced()
                || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        // Retain the original midair target, using normal block placement for
        // collision checks, hand-specific stack consumption, sound and sync.
        var context = new BlockPlaceContext(level, player, hand, stack,
                new BlockHitResult(target, Direction.UP, pos, false));
        return new InteractionResultHolder<>(place(context), stack);
    }
}
