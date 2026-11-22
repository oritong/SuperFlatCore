package com.oritong.superflatcore.client;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.recipe.TinkersFuelCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TinkersFuelClientEvents {
    private TinkersFuelClientEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        // Tinkers queues its own invalidation at NORMAL priority; rebuild after that callback.
        TinkersFuelCache.refresh(event.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.FUEL.get()));
    }
}
