package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.gtlcore.gtlcore.common.data.machines.GCyMMachines", remap = false)
public abstract class GTLCoreLargeExtruderMixin {
    @Shadow
    @Final
    public static MultiblockMachineDefinition LARGE_EXTRUDER;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void superflatcore$allowFluidHatches(CallbackInfo ci) {
        LARGE_EXTRUDER.setPatternFactory(() -> FactoryBlockPattern.start()
                .aisle("##XXX", "##XXX", "##XXX")
                .aisle("##XXX", "##XPX", "##XGX").setRepeatable(2)
                .aisle("XXXXX", "XXXPX", "XXXGX")
                .aisle("XXXXX", "XAXPX", "XXXGX")
                .aisle("XXXXX", "XSXXX", "XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(LARGE_EXTRUDER.getBlock())))
                .where('X', Predicates.blocks(GCYMBlocks.CASING_STRESS_PROOF.get()).setMinGlobalLimited(40)
                        .or(Predicates.autoAbilities(LARGE_EXTRUDER.getRecipeTypes()))
                        .or(Predicates.autoAbilities(true, false, true))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                .where('P', Predicates.blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where('G', Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                .where('A', Predicates.air())
                .where('#', Predicates.any())
                .build());
    }
}
