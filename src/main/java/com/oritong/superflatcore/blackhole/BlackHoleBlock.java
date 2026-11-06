package com.oritong.superflatcore.blackhole;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFBlockEntities;
import com.oritong.superflatcore.registry.SFBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Oritech-compatible black-hole block; its visible body is supplied by the skill renderer. */
public final class BlackHoleBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(3, 3, 3, 13, 13, 13);
    private static final ResourceKey<DamageType> BLACK_HOLE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, SuperFlatCore.id("black_hole"));

    public BlackHoleBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlackHoleBlockEntity(SFBlockEntities.BLACK_HOLE.get(), pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, entity) -> {
            if (entity instanceof BlackHoleBlockEntity blackHole) {
                BlackHoleBlockEntity.serverTick(level1, pos, state1, blackHole);
            }
        };
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        if (entity instanceof Player player) {
            DamageSource damageSource = new DamageSource(level.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(BLACK_HOLE_DAMAGE));
            player.hurt(damageSource, Float.MAX_VALUE);
            return;
        }
        entity.kill();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // The skill renderer owns the visible effect. Keep a small vanilla fallback particle for
        // resource reloads and shader compilation frames.
        if (level.isClientSide && random.nextInt(4) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    pos.getX() + random.nextDouble(), pos.getY() + .8,
                    pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }
}
