package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.oritong.superflatcore.registry.SFMachines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = GCYMMachines.class, remap = false)
public abstract class GCYMMachinesMixin {
    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private static void superflatcore$radioactiveHatches(CallbackInfo ci) {
        GCYMMachines.LARGE_CENTRIFUGE.setPatternFactory(() -> FactoryBlockPattern.start()
                .aisle("#XXX#", "XXXXX", "#XXX#")
                .aisle("XXXXX", "XAPAX", "XXXXX")
                .aisle("XXXXX", "XPAPX", "XXXXX")
                .aisle("XXXXX", "XAPAX", "XXXXX")
                .aisle("#XXX#", "XXSXX", "#XXX#")
                .where('S', Predicates.controller(Predicates.blocks(GCYMMachines.LARGE_CENTRIFUGE.getBlock())))
                .where('X', Predicates.blocks(GCYMBlocks.CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(40)
                        .or(Predicates.autoAbilities(GCYMMachines.LARGE_CENTRIFUGE.getRecipeTypes()))
                        .or(Predicates.autoAbilities(true, false, true)))
                .where('P', Predicates.blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                .where('A', Predicates.air()
                        .or(Predicates.blocks(SFMachines.RADIOACTIVE_FLUID_INPUT_HATCH.get()))
                        .or(Predicates.blocks(SFMachines.RADIOACTIVE_FLUID_OUTPUT_HATCH.get())))
                .where('#', Predicates.any())
                .build());

        GCYMMachines.LARGE_EXTRACTOR.setPatternFactory(() -> FactoryBlockPattern.start()
                .aisle("XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XCACX", "XXXXX")
                .aisle("XXXXX", "XXSXX", "XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(GCYMMachines.LARGE_EXTRACTOR.getBlock())))
                .where('X', Predicates.blocks(GCYMBlocks.CASING_WATERTIGHT.get()).setMinGlobalLimited(25)
                        .or(Predicates.autoAbilities(GCYMMachines.LARGE_EXTRACTOR.getRecipeTypes()))
                        .or(Predicates.autoAbilities(true, false, true)))
                .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                .where('A', Predicates.air()
                        .or(Predicates.blocks(SFMachines.RADIOACTIVE_FLUID_INPUT_HATCH.get()))
                        .or(Predicates.blocks(SFMachines.RADIOACTIVE_FLUID_OUTPUT_HATCH.get())))
                .build());
    }
}
