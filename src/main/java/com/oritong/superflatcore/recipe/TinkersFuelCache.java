package com.oritong.superflatcore.recipe;

import com.oritong.superflatcore.mixin.TinkersFuelLookupAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Restores Tinkers' tooltip and machine lookup from the authoritative, fully loaded fuel recipes. */
public final class TinkersFuelCache {
    private static final Logger LOGGER = LogManager.getLogger();

    private TinkersFuelCache() {}

    public static void refresh(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes) {
        refresh(recipes.getOrDefault(TinkerRecipeTypes.FUEL.get(), Map.of()).values());
    }

    public static synchronized void refresh(Collection<? extends Recipe<?>> recipes) {
        List<MeltingFuel> fuels = recipes.stream().filter(MeltingFuel.class::isInstance)
                .map(MeltingFuel.class::cast).toList();
        // Clears cached misses and the queued client invalidation before repopulating all active fuels.
        TinkersFuelLookupAccessor.superflatcore$getListener().clearCache();
        fuels.forEach(MeltingFuelLookup::addFuel);
        MeltingFuel lava = MeltingFuelLookup.findFuel(Fluids.LAVA);
        LOGGER.info("Refreshed Tinkers fuel lookup from {} active recipes; lava fuel temperature: {}",
                fuels.size(), lava == null ? "missing" : lava.getTemperature());
    }
}
