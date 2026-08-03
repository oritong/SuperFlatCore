package com.oritong.superflatcore.registry;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.effect.MachineSoulDispleasureEffect;
import com.oritong.superflatcore.effect.MachineSoulJoyEffect;

import net.minecraft.world.effect.MobEffect;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SFMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(
            ForgeRegistries.MOB_EFFECTS, SuperFlatCore.MODID);

    public static final RegistryObject<MobEffect> MACHINE_SOUL_JOY = MOB_EFFECTS.register(
            "machine_soul_joy", MachineSoulJoyEffect::new);
    public static final RegistryObject<MobEffect> MACHINE_SOUL_DISPLEASURE = MOB_EFFECTS.register(
            "machine_soul_displeasure", MachineSoulDispleasureEffect::new);

    private SFMobEffects() {}

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
