package com.oritong.superflatcore;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.oritong.superflatcore.registry.SFCovers;
import com.oritong.superflatcore.registry.SFItems;
import com.oritong.superflatcore.registry.SFMachines;
import com.oritong.superflatcore.registry.SFMobEffects;
import com.oritong.superflatcore.registry.SFRegistrate;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SuperFlatCore.MODID)
public class SuperFlatCore {
    public static final String MODID = "superflatcore";
    private static final Logger LOGGER = LogManager.getLogger();

    public SuperFlatCore() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SFRegistrate.REGISTRATE.registerEventListeners(modBus);
        SFItems.register(modBus);
        SFMobEffects.register(modBus);
        modBus.addGenericListener(CoverDefinition.class, SFCovers::registerCovers);
        modBus.addGenericListener(MachineDefinition.class, SFMachines::registerMachines);
        modBus.addListener(SFMachines::addCreativeTabItems);

        LOGGER.info("SuperFlatCore loaded");
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }
}
