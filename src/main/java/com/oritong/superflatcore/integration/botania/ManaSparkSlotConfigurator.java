package com.oritong.superflatcore.integration.botania;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import vazkii.botania.common.item.BotaniaItems;

import java.util.List;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ManaSparkSlotConfigurator implements IFancyConfigurator {
    private final CustomItemStackHandler inventory;
    private final Supplier<DyeColor> network;
    private final Consumer<DyeColor> setNetwork;

    public ManaSparkSlotConfigurator(CustomItemStackHandler inventory,
                                    Supplier<DyeColor> network, Consumer<DyeColor> setNetwork) {
        this.inventory = inventory;
        this.network = network;
        this.setNetwork = setNetwork;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.superflatcore.mana_spark_slot.title");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(BotaniaItems.spark);
    }

    @Override
    public List<Component> getTooltips() {
        return List.of(
                Component.translatable("gui.superflatcore.mana_spark_slot.desc.0"),
                Component.translatable("gui.superflatcore.mana_spark_slot.desc.1"),
                Component.translatable("gui.superflatcore.mana_spark_slot.desc.2"),
                Component.translatable("gui.superflatcore.mana_spark_slot.desc.3"));
    }

    @Override
    public Widget createConfigurator() {
        WidgetGroup group = new WidgetGroup(0, 0, 140, 74);
        WidgetGroup container = new WidgetGroup(4, 4, 132, 66);
        container.addWidget(new SlotWidget(inventory, 0, 4, 4, true, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setIngredientIO(IngredientIO.INPUT));
        container.addWidget(new LabelWidget(28, 9, getTitle()));
        container.addWidget(new LabelWidget(4, 28,
                Component.translatable("gui.superflatcore.mana_spark_slot.network")));
        List<String> colors = Arrays.stream(DyeColor.values())
                .map(color -> "color.minecraft." + color.getName()).toList();
        container.addWidget(new SelectorWidget(4, 42, 124, 16, colors, -1)
                .setButtonBackground(GuiTextures.BUTTON)
                .setMaxCount(4)
                .setValue("color.minecraft." + network.get().getName())
                .setSupplier(() -> "color.minecraft." + network.get().getName())
                .setOnChanged(value -> {
                    int index = colors.indexOf(value);
                    if (index >= 0) {
                        setNetwork.accept(DyeColor.values()[index]);
                    }
                }));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
