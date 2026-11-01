package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.oritong.superflatcore.block.WindGeneratorCollisionBlock;
import com.oritong.superflatcore.registry.SFMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MetaMachineBlock.class, remap = false)
public abstract class WindGeneratorBoundingMixin {
    @Inject(method = "m_5573_", at = @At("RETURN"), cancellable = true, remap = false)
    private void superflatcore$requireWindGeneratorClearance(BlockPlaceContext context,
                                                             CallbackInfoReturnable<BlockState> callback) {
        if ((Object) this == SFMachines.WIND_GENERATOR.getBlock() && callback.getReturnValue() != null
                && !WindGeneratorCollisionBlock.canPlaceAt(context.getLevel(), context.getClickedPos())) {
            callback.setReturnValue(null);
        }
    }

    @Inject(method = "m_6810_", at = @At("HEAD"), remap = false)
    private void superflatcore$removeWindGeneratorCollision(BlockState state, Level level, BlockPos pos,
                                                            BlockState replacement, boolean moving,
                                                            CallbackInfo callback) {
        if (state.is(SFMachines.WIND_GENERATOR.getBlock())
                && !replacement.is(SFMachines.WIND_GENERATOR.getBlock())) {
            WindGeneratorCollisionBlock.removeFor(level, pos);
        }
    }
}
