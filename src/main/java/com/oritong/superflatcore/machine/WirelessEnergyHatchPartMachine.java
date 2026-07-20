package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WirelessEnergyHatchPartMachine extends EnergyHatchPartMachine
        implements IInteractedMachine, IMachineLife, IWirelessEnergyContainerHolder {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessEnergyHatchPartMachine.class, EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    private @Nullable WirelessEnergyContainer wirelessEnergyContainerCache;
    private TickableSubscription wirelessEnergySubscription;

    public WirelessEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage) {
        super(holder, tier, io, amperage);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateEnergySubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribeWirelessEnergy();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (isRemote()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(GTItems.TOOL_DATA_STICK.get())) {
            return InteractionResult.PASS;
        }
        setOwnerUUID(player.getUUID());
        setWirelessEnergyContainerCache(null);
        updateEnergySubscription();
        player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind",
                player.getName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClick(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
        if (isRemote()) {
            return false;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(GTItems.TOOL_DATA_STICK.get())) {
            return false;
        }
        setOwnerUUID(null);
        setWirelessEnergyContainerCache(null);
        unsubscribeWirelessEnergy();
        player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"));
        return true;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            setOwnerUUID(player.getUUID());
            setWirelessEnergyContainerCache(null);
            updateEnergySubscription();
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    private void updateEnergySubscription() {
        if (isRemote() || getOwnerUUID() == null) {
            unsubscribeWirelessEnergy();
            return;
        }
        if (wirelessEnergySubscription == null || !wirelessEnergySubscription.isStillSubscribed()) {
            wirelessEnergySubscription = subscribeServerTick(this::transferWirelessEnergy);
        }
    }

    private void unsubscribeWirelessEnergy() {
        if (wirelessEnergySubscription != null) {
            wirelessEnergySubscription.unsubscribe();
            wirelessEnergySubscription = null;
        }
    }

    private void transferWirelessEnergy() {
        UUID owner = getOwnerUUID();
        if (owner == null) {
            unsubscribeWirelessEnergy();
            return;
        }
        if (io == IO.IN) {
            pullEnergy(owner);
        } else {
            pushEnergy(owner);
        }
    }

    private void pullEnergy(UUID owner) {
        long currentStored = energyContainer.getEnergyStored();
        long space = energyContainer.getEnergyCapacity() - currentStored;
        long transfer = Math.min(space, transferRate(energyContainer.getInputVoltage(),
                energyContainer.getInputAmperage()));
        if (transfer <= 0) {
            return;
        }
        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container == null) {
            return;
        }
        long moved = container.removeEnergy(transfer, this);
        if (moved > 0) {
            energyContainer.setEnergyStored(currentStored + moved);
        }
    }

    private void pushEnergy(UUID owner) {
        long currentStored = energyContainer.getEnergyStored();
        long transfer = Math.min(currentStored, transferRate(energyContainer.getOutputVoltage(),
                energyContainer.getOutputAmperage()));
        if (transfer <= 0) {
            return;
        }
        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container == null) {
            return;
        }
        long moved = container.addEnergy(transfer, this);
        if (moved > 0) {
            energyContainer.setEnergyStored(currentStored - moved);
        }
    }

    private static long transferRate(long voltage, long amperage) {
        if (voltage <= 0 || amperage <= 0) {
            return 0;
        }
        if (voltage > Long.MAX_VALUE / amperage) {
            return Long.MAX_VALUE;
        }
        return voltage * amperage;
    }

    @Override
    public void setWirelessEnergyContainerCache(@Nullable WirelessEnergyContainer container) {
        this.wirelessEnergyContainerCache = container;
    }

    @Override
    public @Nullable WirelessEnergyContainer getWirelessEnergyContainerCache() {
        return wirelessEnergyContainerCache;
    }

    @Override
    public @Nullable UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }
}
