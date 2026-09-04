package com.oritong.superflatcore.integration.embers;

import com.oritong.superflatcore.SuperFlatCore;
import com.rekindled.embers.EmbersClientEvents;
import com.rekindled.embers.api.EmbersAPI;
import com.rekindled.embers.api.misc.HammerTarget;
import com.rekindled.embers.api.power.IEmberPacketProducer;
import com.rekindled.embers.api.power.IEmberPacketReceiver;
import com.rekindled.embers.api.power.ITargetable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class EmbersMalletIntegration {
    private static final TagKey<Item> CRAFTING_MALLETS = ItemTags.create(new ResourceLocation("gtceu",
            "tools/crafting_mallets"));
    private static boolean registered;

    private EmbersMalletIntegration() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EmbersAPI.registerLinkingHammer((player, hand) -> isCraftingMallet(player.getItemInHand(hand)));
        EmbersAPI.registerHammerTargetGetter(EmbersMalletIntegration::getHammerTarget);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!isCraftingMallet(stack)) {
            return;
        }

        InteractionResult result = useMalletOnEmberTarget(stack, event.getLevel(), event.getPos(), event.getFace());
        if (result.consumesAction()) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static InteractionResult useMalletOnEmberTarget(ItemStack stack, Level level, BlockPos pos,
                                                            Direction clickedFace) {
        CompoundTag nbt = stack.getOrCreateTag();
        BlockEntity tile = level.getBlockEntity(pos);
        if (nbt.contains("targetWorld") && level.dimension().location().toString().equals(nbt.getString("targetWorld"))) {
            BlockPos targetPos = new BlockPos(nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ"));
            BlockEntity targetTile = level.getBlockEntity(targetPos);
            if (targetTile instanceof ITargetable targetable && targetTile instanceof IEmberPacketProducer producer) {
                Direction face = Direction.byName(nbt.getString("targetFace"));
                if (face == null) {
                    return InteractionResult.PASS;
                }
                Vec3 motion = producer.getEmittingDirection(face);
                if (tile instanceof IEmberPacketReceiver receiver && motion != null) {
                    targetable.setTargetPosition(pos, face);
                    receiver.setIncomingDirection(calculateIncomingDirection(pos, targetPos, motion));
                    playHammerSound(level, pos, 1.5f);
                    nbt.remove("targetWorld");
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (tile instanceof IEmberPacketProducer producer && tile instanceof ITargetable) {
            if (clickedFace == null) {
                return InteractionResult.PASS;
            }
            if (producer.getEmittingDirection(clickedFace) == null) {
                return InteractionResult.PASS;
            }
            nbt.putString("targetWorld", level.dimension().location().toString());
            nbt.putString("targetFace", clickedFace.getName());
            nbt.putInt("targetX", pos.getX());
            nbt.putInt("targetY", pos.getY());
            nbt.putInt("targetZ", pos.getZ());
            playHammerSound(level, pos, 1.95f);
            clearClientTarget(level);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static Vec3 calculateIncomingDirection(BlockPos pos, BlockPos targetPos, Vec3 motion) {
        Vec3 hitPos = Vec3.atCenterOf(pos.subtract(targetPos));
        Vec3 oldPos = new Vec3(0.5, 0.5, 0.5);
        Vec3 newPos = oldPos.add(motion);
        for (int i = 0; i <= 80; ++i) {
            Vec3 targetVector = hitPos.subtract(newPos);
            double length = targetVector.length();
            targetVector = targetVector.scale(0.3 / length);
            double weight = 0;
            if (length <= 3) {
                weight = 0.9 * ((3.0 - length) / 3.0);
                if (length <= 0.2) {
                    break;
                }
            }
            motion = new Vec3(
                    (0.9 - weight) * motion.x + (0.1 + weight) * targetVector.x,
                    (0.9 - weight) * motion.y + (0.1 + weight) * targetVector.y,
                    (0.9 - weight) * motion.z + (0.1 + weight) * targetVector.z);
            newPos = oldPos.add(motion);
            oldPos = newPos;
        }
        return motion;
    }

    private static HammerTarget getHammerTarget(Player player) {
        HammerTarget mainHandTarget = getHammerTarget(player, player.getMainHandItem());
        if (mainHandTarget != null) {
            return mainHandTarget;
        }
        return getHammerTarget(player, player.getOffhandItem());
    }

    private static HammerTarget getHammerTarget(Player player, ItemStack stack) {
        if (!isCraftingMallet(stack) || !stack.hasTag()) {
            return null;
        }
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("targetWorld")
                || !player.level().dimension().location().toString().equals(nbt.getString("targetWorld"))) {
            return null;
        }
        Direction face = Direction.byName(nbt.getString("targetFace"));
        if (face == null) {
            return null;
        }
        return new HammerTarget(new BlockPos(nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ")), face);
    }

    public static boolean isCraftingMallet(ItemStack stack) {
        return !stack.isEmpty() && stack.is(CRAFTING_MALLETS);
    }

    private static void playHammerSound(Level level, BlockPos pos, float pitchBase) {
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ANVIL_LAND,
                SoundSource.BLOCKS, 0.5f, pitchBase + level.random.nextFloat() * 0.2f, false);
    }

    private static void clearClientTarget(Level level) {
        if (level.isClientSide) {
            clearClientTarget();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clearClientTarget() {
        EmbersClientEvents.lastTarget = null;
    }
}
