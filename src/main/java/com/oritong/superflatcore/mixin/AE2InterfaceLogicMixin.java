package com.oritong.superflatcore.mixin;

import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import com.oritong.superflatcore.integration.mekanism.MekanismRadiationIntegration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = InterfaceLogic.class, remap = false)
public abstract class AE2InterfaceLogicMixin {
    @Shadow @Final protected InterfaceLogicHost host;

    @Inject(method = "addDrops", at = @At("HEAD"), remap = false)
    private void superflatcore$radiateStoredFluid(List<ItemStack> drops, CallbackInfo ci) {
        BlockEntity blockEntity = host.getBlockEntity();
        if (blockEntity.getLevel() instanceof ServerLevel server) {
            MekanismRadiationIntegration.radiateRadioactiveAeInventory(server, blockEntity.getBlockPos(),
                    ((InterfaceLogic) (Object) this).getStorage());
        }
    }
}
