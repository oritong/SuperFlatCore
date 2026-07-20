package com.oritong.superflatcore.mixin;

import com.buuz135.industrial.block.core.tile.LatexProcessingUnitTile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LatexProcessingUnitTile.class, remap = false)
public abstract class LatexProcessingUnitTileMixin {
    @Unique
    private static final ResourceLocation SUPERFLATCORE$ORITONG_LATEX_ID =
            new ResourceLocation("oritong", "latex");

    @Inject(method = "lambda$new$0", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$useOritongLatex(FluidStack fluidStack,
                                                      CallbackInfoReturnable<Boolean> cir) {
        Fluid oritongLatex = ForgeRegistries.FLUIDS.getValue(SUPERFLATCORE$ORITONG_LATEX_ID);
        cir.setReturnValue(oritongLatex != null && fluidStack.getFluid().isSame(oritongLatex));
    }
}
