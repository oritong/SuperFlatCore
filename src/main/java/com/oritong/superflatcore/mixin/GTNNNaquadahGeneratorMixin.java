package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.arbor.gtnn.common.machine.GTNNGeneratorMachine$Companion", remap = false)
public abstract class GTNNNaquadahGeneratorMixin {
    @Inject(method = "nonParallel", at = @At("RETURN"), cancellable = true)
    private void superflatcore$twoAmpNaquadahGeneration(MetaMachine machine, GTRecipe recipe,
                                                        CallbackInfoReturnable<ModifierFunction> cir) {
        var id = machine.getDefinition().getId();
        if (!(machine instanceof SimpleGeneratorMachine)
                || !id.getNamespace().equals("gtnn") || !id.getPath().endsWith("_naquadah_reactor")) {
            return;
        }
        // GTNN's reactor bypasses SimpleGeneratorMachine's parallel modifier.
        // Keep its efficiency/duration adjustment, then scale fuel, byproducts,
        // and EU/t together using the same two-amp budget as other generators.
        ModifierFunction efficiency = cir.getReturnValue();
        cir.setReturnValue(original -> {
            GTRecipe adjusted = efficiency.apply(original);
            return adjusted == null ? null : SimpleGeneratorMachine.recipeModifier(machine, adjusted).apply(adjusted);
        });
    }
}
