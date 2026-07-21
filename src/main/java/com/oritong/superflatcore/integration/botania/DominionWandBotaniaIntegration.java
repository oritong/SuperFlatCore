package com.oritong.superflatcore.integration.botania;

import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.CoordBoundItem;
import vazkii.botania.common.block.dispenser.WandBehavior;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.forge.CapabilityUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DominionWandBotaniaIntegration {
    private static final ResourceLocation DOMINION_WAND_ID = new ResourceLocation("ars_nouveau", "dominion_wand");
    private static final ResourceLocation BOTANIA_TWIG_WAND_ID = new ResourceLocation("botania", "twig_wand");
    private static final String DOMINION_WAND_LANG = "item.ars_nouveau.dominion_wand";
    private static final String ARS_DOMINION_TAG = "an_dominion_wand";
    private static final String ARS_STRICT_MODE_TAG = "mode";
    private static final Map<UUID, Long> LAST_USE_ON_TICK = new ConcurrentHashMap<>();

    private DominionWandBotaniaIntegration() {
    }

    public static boolean isDominionWand(ItemStack stack) {
        return !stack.isEmpty() && isDominionWandItem(stack.getItem());
    }

    public static boolean isDominionWandItem(Item item) {
        return DOMINION_WAND_ID.equals(ForgeRegistries.ITEMS.getKey(item));
    }

    public static InteractionResult useOn(UseOnContext context) {
        if (!isDominionWand(context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        markUseOn(context);
        return botaniaWandDelegate().m_6225_(context);
    }

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canToggleBotaniaMode(player, stack)) {
            return InteractionResultHolder.pass(stack);
        }
        clearDominionStrictMode(stack);
        if (level.isClientSide) {
            WandOfTheForestItem.setBindMode(stack, !WandOfTheForestItem.getBindMode(stack));
        }
        return botaniaWandDelegate().m_7203_(level, player, hand);
    }

    public static void clearDominionStrictMode(ItemStack stack) {
        if (!isDominionWand(stack)) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ARS_DOMINION_TAG)) {
            return;
        }
        CompoundTag dominionTag = tag.getCompound(ARS_DOMINION_TAG);
        if (dominionTag.getBoolean(ARS_STRICT_MODE_TAG)) {
            dominionTag.putBoolean(ARS_STRICT_MODE_TAG, false);
            tag.put(ARS_DOMINION_TAG, dominionTag);
        }
    }

    public static Component getNameWithBotaniaMode(ItemStack stack, Component fallbackName) {
        MutableComponent name = stack.hasCustomHoverName()
                ? fallbackName.copy()
                : Component.translatable(DOMINION_WAND_LANG);
        return name.append(" (")
                .append(Component.translatable(WandOfTheForestItem.getModeString(stack))
                        .withStyle(ChatFormatting.DARK_GREEN))
                .append(")");
    }

    public static boolean canToggleBotaniaMode(Player player, ItemStack stack) {
        return isDominionWand(stack)
                && player.isSecondaryUseActive()
                && !wasUsedOnBlockThisTick(player)
                && isLookingAtAir(player);
    }

    public static boolean isLookingAtAir(Player player) {
        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        return hitResult == null || hitResult.getType() == HitResult.Type.MISS;
    }

    public static void markUseOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && isDominionWand(context.getItemInHand())) {
            LAST_USE_ON_TICK.put(player.getUUID(), context.getLevel().getGameTime());
        }
    }

    public static boolean wasUsedOnBlockThisTick(Player player) {
        Long tick = LAST_USE_ON_TICK.get(player.getUUID());
        return tick != null && tick == player.level().getGameTime();
    }

    public static void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (isDominionWand(stack)) {
            botaniaWandDelegate().m_6883_(stack, level, entity, slot, selected);
        }
    }

    public static void appendBotaniaTooltip(ItemStack stack, List<Component> tooltip) {
        if (isDominionWand(stack)) {
            tooltip.add(Component.translatable(WandOfTheForestItem.getModeString(stack)).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Item dominionWand = ForgeRegistries.ITEMS.getValue(DOMINION_WAND_ID);
            if (dominionWand != null) {
                DispenserBlock.registerBehavior(dominionWand, new WandBehavior());
            }
        });
    }

    public static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (isDominionWand(stack)) {
            CoordBoundItem boundItem = new WandOfTheForestItem.CoordBoundItemImpl(stack);
            event.addCapability(SuperFlatCore.id("dominion_wand_coord_bound_item"),
                    CapabilityUtil.makeProvider(BotaniaForgeCapabilities.COORD_BOUND_ITEM, boundItem));
        }
    }

    private static WandOfTheForestItem botaniaWandDelegate() {
        Item item = ForgeRegistries.ITEMS.getValue(BOTANIA_TWIG_WAND_ID);
        if (item instanceof WandOfTheForestItem wand) {
            return wand;
        }
        throw new IllegalStateException("Botania twig wand is not registered");
    }
}
