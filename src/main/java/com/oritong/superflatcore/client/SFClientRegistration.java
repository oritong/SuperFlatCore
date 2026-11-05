package com.oritong.superflatcore.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.blackhole.client.BlackHoleBlockEntityRenderer;
import com.oritong.superflatcore.registry.SFBlockEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SFClientRegistration {
    private SFClientRegistration() {}

    @SubscribeEvent
    public static void onModConstruction(FMLConstructModEvent event) {
        DynamicRenderManager.register(SuperFlatCore.id("wind_generator"), WindGeneratorDynamicRender.TYPE);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(TerraPlateMengerRenderer.MODEL_ID);
    }

    @SubscribeEvent
    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SFBlockEntities.BLACK_HOLE.get(), BlackHoleBlockEntityRenderer::new);
    }
}
