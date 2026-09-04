package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.embers.EmbersMalletIntegration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.centrifuge.block.CentrifugeCoreBlock", remap = false)
public abstract class AtomicCentrifugeCoreBlockMixin {
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
