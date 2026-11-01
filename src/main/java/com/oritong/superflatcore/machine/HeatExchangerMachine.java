package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
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
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.oritong.superflatcore.SuperFlatCore;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class HeatExchangerMachine extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HeatExchangerMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    private static final String DISTILLED_WATER_AMOUNT = "superflatcore:heat_exchanger_distilled_water";

    public HeatExchangerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof HeatExchangerMachine)) {
            return ModifierFunction.NULL;
        }

        List<Content> fluidInputs = recipe.inputs.get(FluidRecipeCapability.CAP);
        if (fluidInputs == null || fluidInputs.size() < 2) {
            return ModifierFunction.NULL;
        }

        FluidIngredient hotFluid = FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent());
        FluidIngredient coolant = FluidRecipeCapability.CAP.of(fluidInputs.get(1).getContent());
        if (hotFluid.isEmpty() || coolant.isEmpty() || coolant.getStacks().length == 0) {
            return ModifierFunction.NULL;
        }

        if (coolant.getStacks()[0].getFluid() == GTMaterials.Water.getFluid()) {
            int parallel = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, Integer.MAX_VALUE);
            return parallel > 0
                    ? ignored -> recipe.copy(ContentModifier.multiplier(parallel), false)
                    : ModifierFunction.NULL;
        }

        FluidIngredient[] outputs = recipe.outputs.getOrDefault(FluidRecipeCapability.CAP, List.of()).stream()
                .map(content -> FluidRecipeCapability.CAP.of(content.getContent()))
                .toArray(FluidIngredient[]::new);
        GTRecipe coolingRecipe = new GTRecipeBuilder(
                SuperFlatCore.id("heat_exchanger_cooling"), GTRecipeTypes.DUMMY_RECIPES)
                .inputFluids(hotFluid)
                .outputFluids(outputs)
                .duration(200)
                .buildRawRecipe();
        int parallel = ParallelLogic.getParallelAmountWithoutEU(machine, coolingRecipe, Integer.MAX_VALUE);
        if (parallel <= 0) {
            return ModifierFunction.NULL;
        }

        long distilledWaterAmount = (long) parallel * coolant.getAmount();
        if (distilledWaterAmount <= 0 || distilledWaterAmount > Integer.MAX_VALUE) {
            return ModifierFunction.NULL;
        }

        GTRecipe distilledWaterCheck = new GTRecipeBuilder(
                SuperFlatCore.id("heat_exchanger_distilled_water"), GTRecipeTypes.DUMMY_RECIPES)
                .inputFluids(GTMaterials.DistilledWater.getFluid((int) distilledWaterAmount))
                .buildRawRecipe();
        if (!RecipeHelper.matchRecipe((IRecipeCapabilityHolder) machine, distilledWaterCheck).isSuccess()) {
            return ModifierFunction.NULL;
        }

        GTRecipe modified = coolingRecipe.copy(ContentModifier.multiplier(parallel), false);
        CompoundTag data = modified.data == null ? new CompoundTag() : modified.data.copy();
        data.putInt(DISTILLED_WATER_AMOUNT, (int) distilledWaterAmount);
        modified.data = data;
        return ignored -> modified;
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        if (!super.beforeWorking(recipe)) {
            return false;
        }
        if (recipe == null || recipe.data == null || !recipe.data.contains(DISTILLED_WATER_AMOUNT)) {
            return true;
        }

        int amount = recipe.data.getInt(DISTILLED_WATER_AMOUNT);
        GTRecipe waterRecipe = new GTRecipeBuilder(
                SuperFlatCore.id("heat_exchanger_distilled_water"), GTRecipeTypes.DUMMY_RECIPES)
                .inputFluids(GTMaterials.DistilledWater.getFluid(amount))
                .buildRawRecipe();
        return RecipeHelper.handleRecipeIO(this, waterRecipe, IO.IN,
                getRecipeLogic().getChanceCaches()).isSuccess();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable(getRecipeType().registryName.toLanguageKey())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.gui.machinemode.title")))));
            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                int progress = (int) (recipeLogic.getProgressPercent() * 100);
                textList.add(Component.translatable("gtceu.multiblock.progress", progress));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }
            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.waiting")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                Component waitingReason = recipeLogic.getWaitingReason();
                if (waitingReason != null) {
                    textList.add(waitingReason.copy().withStyle(ChatFormatting.RED));
                }
            }
        } else {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
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
