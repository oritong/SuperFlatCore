package com.oritong.superflatcore.block;

import com.oritong.superflatcore.registry.SFMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WindGeneratorCollisionBlock extends Block {
    public static final IntegerProperty PART = IntegerProperty.create("part", 1, 4);

    public WindGeneratorCollisionBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return proxyShape(state, level, pos, context, false, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return proxyShape(state, level, pos, context, false, true);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return proxyShape(state, level, pos, null, true, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        BlockPos rootPos = pos.below(state.getValue(PART));
        BlockState rootState = level.getBlockState(rootPos);
        if (!rootState.is(SFMachines.WIND_GENERATOR.getBlock())) {
            return InteractionResult.PASS;
        }
        BlockHitResult rootHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), rootPos, hit.isInside());
        return rootState.getBlock().use(rootState, level, rootPos, player, hand, rootHit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
                                       Player player) {
        BlockPos rootPos = pos.below(state.getValue(PART));
        BlockState rootState = level.getBlockState(rootPos);
        return rootState.is(SFMachines.WIND_GENERATOR.getBlock())
                ? rootState.getBlock().getCloneItemStack(rootState, target, level, rootPos, player)
                : ItemStack.EMPTY;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (willHarvest) {
            return true;
        }
        BlockPos rootPos = pos.below(state.getValue(PART));
        BlockState rootState = level.getBlockState(rootPos);
        if (rootState.is(SFMachines.WIND_GENERATOR.getBlock())) {
            rootState.onDestroyedByPlayer(level, rootPos, player, false, rootState.getFluidState());
        }
        return super.onDestroyedByPlayer(state, level, pos, player, false, fluid);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity,
                              ItemStack tool) {
        BlockPos rootPos = pos.below(state.getValue(PART));
        BlockState rootState = level.getBlockState(rootPos);
        if (rootState.is(SFMachines.WIND_GENERATOR.getBlock())) {
            rootState.getBlock().playerDestroy(level, player, rootPos, rootState, level.getBlockEntity(rootPos), tool);
        } else {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }
        level.removeBlock(pos, false);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            BlockPos rootPos = pos.below(state.getValue(PART));
            if (level.getBlockState(rootPos).is(SFMachines.WIND_GENERATOR.getBlock())) {
                level.removeBlock(rootPos, false);
            }
            super.onRemove(state, level, pos, replacement, moving);
        }
    }

    public static void placeFor(Level level, BlockPos rootPos) {
        if (level.isClientSide) {
            return;
        }
        for (int part = 1; part <= 4; part++) {
            BlockPos partPos = rootPos.above(part);
            BlockState current = level.getBlockState(partPos);
            if (current.is(SFMachines.WIND_GENERATOR.getBlock())) {
                continue;
            }
            if (current.isAir() || current.canBeReplaced()) {
                BlockState collisionState = com.oritong.superflatcore.registry.SFBlocks.WIND_GENERATOR_COLLISION.get()
                        .defaultBlockState().setValue(PART, part);
                if (!current.equals(collisionState)) {
                    level.setBlock(partPos, collisionState, 3);
                }
            }
        }
    }

    public static boolean canPlaceAt(BlockGetter level, BlockPos rootPos) {
        for (int part = 1; part <= 4; part++) {
            BlockState state = level.getBlockState(rootPos.above(part));
            if (!state.isAir() && !state.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static void removeFor(Level level, BlockPos rootPos) {
        if (level.isClientSide) {
            return;
        }
        for (int part = 1; part <= 4; part++) {
            BlockPos partPos = rootPos.above(part);
            if (level.getBlockState(partPos).is(com.oritong.superflatcore.registry.SFBlocks.WIND_GENERATOR_COLLISION.get())) {
                level.removeBlock(partPos, false);
            }
        }
    }

    private static VoxelShape proxyShape(BlockState state, BlockGetter level, BlockPos pos,
                                         CollisionContext context, boolean occlusion, boolean collision) {
        BlockPos rootPos = pos.below(state.getValue(PART));
        BlockState rootState = level.getBlockState(rootPos);
        if (!rootState.is(SFMachines.WIND_GENERATOR.getBlock())) {
            return Shapes.empty();
        }
        VoxelShape shape;
        if (occlusion) {
            shape = rootState.getBlock().getOcclusionShape(rootState, level, rootPos);
        } else if (collision) {
            shape = rootState.getBlock().getCollisionShape(rootState, level, rootPos, context);
        } else {
            shape = rootState.getShape(level, rootPos);
        }
        return shape.move(0, -state.getValue(PART), 0);
    }
}
