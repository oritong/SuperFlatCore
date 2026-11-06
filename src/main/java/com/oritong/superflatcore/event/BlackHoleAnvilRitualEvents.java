package com.oritong.superflatcore.event;

import com.oritong.superflatcore.registry.SFBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** Turns a high-drop anvil impact into the black-hole acquisition ritual. */
@Mod.EventBusSubscriber(modid = "superflatcore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlackHoleAnvilRitualEvents {
    private static final int MIN_DROP_HEIGHT = 167;
    private static final ResourceLocation SHATTERED_CRYSTAL = new ResourceLocation("beyonddimensions", "shattered_space_time_crystallization");
    private static final ResourceLocation SINGULARITY = new ResourceLocation("ae2", "singularity");
    private static final ResourceLocation MATTER_BALL = new ResourceLocation("ae2", "matter_ball");

    private BlackHoleAnvilRitualEvents() {
    }

    @SubscribeEvent
    public static void onFallingEntityLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof FallingBlockEntity falling)
                || !(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }
        BlockState fallingState = falling.getBlockState();
        if (!isAnvil(fallingState)) {
            return;
        }
        BlockPos start = falling.getStartPos();
        BlockPos landing = falling.blockPosition();
        if (start == null || start.getY() - landing.getY() <= MIN_DROP_HEIGHT) {
            return;
        }
        if (!isAnvil(level.getBlockState(landing))) {
            return;
        }
        tryCreate(level, landing);
    }

    private static void tryCreate(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(pos), entity -> !entity.isRemoved());
        int crystal = 0;
        int singularity = 0;
        int matterBall = 0;
        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (SHATTERED_CRYSTAL.equals(id)) crystal += stack.getCount();
            else if (SINGULARITY.equals(id)) singularity += stack.getCount();
            else if (MATTER_BALL.equals(id)) matterBall += stack.getCount();
        }
        if (crystal < 8 || singularity < 8 || matterBall < 64) {
            return;
        }
        consume(items, SHATTERED_CRYSTAL, 8);
        consume(items, SINGULARITY, 8);
        consume(items, MATTER_BALL, 64);
        level.setBlock(pos, SFBlocks.BLACK_HOLE.get().defaultBlockState(), 3);
        playRitualEffect(level, pos);
    }

    private static void consume(List<ItemEntity> entities, ResourceLocation id, int amount) {
        for (ItemEntity entity : entities) {
            if (amount <= 0) return;
            ItemStack stack = entity.getItem();
            if (!id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) continue;
            int removed = Math.min(amount, stack.getCount());
            stack.shrink(removed);
            amount -= removed;
            if (stack.isEmpty()) entity.discard();
        }
    }

    private static void playRitualEffect(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        // A short, layered reality-collapse burst: a white core flash, a dimensional
        // shockwave, then purple/teal energy and bright motes expanding around it.
        level.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SONIC_BOOM, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 160, 1.35, 1.0, 1.35, 0.8);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 72, 0.9, 0.8, 0.9, 0.16);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 112, 1.1, 0.9, 1.1, 0.28);
        level.sendParticles(ParticleTypes.END_ROD, x, y + 0.15, z, 72, 1.15, 0.65, 1.15, 0.12);
        level.sendParticles(ParticleTypes.GLOW, x, y, z, 48, 0.7, 0.7, 0.7, 0.04);
        level.sendParticles(ParticleTypes.WITCH, x, y, z, 64, 1.2, 1.0, 1.2, 0.18);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 0.62F);
        level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.5F, 0.72F);
    }

    private static boolean isAnvil(BlockState state) {
        return state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL);
    }
}
