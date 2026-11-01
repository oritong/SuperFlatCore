package com.oritong.superflatcore.integration.mana;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Shared tag check for the fluids accepted by mana hatches. */
public final class ManaFluidPolicy {
    public static final ResourceLocation MANA_FLUID_ID = new ResourceLocation("technicalflowers", "mana");

    private ManaFluidPolicy() {}

    public static boolean isMana(FluidStack stack) {
        return !stack.isEmpty() && isMana(stack.getFluid());
    }

    public static boolean isMana(Fluid fluid) {
        return MANA_FLUID_ID.equals(ForgeRegistries.FLUIDS.getKey(fluid));
    }
}
