package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.blackhole.BlackHoleBlock;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets Mekanism's cardboard box pick up the black hole despite its otherwise
 * unbreakable block properties. This runs in the box item's onItemUseFirst,
 * so the box gets priority over the black hole block's own interaction.
 */
@Mixin(value = ItemBlockCardboardBox.class, remap = false)
public abstract class MekanismCardboardBoxMixin {

    @Inject(method = "onItemUseFirst", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$pickUpBlackHole(ItemStack stack, UseOnContext context,
                                                CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        if (player == null || stack.isEmpty()) {
            return;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlackHoleBlock) || player.isShiftKeyDown()) {
            return;
        }

        ItemBlockCardboardBox boxItem = (ItemBlockCardboardBox) (Object) this;
        if (boxItem.getBlockData(level, stack) != null) {
            return;
        }
        if (!canReplace(level, player, pos, context.getClickedFace(), state, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile != null && (MekanismTags.TileEntityTypes.CARDBOARD_BLACKLIST_LOOKUP.contains(tile.getType())
                || !ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile))) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        if (!level.isClientSide) {
            BlockCardboardBox.BlockData data = new BlockCardboardBox.BlockData(state);
            if (tile != null) {
                data.tileTag = tile.saveWithFullMetadata();
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            CommonWorldTickHandler.monitoringCardboardBox = true;
            try {
                level.setBlockAndUpdate(pos, MekanismBlocks.CARDBOARD_BOX.getBlock().defaultBlockState()
                        .setValue(BlockStateHelper.storageProperty, true));
            } finally {
                CommonWorldTickHandler.monitoringCardboardBox = false;
            }
            TileEntityCardboardBox cardboardBox = WorldUtils.getTileEntity(TileEntityCardboardBox.class, level, pos);
            if (cardboardBox != null) {
                cardboardBox.storedData = data;
                cardboardBox.setChanged();
            }
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }

    private static boolean canReplace(Level level, Player player, BlockPos pos, Direction side,
                                      BlockState state, ItemStack stack) {
        return level.mayInteract(player, pos)
                && player.mayUseItemAt(pos.relative(side), side, stack)
                && !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))
                && !state.is(MekanismTags.Blocks.CARDBOARD_BLACKLIST)
                && !MekanismConfig.general.cardboardModBlacklist.get()
                .contains(RegistryUtils.getNamespace(state.getBlock()));
    }
}
