package com.oritong.superflatcore.event;

import com.magorage.tiab.api.ITimeInABottleAPI;
import com.magorage.tiab.api.TiabProvider;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class TimeCrystalTiabEvents {
    private static final ResourceLocation TIME_CRYSTAL_ID = new ResourceLocation("beyonddimensions",
            "shattered_space_time_crystallization");
    private static final int TICKS_PER_SECOND = 20;
    private static final int ADDED_SECONDS = 15 * 60;
    private static final int ADDED_TICKS = ADDED_SECONDS * TICKS_PER_SECOND;
    private static TiabProvider tiabProvider;
    private static ITimeInABottleAPI tiabApi;

    private TimeCrystalTiabEvents() {}

    public static void requestTiabApi() {
        if (tiabProvider != null) {
            return;
        }
        tiabProvider = new TiabProvider(api -> tiabApi = api);
        InterModComms.sendTo(ITimeInABottleAPI.IMC.MOD_ID, ITimeInABottleAPI.IMC.GET_API, () -> tiabProvider);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleCrystalUse(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleCrystalUse(event);
    }

    private static void handleCrystalUse(PlayerInteractEvent event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
                || !isTimeCrystal(event.getItemStack())) {
            return;
        }

        Level level = player.level();
        if (level.isClientSide) {
            return;
        }

        InteractionResult result = addTime((ServerPlayer) player, event.getItemStack());
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    private static InteractionResult addTime(ServerPlayer player, ItemStack crystalStack) {
        if (tiabApi == null || !tiabApi.canUse()) {
            sendStatus(player, "message.superflatcore.time_crystal.api_unavailable");
            return InteractionResult.SUCCESS;
        }

        ItemStack bottle = findTimeInABottle(player);
        if (bottle.isEmpty()) {
            sendStatus(player, "message.superflatcore.time_crystal.no_bottle");
            return InteractionResult.SUCCESS;
        }

        int before = tiabApi.getStoredTime(bottle);
        tiabApi.setStoredTime(bottle, safeAdd(before, ADDED_TICKS));
        int after = tiabApi.getStoredTime(bottle);
        if (after <= before) {
            sendStatus(player, "message.superflatcore.time_crystal.full");
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            crystalStack.shrink(1);
        }
        player.getInventory().setChanged();
        sendStatus(player, "message.superflatcore.time_crystal.added");
        return InteractionResult.CONSUME;
    }

    private static ItemStack findTimeInABottle(Player player) {
        var tiabItem = tiabApi.getRegistryObject().get();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(tiabItem)) {
                return stack;
            }
        }

        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, tiabItem)
                .map(slotResult -> slotResult.stack().is(tiabItem) ? slotResult.stack() : ItemStack.EMPTY)
                .orElse(ItemStack.EMPTY);
    }

    private static boolean isTimeCrystal(ItemStack stack) {
        return !stack.isEmpty() && TIME_CRYSTAL_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    private static int safeAdd(int value, int addition) {
        if (value > Integer.MAX_VALUE - addition) {
            return Integer.MAX_VALUE;
        }
        return value + addition;
    }

    private static void sendStatus(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }
}
