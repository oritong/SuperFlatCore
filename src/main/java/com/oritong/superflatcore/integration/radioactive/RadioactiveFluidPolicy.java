package com.oritong.superflatcore.integration.radioactive;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Shared tag check for every fluid handler that must reject radioactive fluids. */
public final class RadioactiveFluidPolicy {
    public static final TagKey<Fluid> RADIOACTIVE_FLUIDS = TagKey.create(
            Registries.FLUID, new ResourceLocation("oritong", "radioactive_fluids"));

    private RadioactiveFluidPolicy() {}

    public static boolean isRadioactive(FluidStack stack) {
        return !stack.isEmpty() && isRadioactive(stack.getFluid());
    }

    public static boolean isRadioactive(Fluid fluid) {
        return fluid.builtInRegistryHolder().is(RADIOACTIVE_FLUIDS);
    }
}
