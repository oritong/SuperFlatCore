package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the ability that produced a GTCEu predicate.
 *
 * GTCEu's {@code Predicates.abilities} deliberately returns a normal
 * {@code PredicateBlocks}, so the original ability is otherwise lost before
 * the client structure preview gets to inspect it. Identity is used because
 * TraceabilityPredicate.or() reuses the same SimplePredicate instances.
 */
public final class AbilityPredicateMetadata {
    private static final Map<SimplePredicate, List<String>> ABILITIES =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private AbilityPredicateMetadata() {}

    public static void record(TraceabilityPredicate predicate, PartAbility[] abilities) {
        if (predicate == null || abilities == null || abilities.length == 0) {
            return;
        }

        List<String> names = new ArrayList<>(abilities.length);
        for (PartAbility ability : abilities) {
            if (ability != null && !names.contains(ability.getName())) {
                names.add(ability.getName());
            }
        }
        if (names.isEmpty()) {
            return;
        }

        List<String> immutableNames = List.copyOf(names);
        synchronized (ABILITIES) {
            predicate.common.forEach(simple -> ABILITIES.put(simple, immutableNames));
            predicate.limited.forEach(simple -> ABILITIES.put(simple, immutableNames));
        }
    }

    public static List<String> get(SimplePredicate predicate) {
        synchronized (ABILITIES) {
            return ABILITIES.getOrDefault(predicate, List.of());
        }
    }
}
