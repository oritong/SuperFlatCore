package com.oritong.superflatcore.integration.jade;

import com.haoict.tiab.common.entities.TimeAcceleratorEntity;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.event.MachineSoulDispleasureManager;

import java.util.List;
import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.ITooltip;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin("tiab")
public class TiabJadePlugin implements IWailaPlugin {
    private static final ResourceLocation TIAB_INFO = SuperFlatCore.id("tiab_info");
    private static final String TAG_HAS_ACCELERATOR = "SuperFlatCoreTiabAccelerated";
    private static final String TAG_TIME_RATE = "SuperFlatCoreTiabTimeRate";
    private static final String TAG_REMAINING_TIME = "SuperFlatCoreTiabRemainingTime";
    private static final String TAG_HAS_SLOWDOWN = "SuperFlatCoreTiabSlowdown";
    private static final String TAG_SLOWDOWN_MULTIPLIER = "SuperFlatCoreTiabSlowdownMultiplier";
    private static final String TAG_SLOWDOWN_REMAINING_TIME = "SuperFlatCoreTiabSlowdownRemainingTime";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TiabBlockDataProvider.INSTANCE, BlockEntity.class);
        registration.registerEntityDataProvider(TiabEntityDataProvider.INSTANCE, TimeAcceleratorEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(TIAB_INFO, true);
        registration.markAsClientFeature(TIAB_INFO);
        registration.registerBlockComponent(TiabTooltipProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(TiabTooltipProvider.INSTANCE, TimeAcceleratorEntity.class);
    }

    private static final class TiabTooltipProvider implements IBlockComponentProvider, IEntityComponentProvider {
        private static final TiabTooltipProvider INSTANCE = new TiabTooltipProvider();

        @Override
        public ResourceLocation getUid() {
            return TIAB_INFO;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (config.get(TIAB_INFO)) {
                appendInfo(tooltip, accessor.getServerData());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (config.get(TIAB_INFO)) {
                appendInfo(tooltip, accessor.getServerData());
            }
        }

        private static void appendInfo(ITooltip tooltip, CompoundTag data) {
            if (data.getBoolean(TAG_HAS_ACCELERATOR)) {
                int multiplier = Math.max(1, data.getInt(TAG_TIME_RATE)) * 2;
                String remainingTime = String.format(Locale.ROOT, "%.1f",
                        Math.max(0, data.getInt(TAG_REMAINING_TIME)) / 20.0D);
                tooltip.add(Component.translatable("tooltip.superflatcore.tiab.multiplier", multiplier));
                tooltip.add(Component.translatable("tooltip.superflatcore.tiab.remaining_time", remainingTime));
                return;
            }
            if (!data.getBoolean(TAG_HAS_SLOWDOWN)) {
                return;
            }

            String multiplier = Double.toString(data.getDouble(TAG_SLOWDOWN_MULTIPLIER));
            String remainingTime = String.format(Locale.ROOT, "%.1f",
                    Math.max(0, data.getInt(TAG_SLOWDOWN_REMAINING_TIME)) / 20.0D);
            tooltip.add(Component.translatable("tooltip.superflatcore.tiab.multiplier", multiplier)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.superflatcore.tiab.remaining_time", remainingTime)
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static final class TiabBlockDataProvider implements IServerDataProvider<BlockAccessor> {
        private static final TiabBlockDataProvider INSTANCE = new TiabBlockDataProvider();

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            TimeAcceleratorEntity accelerator = findAccelerator(accessor.getLevel(), accessor.getPosition());
            if (accelerator != null) {
                writeInfo(data, accelerator);
            } else if (accessor.getLevel() instanceof ServerLevel serverLevel) {
                MachineSoulDispleasureManager.SlowdownInfo slowdown =
                        MachineSoulDispleasureManager.getInfo(serverLevel, accessor.getPosition());
                if (slowdown != null) {
                    writeSlowdownInfo(data, slowdown);
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return TIAB_INFO;
        }
    }

    private static final class TiabEntityDataProvider implements IServerDataProvider<EntityAccessor> {
        private static final TiabEntityDataProvider INSTANCE = new TiabEntityDataProvider();

        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (accessor.getEntity() instanceof TimeAcceleratorEntity accelerator) {
                if (accelerator.getTimeRate() > 0) {
                    writeInfo(data, accelerator);
                } else if (accelerator.getTimeRate() < 0) {
                    writeSlowdownInfo(data, getVisualSlowdownInfo(accelerator));
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return TIAB_INFO;
        }
    }

    private static TimeAcceleratorEntity findAccelerator(Level level, BlockPos pos) {
        List<TimeAcceleratorEntity> accelerators = level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos));
        return accelerators.stream()
                .filter(accelerator -> accelerator.getTimeRate() > 0)
                .findFirst()
                .orElse(null);
    }

    private static void writeInfo(CompoundTag data, TimeAcceleratorEntity accelerator) {
        data.putBoolean(TAG_HAS_ACCELERATOR, true);
        data.putInt(TAG_TIME_RATE, accelerator.getTimeRate());
        data.putInt(TAG_REMAINING_TIME, accelerator.getRemainingTime());
    }

    private static void writeSlowdownInfo(CompoundTag data, MachineSoulDispleasureManager.SlowdownInfo slowdown) {
        data.putBoolean(TAG_HAS_SLOWDOWN, true);
        data.putDouble(TAG_SLOWDOWN_MULTIPLIER, slowdown.multiplier());
        data.putInt(TAG_SLOWDOWN_REMAINING_TIME, slowdown.remainingTicks());
    }

    private static MachineSoulDispleasureManager.SlowdownInfo getVisualSlowdownInfo(
            TimeAcceleratorEntity accelerator
    ) {
        double multiplier = accelerator.getTimeRate() == -2 ? 0.25D : 0.5D;
        return new MachineSoulDispleasureManager.SlowdownInfo(multiplier, accelerator.getRemainingTime());
    }
}
