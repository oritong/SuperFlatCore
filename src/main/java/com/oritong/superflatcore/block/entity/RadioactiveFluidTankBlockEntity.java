package com.oritong.superflatcore.block.entity;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.oritong.superflatcore.integration.radioactive.IRadioactiveFluidStorage;
import com.oritong.superflatcore.integration.radioactive.RadioactiveFluidPolicy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public final class RadioactiveFluidTankBlockEntity extends FluidTankBlockEntity {
    public static final int CAPACITY_PER_TANK = 16_000;

    public RadioactiveFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected SmartFluidTank createInventory() {
        SmartFluidTank tank = new RadioactiveSmartFluidTank(CAPACITY_PER_TANK, this::onFluidStackChanged);
        tank.setValidator(RadioactiveFluidPolicy::isRadioactive);
        return tank;
    }

    @Override
    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * CAPACITY_PER_TANK);
        int overflow = tankInventory.getFluidAmount() - tankInventory.getCapacity();
        if (overflow > 0) {
            tankInventory.drain(overflow, FluidAction.EXECUTE);
        }
        forceFluidLevelUpdate = true;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        CompoundTag tankData = compound.contains("TankContent")
                ? compound.getCompound("TankContent").copy()
                : null;
        super.read(compound, clientPacket);
        if (!isController()) {
            return;
        }

        tankInventory.setCapacity(getTotalTankSize() * CAPACITY_PER_TANK);
        if (tankData == null) {
            return;
        }

        FluidStack stored = FluidStack.loadFluidStackFromNBT(tankData);
        if (RadioactiveFluidPolicy.isRadioactive(stored)) {
            stored.setAmount(Math.min(stored.getAmount(), tankInventory.getCapacity()));
            tankInventory.setFluid(stored);
        } else {
            tankInventory.setFluid(FluidStack.EMPTY);
        }
    }

    @Override
    public int getTankSize(int tank) {
        return CAPACITY_PER_TANK;
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    private static final class RadioactiveSmartFluidTank extends SmartFluidTank implements IRadioactiveFluidStorage {
        private RadioactiveSmartFluidTank(int capacity, java.util.function.Consumer<FluidStack> updateCallback) {
            super(capacity, updateCallback);
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(RadioactiveFluidPolicy.isRadioactive(stack) ? stack : FluidStack.EMPTY);
        }
    }
}
