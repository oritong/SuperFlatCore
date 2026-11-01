package com.oritong.superflatcore.mixin.diamondcore.gtnn;

import com.oritong.superflatcore.compat.diamondcore.gtnn.GTNNBedrockOreMinerRestrictions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.arbor.gtnn.common.machine.StoneBedrockOreMinerMachine", remap = false)
public abstract class MixinStoneBedrockOreMinerMachine {

    @Inject(method = "_init_$lambda$0", at = @At("HEAD"), cancellable = true, remap = false)
    private static void oritongsdiamondcore$rejectBannedFilterItems(
            ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (GTNNBedrockOreMinerRestrictions.isBanned(stack)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getFilter", at = @At("RETURN"), cancellable = true, remap = false)
    private void oritongsdiamondcore$invalidateBannedExistingFilters(CallbackInfoReturnable<Item> cir) {
        if (GTNNBedrockOreMinerRestrictions.isBanned(cir.getReturnValue())) {
            cir.setReturnValue(null);
        }
    }
}
