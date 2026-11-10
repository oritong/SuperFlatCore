package com.oritong.superflatcore.mixin;

import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ItemAtomicDisassembler.class, remap = false)
public abstract class MekanismAtomicDisassemblerMixin {
    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lmekanism/common/item/ItemEnergized;<init>(Lmekanism/api/math/FloatingLongSupplier;Lmekanism/api/math/FloatingLongSupplier;Lnet/minecraft/world/item/Item$Properties;)V"),
            index = 1,
            remap = false)
    private static FloatingLongSupplier superflatcore$setEnergyCapacity(FloatingLongSupplier original) {
        // Replace the shared capacity supplier so capabilities and creative full-charge variants agree.
        return () -> EnergyUnit.FORGE_ENERGY.convertFrom(FloatingLong.createConst(3_250_000));
    }
}
