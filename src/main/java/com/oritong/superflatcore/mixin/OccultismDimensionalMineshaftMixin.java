package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.occultism.OccultismGtnnMiningIntegration;
import com.klikli_dev.occultism.common.blockentity.DimensionalMineshaftBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DimensionalMineshaftBlockEntity.class, remap = false)
public abstract class OccultismDimensionalMineshaftMixin {
    @Shadow
    public LazyOptional<ItemStackHandler> inputHandler;

    @Shadow
    public LazyOptional<ItemStackHandler> outputHandler;

    @Shadow
    public int rollsPerOperation;

    @Inject(method = "mine()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$useGtnnOrePool(CallbackInfo callback) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        OccultismGtnnMiningIntegration.handleMine(blockEntity.getLevel(), inputHandler, outputHandler,
                rollsPerOperation);
        callback.cancel();
    }
}
