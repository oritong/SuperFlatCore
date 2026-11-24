package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SimpleGeneratorMachine.class, remap = false)
public abstract class SimpleGeneratorMachineMixin {
    @Redirect(method = "recipeModifier", at = @At(value = "INVOKE",
            target = "Lcom/gregtechceu/gtceu/api/machine/SimpleGeneratorMachine;getOverclockVoltage()J"))
    private static long superflatcore$twoAmpGenerationBudget(SimpleGeneratorMachine generator) {
        // Keep GT's fuel/input/output parallel scaling and efficiency. Only double
        // the power budget; the machine's voltage and available fuel still limit it.
        return generator.getOverclockVoltage() * 2;
    }
}
