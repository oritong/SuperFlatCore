package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.oritong.superflatcore.machine.ConfigurableAutoMaintenanceHatchPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CleanroomCondition.class, remap = false)
public class CleanroomConditionMixin {
    @Shadow
    private CleanroomType cleanroom;

    @Inject(method = "testCondition", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$recognizeCleaningMaintenanceHatches(GTRecipe recipe, RecipeLogic recipeLogic,
                                                                    CallbackInfoReturnable<Boolean> cir) {
        if (cleanroom == null) {
            return;
        }

        MetaMachine machine = recipeLogic.getMachine();
        if (!(machine instanceof IMultiController controller)) {
            return;
        }

        for (IMultiPart part : controller.getParts()) {
            if (part instanceof ConfigurableAutoMaintenanceHatchPartMachine hatch
                    && hatch.supportsCleanroom(cleanroom)) {
                cir.setReturnValue(true);
                return;
            }
            if (part instanceof CleaningMaintenanceHatchPartMachine hatch
                    && supportsCleanroom(hatch.getCleanroomType(), cleanroom)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    private static boolean supportsCleanroom(CleanroomType providedType, CleanroomType requiredType) {
        return providedType == requiredType
                || providedType == CleanroomType.STERILE_CLEANROOM && requiredType == CleanroomType.CLEANROOM;
    }
}
