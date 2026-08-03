package com.oritong.superflatcore.event;

import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class FireEssenceEvents {
    private static final ResourceLocation FIRE_ESSENCE_ID = new ResourceLocation("ars_nouveau", "fire_essence");

    private FireEssenceEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleUse(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleUse(event);
    }

    private static void handleUse(PlayerInteractEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isFireEssence(stack)) {
            return;
        }

        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel) {
            launchFireball(serverLevel, player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
        event.setCanceled(true);
    }

    private static boolean isFireEssence(ItemStack stack) {
        return !stack.isEmpty() && FIRE_ESSENCE_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    private static void launchFireball(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        SmallFireball fireball = new SmallFireball(level, player, look.x, look.y, look.z);
        fireball.setPos(
                player.getX() + look.x * 0.8D,
                player.getEyeY() - 0.1D + look.y * 0.3D,
                player.getZ() + look.z * 0.8D
        );
        fireball.shoot(look.x, look.y, look.z, 1.5F, 0.0F);
        level.addFreshEntity(fireball);
    }
}
