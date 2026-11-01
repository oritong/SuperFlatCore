package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.embers.EmbersMalletIntegration;
import com.oritong.superflatcore.integration.mekanism.MekanismRadiationIntegration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.reactor.block.ReactorPortBlock", remap = false)
public abstract class AtomicReactorPortBlockMixin {
    @Inject(method = "m_6810_", at = @At("HEAD"), remap = false)
    private void superflatcore$radiateOnRemove(BlockState state, Level level, BlockPos pos,
                                                BlockState newState, boolean moving,
                                                CallbackInfo ci) {
        if (!moving && state.getBlock() != newState.getBlock() && level instanceof ServerLevel server
                && state.getValue(com.shiory.atomicembers.feature.reactor.block.ReactorPortBlock.FORMED)) {
            MekanismRadiationIntegration.radiateReactorAt(server, pos);
        }
    }

    @Redirect(
            method = "m_6227_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;m_150930_(Lnet/minecraft/world/item/Item;)Z"),
            remap = false)
    private boolean superflatcore$acceptCraftingMallet(ItemStack stack, Item item) {
        return stack.is(item) || EmbersMalletIntegration.isCraftingMallet(stack);
    }
}
