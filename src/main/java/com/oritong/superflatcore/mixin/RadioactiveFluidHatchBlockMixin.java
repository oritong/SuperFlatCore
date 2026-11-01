package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.oritong.superflatcore.integration.mekanism.MekanismRadiationIntegration;
import com.oritong.superflatcore.machine.RadioactiveFluidHatchPartMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MetaMachineBlock.class, remap = false)
public abstract class RadioactiveFluidHatchBlockMixin {
    @Inject(method = "m_6810_", at = @At("HEAD"), remap = false)
    private void superflatcore$radiateStoredFluid(BlockState state, Level level, BlockPos pos,
                                                  BlockState replacement, boolean moving, CallbackInfo ci) {
        if (moving || state.getBlock() == replacement.getBlock() || !(level instanceof ServerLevel server)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity blockEntity
                && blockEntity.getMetaMachine() instanceof RadioactiveFluidHatchPartMachine hatch) {
            MekanismRadiationIntegration.radiateRadioactiveFluidHandler(server, pos, hatch.tank);
        }
    }
}
