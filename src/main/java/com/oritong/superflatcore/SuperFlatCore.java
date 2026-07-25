package com.oritong.superflatcore;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.oritong.superflatcore.registry.SFCovers;
import com.oritong.superflatcore.registry.SFItems;
import com.oritong.superflatcore.registry.SFMachines;
import com.oritong.superflatcore.registry.SFMobEffects;
import com.oritong.superflatcore.registry.SFRegistrate;
import com.oritong.superflatcore.event.TimeCrystalTiabEvents;
import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
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
        modBus.addListener(DominionWandBotaniaIntegration::commonSetup);
        TimeCrystalTiabEvents.requestTiabApi();
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, DominionWandBotaniaIntegration::attachCapabilities);

        LOGGER.info("SuperFlatCore loaded");
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }
}
