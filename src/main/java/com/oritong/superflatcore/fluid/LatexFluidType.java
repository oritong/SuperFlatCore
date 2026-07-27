package com.oritong.superflatcore.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class LatexFluidType extends FluidType {
    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("oritong",
            "block/fluid/latex_still");
    private static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("oritong",
            "block/fluid/latex_flow");

    public LatexFluidType() {
        super(Properties.create()
                .descriptionId("fluid.oritong.latex")
                .density(1200)
                .viscosity(4000)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }
        });
    }
}
