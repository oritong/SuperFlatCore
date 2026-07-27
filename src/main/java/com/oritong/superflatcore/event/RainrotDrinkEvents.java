package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFMobEffects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class RainrotDrinkEvents {
    private static final Set<ResourceLocation> FIVE_PEBBSI_IDS = Set.of(
            new ResourceLocation("rainrot", "five_pebbsi_classic"),
            new ResourceLocation("rainrot", "five_pebbsi_rubicon"),
            new ResourceLocation("rainrot", "five_pebbsi_crystal"));
    private static final ResourceLocation MAGNALIUM_FOIL_ID = new ResourceLocation("gtceu", "magnalium_foil");
    private static final ResourceLocation ALUMINIUM_RING_ID = new ResourceLocation("gtceu", "aluminium_ring");
    private static final int TICKS_PER_SECOND = 20;

    private RainrotDrinkEvents() {}

    @SubscribeEvent
    public static void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player) || !isFivePebbsi(event.getItem())) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * TICKS_PER_SECOND, 0));
        player.addEffect(new MobEffectInstance(SFMobEffects.MACHINE_SOUL_JOY.get(), 180 * TICKS_PER_SECOND, 0));
        giveItem(player, MAGNALIUM_FOIL_ID);
        giveItem(player, ALUMINIUM_RING_ID);
    }

    private static boolean isFivePebbsi(ItemStack stack) {
        return !stack.isEmpty() && FIVE_PEBBSI_IDS.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    private static void giveItem(ServerPlayer player, ResourceLocation itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            player.displayClientMessage(Component.literal("Missing item: " + itemId), true);
            return;
        }

        ItemStack stack = new ItemStack(item);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
