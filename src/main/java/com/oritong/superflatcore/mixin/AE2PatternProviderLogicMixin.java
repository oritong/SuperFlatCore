package com.oritong.superflatcore.mixin;

import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
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

@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class AE2PatternProviderLogicMixin {
    @Shadow @Final private PatternProviderLogicHost host;
    @Shadow @Final private List<GenericStack> sendList;
    @Shadow @Final private PatternProviderReturnInventory returnInv;

    @Inject(method = "addDrops", at = @At("HEAD"), remap = false)
    private void superflatcore$radiateStoredFluid(List<ItemStack> drops, CallbackInfo ci) {
        BlockEntity blockEntity = host.getBlockEntity();
        if (!(blockEntity.getLevel() instanceof ServerLevel server)) {
            return;
        }
        for (GenericStack stack : sendList) {
            MekanismRadiationIntegration.radiateRadioactiveAeStack(server, blockEntity.getBlockPos(), stack);
        }
        MekanismRadiationIntegration.radiateRadioactiveAeInventory(server, blockEntity.getBlockPos(), returnInv);
    }
}
