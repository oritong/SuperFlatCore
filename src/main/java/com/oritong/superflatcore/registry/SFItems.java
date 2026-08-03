package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.DontTouchMeItem;
import com.oritong.superflatcore.item.EmployeeCompanionItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Locale;

public final class SFItems {
    private static final ResourceLocation GTCEU_ITEM_TAB = new ResourceLocation("gtceu", "item");
    public static final String DONT_TOUCH_ME_MODID = "dont_touch_me";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            SuperFlatCore.MODID);
    public static final DeferredRegister<Item> DONT_TOUCH_ME_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            DONT_TOUCH_ME_MODID);
    public static final DeferredRegister<CreativeModeTab> DONT_TOUCH_ME_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, DONT_TOUCH_ME_MODID);

    public static final RegistryObject<ComponentItem> SUPER_INFINITE_WATER_COVER = ITEMS.register(
            "super_infinite_water_cover",
            () -> {
                ComponentItem item = ComponentItem.create(new Item.Properties());
                item.attachComponents(
                        new TooltipBehavior(lines -> lines.add(Component.translatable(
                                "item.superflatcore.super_infinite_water_cover.tooltip"))),
                        new CoverPlaceBehavior(SFCovers.SUPER_INFINITE_WATER));
                return item;
            });

    public static final RegistryObject<ComponentItem> BASIC_AIR_INTAKE_COVER = airIntakeCover(
            "basic_air_intake_cover", SFCovers.BASIC_AIR_INTAKE, SFCovers.BASIC_AIR_INTAKE_RATE);
    public static final RegistryObject<ComponentItem> ADVANCED_AIR_INTAKE_COVER = airIntakeCover(
            "advanced_air_intake_cover", SFCovers.ADVANCED_AIR_INTAKE, SFCovers.ADVANCED_AIR_INTAKE_RATE);
    public static final RegistryObject<ComponentItem> ELITE_AIR_INTAKE_COVER = airIntakeCover(
            "elite_air_intake_cover", SFCovers.ELITE_AIR_INTAKE, SFCovers.ELITE_AIR_INTAKE_RATE);
    public static final RegistryObject<ComponentItem> ULTIMATE_AIR_INTAKE_COVER = airIntakeCover(
            "ultimate_air_intake_cover", SFCovers.ULTIMATE_AIR_INTAKE, SFCovers.ULTIMATE_AIR_INTAKE_RATE);
    public static final RegistryObject<ComponentItem> CREATIVE_AIR_INTAKE_COVER = airIntakeCover(
            "creative_air_intake_cover", SFCovers.CREATIVE_AIR_INTAKE, SFCovers.CREATIVE_AIR_INTAKE_RATE);

    public static final RegistryObject<EmployeeCompanionItem> EMPLOYEE_COMPANION = ITEMS.register(
            "employee_companion",
            () -> new EmployeeCompanionItem(new Item.Properties()));

    public static final RegistryObject<DontTouchMeItem> DONT_TOUCH_ME = DONT_TOUCH_ME_ITEMS.register(
            "dont_touch_me",
            () -> new DontTouchMeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> DONT_TOUCH_ME_TAB = DONT_TOUCH_ME_TABS.register(
            "dont_touch_me",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dont_touch_me.dont_touch_me"))
                    .icon(() -> new ItemStack(DONT_TOUCH_ME.get()))
                    .displayItems((parameters, output) -> output.accept(DONT_TOUCH_ME.get()))
                    .build());

    private static final List<RegistryObject<? extends Item>> CREATIVE_TAB_ITEMS = List.of(
            SUPER_INFINITE_WATER_COVER,
            BASIC_AIR_INTAKE_COVER,
            ADVANCED_AIR_INTAKE_COVER,
            ELITE_AIR_INTAKE_COVER,
            ULTIMATE_AIR_INTAKE_COVER,
            CREATIVE_AIR_INTAKE_COVER,
            EMPLOYEE_COMPANION);

    private SFItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        DONT_TOUCH_ME_ITEMS.register(modBus);
        DONT_TOUCH_ME_TABS.register(modBus);
        modBus.addListener(SFItems::addCreativeTabItems);
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEU_ITEM_TAB.equals(event.getTabKey().location())) {
            CREATIVE_TAB_ITEMS.forEach(item -> event.accept(item.get()));
        }
    }

    private static RegistryObject<ComponentItem> airIntakeCover(String id, CoverDefinition coverDefinition,
                                                                int transferRate) {
        return ITEMS.register(id, () -> {
            ComponentItem item = ComponentItem.create(new Item.Properties());
            item.attachComponents(
                    new TooltipBehavior(lines -> lines.add(Component.translatable(
                            "item.superflatcore.air_intake_cover.tooltip", formatRate(transferRate)))),
                    new CoverPlaceBehavior(coverDefinition));
            return item;
        });
    }

    private static String formatRate(int transferRate) {
        return String.format(Locale.ROOT, "%,d", transferRate);
    }
}
