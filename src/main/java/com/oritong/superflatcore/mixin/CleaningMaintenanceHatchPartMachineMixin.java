package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.List;

@Mixin(value = CleaningMaintenanceHatchPartMachine.class, remap = false)
public class CleaningMaintenanceHatchPartMachineMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/multiblock/DummyCleanroom;createForTypes(Ljava/util/Collection;)Lcom/gregtechceu/gtceu/api/machine/multiblock/DummyCleanroom;"),
            index = 0,
            remap = false)
    private Collection<CleanroomType> superflatcore$includeCleanroomForSterileHatch(
                                                                                     Collection<CleanroomType> cleanroomTypes) {
        if (cleanroomTypes.contains(CleanroomType.STERILE_CLEANROOM)) {
            return List.of(CleanroomType.CLEANROOM, CleanroomType.STERILE_CLEANROOM);
        }
        return cleanroomTypes;
    }
}
