package com.oritong.superflatcore.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.entity.EntityAutoPlayer;

import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EmployeeCompanionItem extends Item {
    private static final String MODE_TAG = "SuperFlatCoreEmployeeCompanionMode";

    public EmployeeCompanionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            EntityAutoPlayer autoPlayer = Mino.ENTITY_AUTO_PLAYER.get().create(serverLevel);
            if (autoPlayer == null) {
                return InteractionResultHolder.fail(stack);
            }

            Vec3 spawnPos = getSpawnPos(player);
            autoPlayer.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);
            autoPlayer.setYHeadRot(player.getYRot());
            autoPlayer.setCustomName(Component.literal(EmployeeCompanionNames.random(player.getRandom())));
            autoPlayer.setCustomNameVisible(true);
            applyMode(autoPlayer, CompanionMode.NORMAL, player.getRandom());
            serverLevel.addFreshEntity(autoPlayer);
            sendSummonedMessage(player, autoPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void handleAutoPlayerInteraction(Player player, EntityAutoPlayer autoPlayer) {
        if (player.isShiftKeyDown()) {
            playDismissParticles(autoPlayer);
            autoPlayer.remove(Entity.RemovalReason.KILLED);
            player.displayClientMessage(Component.translatable(
                    "message.superflatcore.employee_companion.removed"), false);
            return;
        }

        CompanionMode nextMode = nextMode(getMode(autoPlayer));
        applyMode(autoPlayer, nextMode, player.getRandom());
        sendModeMessage(player, autoPlayer, nextMode);
    }

    private static Vec3 getSpawnPos(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            horizontal = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        }
        horizontal = horizontal.normalize().scale(1.5D);
        return new Vec3(player.getX() + horizontal.x, player.getY(), player.getZ() + horizontal.z);
    }

    private static CompanionMode getMode(EntityAutoPlayer autoPlayer) {
        String modeId = autoPlayer.getPersistentData().getString(MODE_TAG);
        for (CompanionMode mode : CompanionMode.values()) {
            if (mode.id.equals(modeId)) {
                return mode;
            }
        }
        if (autoPlayer.autoPlayer.aiNoWin) {
            return CompanionMode.SUPER_NOOB;
        }
        if (autoPlayer.autoPlayer.aiForgetChance <= 0.001F && autoPlayer.autoPlayer.aiNoDelay >= 2) {
            return CompanionMode.INFERNO;
        }
        return CompanionMode.NORMAL;
    }

    private static CompanionMode nextMode(CompanionMode mode) {
        return switch (mode) {
            case NORMAL -> CompanionMode.SUPER_NOOB;
            case SUPER_NOOB -> CompanionMode.INFERNO;
            case INFERNO -> CompanionMode.NORMAL;
        };
    }

    private static void applyMode(EntityAutoPlayer autoPlayer, CompanionMode mode, RandomSource random) {
        autoPlayer.setActive(true);
        autoPlayer.getPersistentData().putString(MODE_TAG, mode.id);
        switch (mode) {
            case NORMAL -> {
                autoPlayer.autoPlayer.aiNoWin = false;
                autoPlayer.autoPlayer.aiNoPlayerDraw = false;
                autoPlayer.autoPlayer.aiForgetChance = rollNormalForgetChance(random);
                autoPlayer.autoPlayer.aiNoDelay = 0;
                autoPlayer.autoPlayer.aiStartGame = false;
            }
            case SUPER_NOOB -> {
                autoPlayer.autoPlayer.aiNoWin = true;
                autoPlayer.autoPlayer.aiNoPlayerDraw = true;
                autoPlayer.autoPlayer.aiForgetChance = 1.0F;
                autoPlayer.autoPlayer.aiNoDelay = 0;
                autoPlayer.autoPlayer.aiStartGame = false;
            }
            case INFERNO -> {
                autoPlayer.autoPlayer.aiNoWin = false;
                autoPlayer.autoPlayer.aiNoPlayerDraw = false;
                autoPlayer.autoPlayer.aiForgetChance = 0.0F;
                autoPlayer.autoPlayer.aiNoDelay = 2;
                autoPlayer.autoPlayer.aiStartGame = true;
            }
        }
    }

    private static float rollNormalForgetChance(RandomSource random) {
        return random.nextFloat() * 0.2F;
    }

    private static void playDismissParticles(EntityAutoPlayer autoPlayer) {
        if (autoPlayer.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    autoPlayer.getX(), autoPlayer.getY() + autoPlayer.getBbHeight() * 0.5D, autoPlayer.getZ(),
                    20, autoPlayer.getBbWidth() * 0.5D, autoPlayer.getBbHeight() * 0.25D,
                    autoPlayer.getBbWidth() * 0.5D, 0.02D);
        }
    }

    private static void sendSummonedMessage(Player player, EntityAutoPlayer autoPlayer) {
        player.displayClientMessage(Component.translatable(
                "message.superflatcore.employee_companion.summoned",
                Component.translatable(CompanionMode.NORMAL.translationKey)), false);
    }

    private static void sendModeMessage(Player player, EntityAutoPlayer autoPlayer, CompanionMode mode) {
        player.displayClientMessage(Component.translatable(
                "message.superflatcore.employee_companion.mode",
                Component.translatable(mode.translationKey)), false);
    }

    private enum CompanionMode {
        NORMAL("normal", "message.superflatcore.employee_companion.mode.normal"),
        SUPER_NOOB("super_noob", "message.superflatcore.employee_companion.mode.super_noob"),
        INFERNO("inferno", "message.superflatcore.employee_companion.mode.inferno");

        private final String id;
        private final String translationKey;

        CompanionMode(String id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }
    }
}
