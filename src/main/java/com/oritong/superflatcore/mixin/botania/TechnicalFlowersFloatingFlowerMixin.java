package com.oritong.superflatcore.mixin.botania;

import com.technicalflowers.common.FluidMatcher;
import com.technicalflowers.common.ManaConversion;
import com.technicalflowers.common.PipeChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

@Mixin(value = FunctionalFlowerBlockEntity.class, remap = false)
public abstract class TechnicalFlowersFloatingFlowerMixin {
    @Inject(method = "tickFlower", at = @At("TAIL"), remap = false)
    private void superflatcore$drawManaFromSidePipes(CallbackInfo ci) {
        FunctionalFlowerBlockEntity flower = (FunctionalFlowerBlockEntity) (Object) this;
        Level level = flower.getLevel();
        if (level == null || level.isClientSide || !flower.isFloating()
                || !flower.getBlockState().is(BotaniaTags.Blocks.FLOATING_FLOWERS)) {
            return;
        }

        int missingMana = flower.getMaxMana() - flower.getMana();
        if (missingMana <= 0) {
            return;
        }

        BlockPos flowerPos = flower.getBlockPos();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos pipePos = flowerPos.relative(side);
            Direction pipeFace = side.getOpposite();
            if (!PipeChecker.isPipe(level, pipePos, pipeFace)) {
                continue;
            }

            BlockEntity pipe = level.getBlockEntity(pipePos);
            if (pipe == null) {
                continue;
            }

            IFluidHandler fluidHandler = pipe.getCapability(ForgeCapabilities.FLUID_HANDLER, pipeFace)
                    .resolve()
                    .orElse(null);
            if (fluidHandler == null) {
                continue;
            }

            int mana = ManaConversion.drainMana(fluidHandler,
                    FluidMatcher.findSuitableManaFluid(fluidHandler), missingMana);
            if (mana > 0) {
                flower.addMana(mana);
                flower.sync();
                return;
            }
        }
    }
}
