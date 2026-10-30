package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Records which PartAbility a GTCEu ability predicate represents. */
@Mixin(value = Predicates.class, remap = false)
public abstract class PredicatesAbilitiesMixin {

    @Inject(method = "abilities", at = @At("RETURN"), remap = false)
    private static void superflatcore$rememberAbilities(PartAbility[] abilities,
                                                         CallbackInfoReturnable<TraceabilityPredicate> cir) {
        AbilityPredicateMetadata.record(cir.getReturnValue(), abilities);
    }

    @Inject(method = "ability", at = @At("RETURN"), remap = false)
    private static void superflatcore$rememberAbility(PartAbility ability, int[] tiers,
                                                       CallbackInfoReturnable<TraceabilityPredicate> cir) {
        AbilityPredicateMetadata.record(cir.getReturnValue(), new PartAbility[] {ability});
    }
}
