package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.oritong.superflatcore.config.SFConfig;
import com.oritong.superflatcore.block.WindGeneratorCollisionBlock;
import com.oritong.superflatcore.item.WindRotorItem;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import mekanism.generators.common.tile.TileEntityWindGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

public class WindGeneratorMachine extends TieredEnergyMachine implements IControllable, IFancyUIMachine {
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WindGeneratorMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);
    private static final long ENERGY_PER_AMP = GTValues.V[GTValues.LV];

    public final NotifiableItemStackHandler rotorSlot;
    private final int baseProduction;

    @Persisted
    @DescSynced
    private boolean workingEnabled = true;

    @DescSynced
    private boolean windAvailable;

    @DescSynced
    private boolean generating;

    @DescSynced
    private boolean rotorMoving;

    @Persisted
    @DescSynced
    private double rotorAngle;

    private double productionRemainder;

    private int rotorWearMode;

    private int rotorWearTicks;

    private TickableSubscription windGeneratorSubscription;

    public WindGeneratorMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        baseProduction = SFConfig.WIND_GENERATOR_BASE_PRODUCTION.get();
        energyContainer.setSideOutputCondition(side -> side == getFrontFacing());
        rotorSlot = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH)
                .setFilter(WindGeneratorMachine::isWindRotor);
        attachTraits(rotorSlot);
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long output = SFConfig.WIND_GENERATOR_BASE_PRODUCTION.get();
        long outputAmperage = Math.max(1, (output + ENERGY_PER_AMP - 1) / ENERGY_PER_AMP);
        return NotifiableEnergyContainer.emitterContainer(this, output * 64, ENERGY_PER_AMP, outputAmperage);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 100, 100);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new LabelWidget(5, 6, Component.translatable("block.superflatcore.wind_generator")));
        group.addWidget(new LabelWidget(5, 24, () -> Component.translatable(
                "gui.superflatcore.wind_generator.height", getHolder().pos().getY()).getString()));
        group.addWidget(new LabelWidget(5, 40, () -> Component.translatable(
                "gui.superflatcore.wind_generator.base_production", baseProduction).getString()));
        group.addWidget(new LabelWidget(5, 56, () -> Component.translatable(
                "gui.superflatcore.wind_generator.actual_production",
                String.format(Locale.ROOT, "%.1f", getActualProduction())).getString()));
        group.addWidget(new SlotWidget(rotorSlot, 0, 78, 76, true, true)
                .setBackgroundTexture(GuiTextures.SLOT));
        return group;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            WindGeneratorCollisionBlock.placeFor(getLevel(), getHolder().pos());
            windGeneratorSubscription = subscribeServerTick(this::tickWindGenerator);
        }
    }

    @Override
    public void onUnload() {
        if (windGeneratorSubscription != null) {
            windGeneratorSubscription.unsubscribe();
            windGeneratorSubscription = null;
        }
        super.onUnload();
    }

    private void tickWindGenerator() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        BlockPos position = getHolder().pos();
        BlockPos rotorTop = position.above(4);
        boolean hasWind = position.getY() >= 0 && level.getFluidState(rotorTop).isEmpty()
                && level.canSeeSky(rotorTop);
        setWindAvailable(hasWind);

        ItemStack rotor = rotorSlot.getStackInSlot(0);
        WindRotorItem rotorBehaviour = rotor.getItem() instanceof WindRotorItem windRotor ? windRotor : null;
        int rotorPower = rotorBehaviour == null ? 0 : rotorBehaviour.getRotorPower(rotor);
        boolean rotorUsable = isWindRotor(rotor) && rotorPower > 0
                && rotorBehaviour.getRotorDurabilityPercent(rotor) > 0;
        boolean full = energyContainer.getEnergyStored() >= energyContainer.getEnergyCapacity();
        boolean validOperatingConditions = workingEnabled && hasWind && rotorUsable;
        boolean canGenerate = validOperatingConditions && !full;
        setRotorMoving(validOperatingConditions);
        setGenerating(canGenerate);

        int wearMode = canGenerate ? 1 : validOperatingConditions && full ? 2 : 0;
        tickRotorWear(wearMode, rotorBehaviour, rotor);
        if (validOperatingConditions) {
            rotorAngle = (rotorAngle + (position.getY() + 4.0) / TileEntityWindGenerator.SPEED_SCALED) % 360.0;
        }

        if (!canGenerate) {
            productionRemainder = 0;
            return;
        }

        double heightEfficiency = heightEfficiency(position.getY());
        double rotorPowerMultiplier = rotorPower / 100.0;
        double energyToGenerate = productionRemainder
                + baseProduction * heightEfficiency * rotorPowerMultiplier;
        long wholeEnergy = (long) energyToGenerate;
        productionRemainder = energyToGenerate - wholeEnergy;
        if (wholeEnergy > 0) {
            long accepted = energyContainer.changeEnergy(wholeEnergy);
            if (accepted < wholeEnergy) {
                productionRemainder = 0;
            }
        }
    }

    private void tickRotorWear(int mode, WindRotorItem rotorBehaviour, ItemStack rotor) {
        if (mode == 0 || rotorBehaviour == null) {
            rotorWearMode = 0;
            rotorWearTicks = 0;
            return;
        }
        if (rotorWearMode != mode) {
            rotorWearMode = mode;
            rotorWearTicks = 0;
        }
        if (++rotorWearTicks >= 20) {
            int damage = mode == 1 ? SFConfig.WIND_GENERATOR_ACTIVE_ROTOR_WEAR.get()
                    : SFConfig.WIND_GENERATOR_FULL_ROTOR_WEAR.get();
            rotorBehaviour.applyRotorDamage(rotor, damage);
            rotorSlot.setStackInSlot(0, rotor);
            rotorWearTicks = 0;
        }
    }

    private void setWindAvailable(boolean available) {
        if (windAvailable != available) {
            windAvailable = available;
            markDirty();
        }
    }

    private void setGenerating(boolean active) {
        if (generating == active) {
            return;
        }
        generating = active;
        setRenderState(getRenderState().setValue(RecipeLogic.STATUS_PROPERTY,
                active ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
    }

    private void setRotorMoving(boolean active) {
        if (rotorMoving != active) {
            rotorMoving = active;
            markDirty();
        }
    }

    private static double heightEfficiency(int y) {
        if (y < 0) {
            return 0.0;
        }
        return y >= 128 ? 1.0 : 0.35 + 0.65 * y / 128.0;
    }

    private double getActualProduction() {
        if (!generating) {
            return 0.0;
        }
        ItemStack rotor = rotorSlot.getStackInSlot(0);
        if (!(rotor.getItem() instanceof WindRotorItem windRotor)) {
            return 0.0;
        }
        return baseProduction * heightEfficiency(getHolder().pos().getY())
                * Math.max(0, windRotor.getRotorPower(rotor)) / 100.0;
    }

    private static boolean isWindRotor(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof WindRotorItem;
    }

    public boolean isWindAvailable() {
        return windAvailable;
    }

    public boolean isGenerating() {
        return generating;
    }

    public boolean isRotorMoving() {
        return rotorMoving;
    }

    public double getRotorAngle() {
        return rotorAngle;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (workingEnabled != isWorkingAllowed) {
            workingEnabled = isWorkingAllowed;
            markDirty();
        }
    }
}
