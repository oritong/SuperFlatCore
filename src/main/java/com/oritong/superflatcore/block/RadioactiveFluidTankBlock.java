package com.oritong.superflatcore.block;

import com.oritong.superflatcore.integration.mekanism.MekanismRadiationIntegration;
import com.oritong.superflatcore.registry.SFBlockEntities;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class RadioactiveFluidTankBlock extends FluidTankBlock {
    public RadioactiveFluidTankBlock(Properties properties) {
        super(properties, false);
    }

    @Override
    public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
        return SFBlockEntities.RADIOACTIVE_FLUID_TANK.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !isMoving && level instanceof ServerLevel server) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof com.oritong.superflatcore.block.entity.RadioactiveFluidTankBlockEntity tank) {
                MekanismRadiationIntegration.radiateReactorAt(server, pos);
                MekanismRadiationIntegration.onTankBroken(server, pos, tank);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
