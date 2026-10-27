package com.oritong.superflatcore.integration.jei;

import mezz.jei.api.IModPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Explicit JEI plugin list for the Diamond Continent pack.
 *
 * <p>JEI normally discovers every annotated class from Forge scan data. The
 * pack already has a fixed mod set, so loading these known plugin classes
 * avoids repeating that full annotation scan while retaining the pack's JEI
 * integrations.</p>
 */
public final class SFJeiPluginRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<String> PLUGIN_CLASSES = List.of(
            "mezz.jei.library.plugins.vanilla.VanillaPlugin",
            "mezz.jei.library.plugins.jei.JeiInternalPlugin",
            "dev.emi.emi.jemi.JemiPlugin",
            "mezz.jei.forge.plugins.forge.ForgeGuiPlugin",
            "mezz.jei.gui.plugins.JeiGuiPlugin",
            "com.shiory.atomicembers.compat.jei.AtomicEmbersJeiPlugin",
            "codechicken.lib.internal.compat.JEIPlugin",
            "fr.iglee42.cmr.jei.CMRJEI",
            "insane96mcp.customfluidmixin.integration.jei.CFMJeiPlugin",
            "dev.celestiacraft.industrialplatform.compat.jei.ModJeiPlugin",
            "snownee.kiwi.customization.compat.jei.JEICompat",
            "snownee.lychee.compat.jei.JEICompat",
            "com.buuz135.replication.compat.JEICompat",
            "com.glodblock.github.extendedae.xmod.jei.JEIPlugin",
            "de.mari_023.ae2wtlib.reijei.JEIPlugin",
            "pureneko.conveyor_belt_plus.compat.jei.ConveyorJeiPlugin",
            "com.rekindled.embers.compat.jei.JEIPlugin",
            "vectorwing.farmersdelight.integration.jei.JEIPlugin",
            "com.buuz135.functionalstorage.compat.jei.JEIPlugin",
            "ho.artisan.tgears.plugin.jei.TGJEI",
            "slimeknights.tconstruct.plugin.jei.JEIPlugin",
            "slimeknights.mantle.plugin.jei.JEIPlugin",
            "com.buuz135.industrial.plugin.jei.JEICustomPlugin",
            "net.yxiao233.ifeu.common.compact.jei.JEIPlugin",
            "vazkii.patchouli.client.jei.PatchouliJeiPlugin",
            "appeng.integration.modules.jei.JEIPlugin",
            "appbot.integration.jei.ABJEIPlugin",
            "thetadev.constructionwand.integrations.jei.ConstructionWandJeiPlugin",
            "com.arsmeteorites.arsmeteorites.jei.MeteoritesJeiPlugin",
            "com.hollingsworth.arsnouveau.client.jei.JEIArsNouveauPlugin",
            "committee.nova.mods.avaritia.init.compat.jei.AvaritiaJeiPlugin",
            "ironfurnaces.jei.IronFurnacesJEIPlugin",
            "com.simibubi.create.compat.jei.CreateJEI",
            "com.hlysine.create_connected.compat.CreateConnectedJEI",
            "com.gregtechceu.gtceu.integration.jei.GTJEIPlugin",
            "com.lowdragmc.lowdraglib.jei.JEIPlugin",
            "vazkii.botania.client.integration.jei.JEIBotaniaPlugin",
            "com.klikli_dev.modonomicon.integration.ModonomiconJeiIntegration$ModonomiconJeiPlugin",
            "snownee.jade.compat.JEICompat",
            "com.klikli_dev.occultism.integration.jei.JeiPlugin",
            "net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.CoreJeiPlugin",
            "net.p3pp3rf1y.sophisticatedbackpacks.compat.recipeviewers.jei.BackpackJeiPlugin",
            "ca.teamdman.sfm.client.jei.SFMJEIPlugin",
            "com.wintercogs.beyonddimensions.Integration.JEI.BDjeiPlugin",
            "mekanism.client.jei.MekanismJEI",
            "mekanism.generators.client.jei.GeneratorsJEI",
            "com.jerry.mekanism_extras.client.jei.ExtraJEI",
            "com.jerry.mekmm.client.jei.MoreMachineJEI",
            "com.mrcrayfish.configured.impl.jei.ConfiguredJeiPlugin",
            "com.hrznstudio.titanium.jei.TitaniumJEIPlugin",
            "net.pedroksl.advanced_ae.xmod.jei.JEIPlugin",
            "dev.uncandango.alltheleaks.plugins.ATLJeiPlugin",
            "com.portingdeadmods.cable_facades.compat.CFJeiPlugin",
            "com.mrh0.createaddition.compat.jei.CreateAdditionJEI",
            "top.theillusivec4.curios.common.integration.jei.CuriosJeiPlugin",
            "cyb0124.curvy_pipes.compat.JEICompat",
            "com.extendedae_plus.integration.jei.ExtendedAEJeiPlugin",
            "dev.ftb.mods.ftbfiltersystem.integration.jei.JEIIntegration",
            "dev.ftb.mods.ftblibrary.integration.forge.JEIIntegration",
            "dev.ftb.mods.ftbxmodcompat.ftbquests.jei.FTBQuestsJEIIntegration",
            "invtweaks.jei.InvTweaksJEI",
            "dev.latvian.mods.kubejs.integration.forge.jei.JEIPlugin",
            "pie.ilikepiefoo.compat.jei.JEIPlugin",
            "mcjty.lib.compat.JeiCompatibility",
            "de.maxhenkel.pipez.integration.jei.JEIPlugin",
            "net.creeperhost.polylib.compat.jei.PolyJEIPlugin",
            "ca.teamdman.sfmgui.client.jei.SfmGuiJeiPlugin",
            "mezz.itemzoom.client.compat.JeiModPlugin",
            "com.pureneko.vjrm.client.VjrmJeiPlugin"
    );

    private SFJeiPluginRegistry() {
    }

    public static List<IModPlugin> createPlugins() {
        Set<String> uniqueClasses = new LinkedHashSet<>(PLUGIN_CLASSES);
        List<IModPlugin> plugins = new ArrayList<>(uniqueClasses.size());
        for (String className : uniqueClasses) {
            try {
                Class<?> pluginClass = Class.forName(className);
                if (!IModPlugin.class.isAssignableFrom(pluginClass)) {
                    LOGGER.warn("Skipping JEI class that is not an IModPlugin: {}", className);
                    continue;
                }
                Constructor<?> constructor = pluginClass.getDeclaredConstructor();
                plugins.add((IModPlugin) constructor.newInstance());
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Optional integrations are allowed to be absent from a client.
            }
        }
        LOGGER.info("Loaded {} explicit JEI plugins without Forge annotation scanning", plugins.size());
        return plugins;
    }
}
