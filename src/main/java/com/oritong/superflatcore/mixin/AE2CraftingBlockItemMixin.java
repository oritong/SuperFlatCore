package com.oritong.superflatcore.mixin;

import appeng.block.crafting.CraftingBlockItem;
import appeng.core.definitions.AEBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(value = CraftingBlockItem.class, remap = false)
public abstract class AE2CraftingBlockItemMixin {
    @Unique
    private static final ResourceLocation SUPERFLATCORE$EV_CIRCUIT =
            new ResourceLocation("kubejs", "general_circuit_ev");

    @Redirect(method = "m_7203_", at = @At(value = "INVOKE",
            target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"), remap = false)
    private Object superflatcore$acceleratorDisassemblyExtra(Supplier<?> original) {
        if ((Object) this == ((ItemLike) AEBlocks.CRAFTING_ACCELERATOR).asItem()) {
            return ForgeRegistries.ITEMS.getValue(SUPERFLATCORE$EV_CIRCUIT);
        }
        return original.get();
    }
}
