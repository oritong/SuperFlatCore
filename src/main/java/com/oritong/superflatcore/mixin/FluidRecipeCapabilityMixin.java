package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents GTM's fluid recipe UI from binding a visible tank to the zero
 * capacity placeholder used by small compressor machines.
 */
@Mixin(value = FluidRecipeCapability.class, remap = false)
public abstract class FluidRecipeCapabilityMixin {
    @Inject(method = "applyWidgetInfo", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$hideZeroCapacityTank(
            Widget widget,
            int index,
            boolean isXEI,
            IO io,
            GTRecipeTypeUI.RecipeHolder recipeHolder,
            GTRecipeType recipeType,
            GTRecipe recipe,
            Content content,
            Object handler,
            int slotIndex,
            int totalSlots,
            CallbackInfo ci) {
        if (!(widget instanceof TankWidget) || !(handler instanceof IFluidHandler fluids)) {
            return;
        }

        if (fluids.getTanks() == 1 && fluids.getTankCapacity(0) == 0) {
            widget.setVisible(false);
            ci.cancel();
        }
    }
}
