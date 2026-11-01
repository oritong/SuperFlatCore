package com.oritong.superflatcore.item;

import com.oritong.superflatcore.registry.SFBlockEntities;
import com.oritong.superflatcore.block.entity.RadioactiveFluidTankBlockEntity;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public final class RadioactiveFluidTankItem extends BlockItem {
    public RadioactiveFluidTankItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction()) {
            tryMultiPlace(context);
        }
        return result;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player,
                                                 ItemStack stack, BlockState state) {
        CompoundTag nbt = stack.getTagElement("BlockEntityTag");
        if (nbt != null) {
            nbt.remove("Luminosity");
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            if (nbt.contains("TankContent")) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("TankContent"));
                if (!fluid.isEmpty()) {
                    fluid.setAmount(Math.min(RadioactiveFluidTankBlockEntity.CAPACITY_PER_TANK, fluid.getAmount()));
                    nbt.put("TankContent", fluid.writeToNBT(new CompoundTag()));
                }
            }
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private void tryMultiPlace(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return;
        }
        Direction face = context.getClickedFace();
        if (!face.getAxis().isVertical()) {
            return;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
        if (!FluidTankBlock.isTank(level.getBlockState(placedOnPos))
                || SymmetryWandItem.presentInHotbar(player)) {
            return;
        }

        FluidTankBlockEntity tank = ConnectivityHandler.partAt(
                SFBlockEntities.RADIOACTIVE_FLUID_TANK.get(), level, placedOnPos);
        if (tank == null || tank.getControllerBE() == null || tank.getControllerBE().getWidth() == 1) {
            return;
        }

        FluidTankBlockEntity controller = tank.getControllerBE();
        int width = controller.getWidth();
        BlockPos start = face == Direction.DOWN
                ? controller.getBlockPos().below()
                : controller.getBlockPos().above(controller.getHeight());
        if (start.getY() != pos.getY()) {
            return;
        }

        int tanksToPlace = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockState state = level.getBlockState(start.offset(x, 0, z));
                if (FluidTankBlock.isTank(state)) {
                    continue;
                }
                if (!state.canBeReplaced()) {
                    return;
                }
                tanksToPlace++;
            }
        }
        ItemStack stack = context.getItemInHand();
        if (!player.isCreative() && stack.getCount() < tanksToPlace) {
            return;
        }

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos target = start.offset(x, 0, z);
                if (FluidTankBlock.isTank(level.getBlockState(target))) {
                    continue;
                }
                BlockPlaceContext placeContext = BlockPlaceContext.at(context, target, face);
                player.getPersistentData().putBoolean("SilenceTankSound", true);
                super.place(placeContext);
                player.getPersistentData().remove("SilenceTankSound");
            }
        }
    }
}
