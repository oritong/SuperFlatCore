package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Arrays;
import java.util.Objects;

public final class LargeAssemblerRecipeIntegration {
    private LargeAssemblerRecipeIntegration() {}

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var aeAssembler = Objects.requireNonNull(GTRegistries.RECIPE_TYPES.get(GTCEu.id("ae_assembler")),
                    "Pack recipe type gtceu:ae_assembler must be registered before common setup");
            var machine = GCYMMachines.LARGE_ASSEMBLER;
            var types = machine.getRecipeTypes();
            if (Arrays.asList(types).contains(aeAssembler)) {
                return;
            }
            var expanded = Arrays.copyOf(types, types.length + 1);
            expanded[types.length] = aeAssembler;
            machine.setRecipeTypes(expanded);
        });
    }
}
