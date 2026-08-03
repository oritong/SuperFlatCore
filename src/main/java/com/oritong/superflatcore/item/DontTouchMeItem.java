package com.oritong.superflatcore.item;

import com.oritong.superflatcore.network.SFNetwork;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DontTouchMeItem extends Item {
    public static final int MAX_STAGE = 5;
    public static final int RESET_TICKS = 10 * 20;
    public static final String STAGE_KEY = "DontTouchMeStage";
    public static final String LAST_USE_KEY = "DontTouchMeLastUse";

    public DontTouchMeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return new InteractionResultHolder<>(activate(level, player, hand, stack), stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return activate(context.getLevel(), player, context.getHand(), context.getItemInHand());
    }

    public static int getStage(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0;
        }
        return Mth.clamp(stack.getOrCreateTag().getInt(STAGE_KEY), 0, MAX_STAGE);
    }

    public static long getLastUseTick(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0L;
        }
        return stack.getOrCreateTag().getLong(LAST_USE_KEY);
    }

    public static void resetStage(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        stack.getOrCreateTag().putInt(STAGE_KEY, 0);
        stack.getOrCreateTag().putLong(LAST_USE_KEY, 0L);
    }

    private static void setStage(ItemStack stack, int stage, long gameTime) {
        stack.getOrCreateTag().putInt(STAGE_KEY, Mth.clamp(stage, 0, MAX_STAGE));
        stack.getOrCreateTag().putLong(LAST_USE_KEY, gameTime);
    }

    private InteractionResult activate(Level level, Player player, InteractionHand hand, ItemStack stack) {
        int stage = getStage(stack);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (stage >= MAX_STAGE) {
            SFNetwork.sendDontTouchMeStage(serverPlayer, MAX_STAGE + 1);
            return InteractionResult.SUCCESS;
        }

        int nextStage = stage + 1;
        setStage(stack, nextStage, level.getGameTime());
        applyServerEffects(serverPlayer, nextStage);
        SFNetwork.sendDontTouchMeStage(serverPlayer, nextStage);
        serverPlayer.getInventory().setChanged();
        serverPlayer.inventoryMenu.broadcastChanges();
        player.swing(hand, true);
        return InteractionResult.CONSUME;
    }

    private static void applyServerEffects(ServerPlayer player, int stage) {
        if (stage == 2) {
            playGlassBreak(player, 1.0F);
        } else if (stage == 3) {
            playGlassBreak(player, 2.0F);
        } else if (stage == 4) {
            playGlassBreak(player, 4.0F);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, RESET_TICKS, 0, false, false, true));
        } else if (stage == 5) {
            player.removeEffect(MobEffects.CONFUSION);
        }
    }

    private static void playGlassBreak(ServerPlayer player, float volume) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS,
                volume, 0.95F + player.getRandom().nextFloat() * 0.1F);
    }

}
