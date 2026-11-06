package com.oritong.superflatcore.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.blackhole.client.BlackHoleBlockEntityRenderer;
import com.oritong.superflatcore.blackhole.client.BlackHoleClient;
import com.oritong.superflatcore.blackhole.client.BlackHoleRender;
import com.oritong.superflatcore.registry.SFBlockEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SFClientRegistration {
    private SFClientRegistration() {}

    @SubscribeEvent
    public static void onModConstruction(FMLConstructModEvent event) {
        DynamicRenderManager.register(SuperFlatCore.id("wind_generator"), WindGeneratorDynamicRender.TYPE);
        MinecraftForge.EVENT_BUS.register(BlackHoleClient.class);
    }

    /**
     * Register the black-hole shader from the client mod bus explicitly.
     *
     * The shader class also has an EventBusSubscriber annotation, but relying
     * on classpath scanning here is fragile with the Embeddium/Oculus launch
     * stack.  This listener keeps the skill renderer attached to the same
     * Forge mod bus as the rest of the client registrations.
     */
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws java.io.IOException {
        BlackHoleRender.register(event);
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
