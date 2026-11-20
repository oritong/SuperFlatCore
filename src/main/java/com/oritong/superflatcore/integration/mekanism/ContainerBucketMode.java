package com.oritong.superflatcore.integration.mekanism;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.common.item.ItemFluidContainer;
import com.oritong.superflatcore.SuperFlatCore;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.smeltery.item.TankItem;

/** Mekanism-style bucket controls backed by each container's own fluid capability. */
@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContainerBucketMode {
    private static final String MODE_TAG = "superflatcore:bucket_mode";

    private ContainerBucketMode() {}

    public static boolean supports(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof TankItem
                || stack.getItem() instanceof DrumMachineItem
                || stack.getItem() instanceof QuantumTankMachineItem
                || stack.getItem() instanceof ComponentItem component
                && component.getComponents().stream().anyMatch(ItemFluidContainer.class::isInstance);
    }

    public static boolean supportsSlot(ItemStack stack, EquipmentSlot slot) {
        return (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) && supports(stack);
    }

    public static boolean isEnabled(ItemStack stack) {
        return supports(stack) && stack.hasTag() && stack.getTag().getBoolean(MODE_TAG);
    }

    public static void changeMode(Player player, ItemStack stack, int shift, IModeItem.DisplayChange display) {
        if (!supports(stack) || (shift & 1) == 0) return;
        boolean enabled = !isEnabled(stack);
        if (enabled) {
            stack.getOrCreateTag().putBoolean(MODE_TAG, true);
        } else {
            stack.removeTagKey(MODE_TAG);
        }
        display.sendMessage(player, () -> MekanismLang.BUCKET_MODE.translate(OnOff.of(enabled, true)));
    }

    public static Component getScrollText(ItemStack stack) {
        return MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(isEnabled(stack), true));
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isEnabled(event.getItemStack())) {
            // Like Mekanism's useOn(PASS): allow block interactions, but never place the tank itself.
            event.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!isEnabled(event.getItemStack())) return;
        event.setCanceled(true);
        event.setCancellationResult(useBucket(event.getLevel(), event.getEntity(), event.getHand()));
    }

    private static InteractionResult useBucket(Level level, Player player, InteractionHand hand) {
        if (player.isSpectator()) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(player.getBlockReach()));
        ClipContext.Fluid clip = player.isShiftKeyDown() ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY;
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, clip, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        if (!level.mayInteract(player, hit.getBlockPos())) return InteractionResult.FAIL;

        // Never fill/drain a stacked capability: GT requires a count of one and Tinkers scales by count.
        ItemStack single = held.copyWithCount(1);
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(single).orElse(null);
        if (handler == null) return InteractionResult.FAIL;
        return player.isShiftKeyDown()
                ? place(level, player, hand, hit, held, handler)
                : pickUp(level, player, hand, hit, held, single, handler);
    }

    private static InteractionResult pickUp(Level level, Player player, InteractionHand hand, BlockHitResult hit,
                                            ItemStack held, ItemStack single, IFluidHandlerItem handler) {
        BlockPos pos = hit.getBlockPos();
        FluidState state = level.getFluidState(pos);
        if (state.isEmpty() || !state.isSource()) return InteractionResult.PASS;
        if (!player.mayUseItemAt(pos, hit.getDirection(), held)) return InteractionResult.FAIL;
        FluidStack bucket = new FluidStack(state.getType(), FluidType.BUCKET_VOLUME);
        if (handler.fill(bucket, FluidAction.SIMULATE) != FluidType.BUCKET_VOLUME) return InteractionResult.FAIL;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        FluidActionResult result = FluidUtil.tryPickUpFluid(single, player, level, pos, hit.getDirection());
        if (!result.isSuccess()) return InteractionResult.FAIL;
        replaceOneContainer(player, hand, held, result.getResult());
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        player.awardStat(Stats.ITEM_USED.get(held.getItem()));
        return InteractionResult.CONSUME;
    }

    private static InteractionResult place(Level level, Player player, InteractionHand hand, BlockHitResult hit,
                                           ItemStack held, IFluidHandlerItem handler) {
        FluidStack bucket = handler.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (bucket.getAmount() != FluidType.BUCKET_VOLUME) return InteractionResult.FAIL;
        BlockPos pos = hit.getBlockPos();
        if (!canContainFluid(level, pos, bucket.getFluid())) pos = pos.relative(hit.getDirection());
        if (!canContainFluid(level, pos, bucket.getFluid())
                || !bucket.getFluid().getFluidType().canBePlacedInLevel(level, pos, bucket)
                || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, hit.getDirection(), held)) return InteractionResult.FAIL;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Only the server changes fluid blocks, so include the user in the placement sound broadcast.
        if (!WorldUtils.tryPlaceContainedLiquid(null, level, pos, bucket, null)) return InteractionResult.FAIL;
        if (!player.isCreative()) {
            handler.drain(bucket, FluidAction.EXECUTE);
            replaceOneContainer(player, hand, held, handler.getContainer());
        }
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        player.awardStat(Stats.ITEM_USED.get(held.getItem()));
        return InteractionResult.CONSUME;
    }

    private static boolean canContainFluid(Level level, BlockPos pos, Fluid fluid) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced(fluid)
                || state.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(level, pos, state, fluid);
    }

    private static void replaceOneContainer(Player player, InteractionHand hand, ItemStack held, ItemStack result) {
        if (held.getCount() == 1) {
            player.setItemInHand(hand, result);
        } else {
            held.shrink(1);
            ItemHandlerHelper.giveItemToPlayer(player, result);
        }
    }
}
