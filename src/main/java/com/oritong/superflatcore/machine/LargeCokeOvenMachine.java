package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LargeCokeOvenMachine extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LargeCokeOvenMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    private static final int MAX_PARALLELS = 32;

    public LargeCokeOvenMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        int parallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, MAX_PARALLELS);
        if (parallels <= 0) {
            return ModifierFunction.NULL;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .parallels(parallels)
                .build();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (!isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure").withStyle(ChatFormatting.RED));
        } else {
            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                textList.add(Component.translatable("gtceu.multiblock.progress",
                        (int) (recipeLogic.getProgressPercent() * 100)));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }
            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.waiting").withStyle(ChatFormatting.RED));
                Component waitingReason = recipeLogic.getWaitingReason();
                if (waitingReason != null) {
                    textList.add(waitingReason.copy().withStyle(ChatFormatting.RED));
                }
            }
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(150)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 208, this, player)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream()
                .filter(IFancyUIProvider.class::isInstance)
                .map(IFancyUIProvider.class::cast)
                .toList();
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
