package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConfigurableAutoMaintenanceHatchPartMachine extends MaintenanceHatchPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ConfigurableAutoMaintenanceHatchPartMachine.class, MaintenanceHatchPartMachine.MANAGED_FIELD_HOLDER);

    private static final float DEFAULT_MAX_DURATION_MULTIPLIER = 1.5f;
    private static final float DEFAULT_MIN_DURATION_MULTIPLIER = 0.5f;
    private static final float CREATIVE_MAX_DURATION_MULTIPLIER = 100f;
    private static final float CREATIVE_MIN_DURATION_MULTIPLIER = 0.001f;
    private static final float DURATION_ACTION_AMOUNT = 0.01f;

    private final float minDurationMultiplier;
    private final float maxDurationMultiplier;
    private final CleanroomType cleanroomType;
    private final ICleanroomProvider cleanroomProvider;

    @Persisted
    private float durationMultiplier = 1f;

    public ConfigurableAutoMaintenanceHatchPartMachine(IMachineBlockEntity holder) {
        this(holder, null, DEFAULT_MIN_DURATION_MULTIPLIER, DEFAULT_MAX_DURATION_MULTIPLIER);
    }

    public ConfigurableAutoMaintenanceHatchPartMachine(IMachineBlockEntity holder, CleanroomType cleanroomType) {
        this(holder, cleanroomType, DEFAULT_MIN_DURATION_MULTIPLIER, DEFAULT_MAX_DURATION_MULTIPLIER);
    }

    public static ConfigurableAutoMaintenanceHatchPartMachine creativeSterile(IMachineBlockEntity holder) {
        return new ConfigurableAutoMaintenanceHatchPartMachine(holder, CleanroomType.STERILE_CLEANROOM,
                CREATIVE_MIN_DURATION_MULTIPLIER, CREATIVE_MAX_DURATION_MULTIPLIER);
    }

    private ConfigurableAutoMaintenanceHatchPartMachine(IMachineBlockEntity holder, CleanroomType cleanroomType,
                                                        float minDurationMultiplier, float maxDurationMultiplier) {
        super(holder, true);
        this.cleanroomType = cleanroomType;
        this.minDurationMultiplier = minDurationMultiplier;
        this.maxDurationMultiplier = maxDurationMultiplier;
        this.cleanroomProvider = cleanroomType == null ? null :
                DummyCleanroom.createForTypes(Collections.singletonList(cleanroomType));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean isFullAuto() {
        return true;
    }

    @Override
    public byte startProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public byte getMaintenanceProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public void setMaintenanceProblems(byte problems) {}

    @Override
    public int getTimeActive() {
        return 0;
    }

    @Override
    public void setTimeActive(int time) {}

    @Override
    public void setTaped(boolean ignored) {}

    @Override
    public boolean isTaped() {
        return false;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (cleanroomProvider != null && controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(cleanroomProvider);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (cleanroomProvider != null && controller instanceof ICleanroomReceiver receiver &&
                receiver.getCleanroom() == cleanroomProvider) {
            receiver.setCleanroom(null);
        }
    }

    @Override
    public int getTier() {
        if (cleanroomType == CleanroomType.CLEANROOM) {
            return 8;
        }
        if (cleanroomType == CleanroomType.STERILE_CLEANROOM) {
            return 9;
        }
        return super.getTier();
    }

    @Override
    public float getDurationMultiplier() {
        return durationMultiplier;
    }

    public void setDurationMultiplier(float durationMultiplier) {
        this.durationMultiplier = clampDurationMultiplier(durationMultiplier);
    }

    @Override
    public float getTimeMultiplier() {
        return BigDecimal.valueOf(Math.max(0.01f, durationMultiplier < 1.0f ?
                -20 * durationMultiplier + 21 :
                -8 * durationMultiplier + 9))
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (hasMaintenanceProblems()) {
                return null;
            }
            if (durationMultiplier != 1.0f) {
                recipe = recipe.copy();
                recipe.duration = Math.max(1, Math.round(recipe.duration * durationMultiplier));
            }
        }
        return recipe;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 150, 70);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 142, 62)
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(new ComponentPanelWidget(4, 5, list -> {
                    list.add(getDurationText(this::getDurationMultiplier));

                    MutableComponent controls = Component.translatable("gtceu.maintenance.configurable_duration.modify");
                    controls.append(" ");
                    controls.append(ComponentPanelWidget.withButton(Component.literal("[-]"), "sub"));
                    controls.append(" ");
                    controls.append(ComponentPanelWidget.withButton(Component.literal("[+]"), "add"));
                    list.add(controls);
                })
                        .setMaxWidthLimit(130)
                        .clickHandler((componentData, clickData) -> {
                            if (!clickData.isRemote) {
                                if ("sub".equals(componentData)) {
                                    if (clickData.isCtrlClick && clickData.isShiftClick) {
                                        setDurationMultiplier(minDurationMultiplier);
                                    } else {
                                        addDurationMultiplier(-getStepMultiplier(clickData.isShiftClick,
                                                clickData.isCtrlClick));
                                    }
                                } else if ("add".equals(componentData)) {
                                    if (clickData.isCtrlClick && clickData.isShiftClick) {
                                        setDurationMultiplier(maxDurationMultiplier);
                                    } else {
                                        addDurationMultiplier(getStepMultiplier(clickData.isShiftClick,
                                                clickData.isCtrlClick));
                                    }
                                }
                            }
                        })));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDurationMultiplier(int stepMultiplier) {
        setDurationMultiplier(durationMultiplier + DURATION_ACTION_AMOUNT * stepMultiplier);
    }

    private int getStepMultiplier(boolean isShiftClick, boolean isCtrlClick) {
        return isCtrlClick ? 100 : isShiftClick ? 10 : 1;
    }

    private float clampDurationMultiplier(float durationMultiplier) {
        if (durationMultiplier > maxDurationMultiplier) {
            return maxDurationMultiplier;
        }
        if (durationMultiplier < minDurationMultiplier) {
            return minDurationMultiplier;
        }
        return durationMultiplier;
    }

    private static Component getDurationText(Supplier<Float> multiplier) {
        Component tooltip = multiplier.get() == 1.0f ?
                Component.translatable("gtceu.maintenance.configurable_duration.unchanged_description") :
                Component.translatable("gtceu.maintenance.configurable_duration.changed_description", multiplier.get());

        return Component.translatable("gtceu.maintenance.configurable_duration", multiplier.get())
                .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }
}
