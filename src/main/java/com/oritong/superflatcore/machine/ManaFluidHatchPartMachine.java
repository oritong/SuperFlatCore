package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.oritong.superflatcore.integration.botania.ManaSparkSlotConfigurator;
import com.oritong.superflatcore.integration.mana.ManaFluidPolicy;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class ManaFluidHatchPartMachine extends FluidHatchPartMachine implements ManaReceiver {
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ManaFluidHatchPartMachine.class, FluidHatchPartMachine.MANAGED_FIELD_HOLDER);
    public static final int CAPACITY = 512000;
    private static final int SPARK_TRANSFER_RATE = 1000;
    private static final Logger LOGGER = LogManager.getLogger();
    private final IO io;

    @Persisted
    private final NotifiableItemStackHandler sparkSlot;
    private TickableSubscription sparkSubscription;
    private boolean sparkTransferLogged;
    private String sparkTransferStatus;

    @Persisted
    @DescSynced
    private int sparkNetwork = DyeColor.WHITE.getId();

    public ManaFluidHatchPartMachine(IMachineBlockEntity holder, IO io) {
        // Pass IO through the parent varargs because createTank runs during
        // the parent constructor, before subclass fields are initialized.
        super(holder, com.gregtechceu.gtceu.api.GTValues.IV, io, CAPACITY, 1, io);
        this.io = io;
        sparkSlot = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE, slots ->
                new CustomItemStackHandler(slots) {
                    @Override
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                }).setFilter(stack -> stack.is(BotaniaItems.spark));
        Runnable originalChangeHandler = sparkSlot.storage.getOnContentsChanged();
        sparkSlot.storage.setOnContentsChanged(() -> {
            originalChangeHandler.run();
            if (!isRemote()) {
                markDirty();
                sparkTransferLogged = false;
                sparkTransferStatus = null;
                ItemStack installed = sparkSlot.getStackInSlot(0);
                LOGGER.info("Mana input hatch spark catalyst changed: pos={}, item={}, count={}",
                        getPos(), installed.isEmpty() ? "empty" : itemId(installed), installed.getCount());
            }
        });
        attachTraits(sparkSlot);
    }

    public boolean isInputHatch() {
        return io == IO.IN;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public DyeColor getSparkNetwork() {
        return DyeColor.byId(sparkNetwork);
    }

    public void setSparkNetwork(DyeColor color) {
        if (sparkNetwork != color.getId()) {
            sparkNetwork = color.getId();
            sparkTransferLogged = false;
            sparkTransferStatus = null;
            markDirty();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isInputHatch() && !isRemote()) {
            sparkSubscription = subscribeServerTick(this::pullManaFromSparkNetwork);
            LOGGER.info("Mana input hatch spark tick hook installed: pos={}, network={}, installed={}, stored={}",
                    getPos(), getSparkNetwork().getName(), sparkSlot.getStackInSlot(0).getCount(), getCurrentMana());
        }
    }

    @Override
    public void onUnload() {
        if (sparkSubscription != null) {
            sparkSubscription.unsubscribe();
            sparkSubscription = null;
        }
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        clearInventory(sparkSlot.storage);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        if (isInputHatch()) {
            configuratorPanel.attachConfigurators(new ManaSparkSlotConfigurator(
                    sparkSlot.storage, this::getSparkNetwork, this::setSparkNetwork));
        }
    }

    @Override
    protected NotifiableFluidTank createTank(int ignoredCapacity, int slots, Object... args) {
        IO tankIo = args.length > 0 && args[0] instanceof IO configuredIo ? configuredIo : IO.NONE;
        return new NotifiableFluidTank(this,
                List.of(new ManaTank(CAPACITY)), tankIo, tankIo);
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getPos();
    }

    @Override
    public int getCurrentMana() {
        FluidStack stored = tank.getFluidInTank(0);
        return ManaFluidPolicy.isMana(stored) ? stored.getAmount() : 0;
    }

    @Override
    public boolean isFull() {
        return !canReceiveManaFromBursts() || getCurrentMana() >= CAPACITY;
    }

    @Override
    public void receiveMana(int mana) {
        if (isRemote() || !canReceiveManaFromBursts() || mana <= 0) {
            return;
        }
        var manaFluid = ForgeRegistries.FLUIDS.getValue(ManaFluidPolicy.MANA_FLUID_ID);
        if (manaFluid != null) {
            int accepted = tank.fillInternal(new FluidStack(manaFluid, mana), IFluidHandler.FluidAction.EXECUTE);
            LOGGER.debug("Mana input hatch received burst: pos={}, offered={}, accepted={}, stored={}",
                    getPos(), mana, accepted, getCurrentMana());
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return isInputHatch() && sparkSlot.getStackInSlot(0).is(BotaniaItems.spark)
                && getCurrentMana() < CAPACITY;
    }

    private void pullManaFromSparkNetwork() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (!sparkSlot.getStackInSlot(0).is(BotaniaItems.spark)) {
            logSparkTransferStatus("no_installed_spark", 0, 0, 0);
            return;
        }
        if (getCurrentMana() >= CAPACITY) {
            logSparkTransferStatus("full", 0, 0, 0);
            return;
        }
        var manaFluid = ForgeRegistries.FLUIDS.getValue(ManaFluidPolicy.MANA_FLUID_ID);
        if (manaFluid == null) {
            logSparkTransferStatus("missing_mana_fluid", 0, 0, 0);
            return;
        }

        BlockPos pos = getPos();
        var sparks = SparkHelper.getSparksAround(level, pos.getX() + 0.5,
                pos.getY() + 1.5, pos.getZ() + 0.5, getSparkNetwork());
        Set<ManaPool> visitedPools = Collections.newSetFromMap(new IdentityHashMap<>());
        int received = 0;
        for (var spark : sparks) {
            if (!spark.entity().isAlive() || spark.getUpgrade() == SparkUpgradeType.ISOLATED
                    || spark.getAttachedTile() == null
                    || !(spark.getAttachedManaReceiver() instanceof ManaPool pool)
                    || !visitedPools.add(pool)) {
                continue;
            }

            int available = pool.getCurrentMana();
            int requested = Math.min(SPARK_TRANSFER_RATE, available);
            if (requested <= 0) {
                continue;
            }
            requested = tank.fillInternal(new FluidStack(manaFluid, requested), IFluidHandler.FluidAction.SIMULATE);
            if (requested == 0) {
                break;
            }

            boolean creative = level.getBlockState(pool.getManaReceiverPos()).is(BotaniaBlocks.creativePool);
            int extracted = requested;
            if (!creative) {
                pool.receiveMana(-requested);
                extracted = Math.min(requested, Math.max(0, available - pool.getCurrentMana()));
            }
            if (extracted == 0) {
                continue;
            }
            int accepted = tank.fillInternal(new FluidStack(manaFluid, extracted), IFluidHandler.FluidAction.EXECUTE);
            if (!creative && accepted < extracted) {
                pool.receiveMana(extracted - accepted);
            }
            received += accepted;
        }

        if (received > 0 && !sparkTransferLogged) {
            sparkTransferLogged = true;
            LOGGER.info("Mana input hatch spark network transfer: pos={}, network={}, sparks={}, received={}, stored={}",
                    pos, getSparkNetwork().getName(), sparks.size(), received, getCurrentMana());
        }
        String status = received > 0 ? "transferring"
                : sparks.isEmpty() ? "no_matching_sparks"
                : visitedPools.isEmpty() ? "no_eligible_pools" : "no_mana_transferred";
        logSparkTransferStatus(status, sparks.size(), visitedPools.size(), received);
    }

    private void logSparkTransferStatus(String status, int sparks, int pools, int received) {
        if (!status.equals(sparkTransferStatus)) {
            sparkTransferStatus = status;
            LOGGER.info("Mana input hatch spark network probe: pos={}, state={}, network={}, sparks={}, pools={}, received={}, stored={}",
                    getPos(), status, getSparkNetwork().getName(), sparks, pools, received, getCurrentMana());
        }
    }

    private static ResourceLocation itemId(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    private static final class ManaTank extends CustomFluidTank {
        private ManaTank(int capacity) {
            super(capacity, ManaFluidPolicy::isMana);
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(ManaFluidPolicy.isMana(stack) ? stack : FluidStack.EMPTY);
        }
    }
}
