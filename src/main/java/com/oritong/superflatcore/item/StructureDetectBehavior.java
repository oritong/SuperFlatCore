package com.oritong.superflatcore.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.error.PatternError;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.error.SinglePredicateError;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.oritong.superflatcore.registry.SFItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * Displays the last failed position from a GTCEu multiblock pattern check and stores it on the tool for client-side
 * highlighting. This mirrors GTOCore's structure detection workflow while remaining compatible with GTM 7.5.3.
 */
public final class StructureDetectBehavior extends TooltipBehavior implements IToolBehavior, IInteractionItem {
    public static final StructureDetectBehavior INSTANCE = new StructureDetectBehavior(lines -> {
        lines.add(Component.translatable("item.superflatcore.structure_detect.tooltip.0"));
        lines.add(Component.translatable("item.superflatcore.structure_detect.tooltip.1"));
        lines.add(Component.translatable("item.superflatcore.structure_detect.tooltip.2"));
    });

    private static final String ERROR_POS_TAG = "error_pos";
    private static final String POSITIONS_TAG = "pos";
    private static final String X_TAG = "x";
    private static final String Y_TAG = "y";
    private static final String Z_TAG = "z";

    private StructureDetectBehavior(Consumer<List<Component>> tooltips) {
        super(tooltips);
    }

    public static BlockPos[] getErrorPositions(ItemStack stack) {
        CompoundTag rootTag = stack.getTag();
        if (rootTag == null || !rootTag.contains(ERROR_POS_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag errorTag = rootTag.getCompound(ERROR_POS_TAG);
        if (!errorTag.contains(POSITIONS_TAG, Tag.TAG_LIST)) {
            return null;
        }

        ListTag positions = errorTag.getList(POSITIONS_TAG, Tag.TAG_COMPOUND);
        if (positions.isEmpty()) {
            return null;
        }

        BlockPos[] result = new BlockPos[positions.size()];
        for (int index = 0; index < positions.size(); index++) {
            CompoundTag position = positions.getCompound(index);
            result[index] = new BlockPos(position.getInt(X_TAG), position.getInt(Y_TAG), position.getInt(Z_TAG));
        }
        return result;
    }

    public static boolean isItem(ItemStack stack) {
        return !stack.isEmpty() && stack.is(SFItems.STRUCTURE_DETECT.get());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }

        clearErrorPositions(stack);
        if (!(MetaMachine.getMachine(level, context.getClickedPos()) instanceof IMultiController controller)) {
            return InteractionResult.PASS;
        }

        if (controller.isFormed()) {
            player.sendSystemMessage(Component.translatable("gtceu.top.valid_structure")
                    .withStyle(ChatFormatting.GREEN));
            return InteractionResult.SUCCESS;
        }

        MultiblockState state = controller.getMultiblockState();
        if (state.error != null) {
            showError(player, state.error, stack);
        }
        return InteractionResult.SUCCESS;
    }

    private static void showError(Player player, PatternError error, ItemStack stack) {
        if (error instanceof PatternStringError) {
            player.sendSystemMessage(error.getErrorInfo());
        } else {
            BlockPos position = error.getPos();
            player.sendSystemMessage(Component.translatable("item.superflatcore.structure_detect.error.header",
                    position.getX(), position.getY(), position.getZ()).withStyle(ChatFormatting.RED));
            for (List<ItemStack> candidates : error.getCandidates()) {
                if (!candidates.isEmpty()) {
                    player.sendSystemMessage(Component.literal(" - ").append(candidates.get(0).getHoverName()));
                }
            }
            if (error instanceof SinglePredicateError) {
                player.sendSystemMessage(error.getErrorInfo());
            }
        }
        addErrorPosition(stack, error.getPos());
    }

    private static void clearErrorPositions(ItemStack stack) {
        CompoundTag errorTag = stack.getOrCreateTagElement(ERROR_POS_TAG);
        errorTag.put(POSITIONS_TAG, new ListTag());
    }

    private static void addErrorPosition(ItemStack stack, BlockPos position) {
        if (position == null) {
            return;
        }

        CompoundTag errorTag = stack.getOrCreateTagElement(ERROR_POS_TAG);
        ListTag positions = errorTag.getList(POSITIONS_TAG, Tag.TAG_COMPOUND);
        CompoundTag positionTag = new CompoundTag();
        positionTag.putInt(X_TAG, position.getX());
        positionTag.putInt(Y_TAG, position.getY());
        positionTag.putInt(Z_TAG, position.getZ());
        positions.add(positionTag);
        errorTag.put(POSITIONS_TAG, positions);
    }
}
