package com.oritong.superflatcore.client;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.DontTouchMeItem;
import com.oritong.superflatcore.registry.SFItems;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DontTouchMeClientSetup {
    private DontTouchMeClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(SFItems.DONT_TOUCH_ME.get(),
                new ResourceLocation(SFItems.DONT_TOUCH_ME_MODID, "stage"),
                (stack, level, entity, seed) -> DontTouchMeItem.getStage(stack)));
    }
}
