package com.oritong.superflatcore.integration.mekanism;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import com.oritong.superflatcore.block.entity.RadioactiveFluidTankBlockEntity;
import com.oritong.superflatcore.integration.radioactive.RadioactiveFluidPolicy;
import com.shiory.atomicembers.feature.reactor.blockentity.ReactorInputBlockEntity;
import com.shiory.atomicembers.feature.reactor.blockentity.ReactorPartBlockEntity;

import mekanism.api.Coord4D;
import mekanism.api.radiation.IRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.HashMap;
import java.util.Map;

/** Bridges SFC's tagged radioactive fluids and Atomic Embers to Mekanism radiation. */
public final class MekanismRadiationIntegration {
    /** Mekanism's nuclear waste gas uses Radiation(0.01), measured in Sv/h per mB. */
    private static final double NUCLEAR_WASTE_RADIOACTIVITY_PER_MB = 0.01D;
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> LAST_REACTOR_RELEASE = new HashMap<>();

    private MekanismRadiationIntegration() {}

    public static void radiate(ServerLevel level, BlockPos pos, double magnitude) {
        if (magnitude <= 0.0D || !IRadiationManager.INSTANCE.isRadiationEnabled()) return;
        IRadiationManager.INSTANCE.radiate(new Coord4D(pos, level), magnitude);
    }

    public static void radiateFluid(ServerLevel level, BlockPos pos, FluidStack stack) {
        if (stack.isEmpty()) return;
        radiate(level, pos, stack.getAmount() * NUCLEAR_WASTE_RADIOACTIVITY_PER_MB);
    }

    public static void radiateRadioactiveFluid(ServerLevel level, BlockPos pos, FluidStack stack) {
        if (RadioactiveFluidPolicy.isRadioactive(stack)) {
            radiateFluid(level, pos, stack);
        }
    }

    public static void radiateRadioactiveFluidHandler(ServerLevel level, BlockPos pos, IFluidHandler handler) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            radiateRadioactiveFluid(level, pos, handler.getFluidInTank(tank));
        }
    }

    public static void radiateRadioactiveAeStack(ServerLevel level, BlockPos pos, GenericStack stack) {
        if (stack == null || !(stack.what() instanceof AEFluidKey fluidKey)
                || !RadioactiveFluidPolicy.isRadioactive(fluidKey.getFluid())) {
            return;
        }
        radiate(level, pos, stack.amount() * NUCLEAR_WASTE_RADIOACTIVITY_PER_MB);
    }

    public static void radiateRadioactiveAeInventory(ServerLevel level, BlockPos pos, GenericStackInv inventory) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            radiateRadioactiveAeStack(level, pos, inventory.getStack(slot));
        }
    }

    /** Emits the share of a tank's contents represented by the block being removed. */
    public static void onTankBroken(ServerLevel level, BlockPos pos, RadioactiveFluidTankBlockEntity broken) {
        var controller = broken.getControllerBE();
        if (!(controller instanceof RadioactiveFluidTankBlockEntity tank)) return;
        FluidStack stored = tank.getTankInventory().getFluid();
        if (stored.isEmpty()) return;
        int totalBlocks = Math.max(1, tank.getTotalTankSize());
        int leakedAmount = broken == tank
                ? stored.getAmount()
                : (int) Math.ceil(stored.getAmount() / (double) totalBlocks);
        FluidStack leaked = stored.copy();
        leaked.setAmount(Math.max(0, leakedAmount));
        radiateFluid(level, pos, leaked);
    }

    /** Called before Atomic Embers disassembles a formed reactor. */
    public static void radiateReactorAt(ServerLevel level, BlockPos brokenPos) {
        ReactorInputBlockEntity reactor = findReactor(level, brokenPos);
        if (reactor == null || !reactor.formed() || reactor.disassembling()) return;

        BlockPos masterPos = reactor.masterPos();
        Map<BlockPos, Long> releases = LAST_REACTOR_RELEASE.computeIfAbsent(
                level.dimension(), ignored -> new HashMap<>());
        long now = level.getGameTime();
        Long previous = releases.get(masterPos);
        if (previous != null && now - previous <= 1L) return;
        releases.put(masterPos.immutable(), now);
        radiateReactor(level, masterPos, reactor);
    }

    /** Emits radiation for an Atomic meltdown/accident exactly when the accident executes. */
    public static void radiateReactor(ServerLevel level, BlockPos pos,
                                      ReactorInputBlockEntity reactor) {
        if (!reactor.formed()) return;
        int fluidAmount = reactor.inputTank().getFluidAmount()
                + reactor.outputTank().getFluidAmount();
        double magnitude = fluidAmount * NUCLEAR_WASTE_RADIOACTIVITY_PER_MB;
        if (magnitude <= 0.0D) {
            magnitude = Math.max(0.0D, reactor.reactorPower())
                    * NUCLEAR_WASTE_RADIOACTIVITY_PER_MB;
        }
        radiate(level, pos, magnitude);
    }

    private static ReactorInputBlockEntity findReactor(ServerLevel level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ReactorInputBlockEntity input) return input;
        if (entity instanceof ReactorPartBlockEntity part && part.formed()) {
            ReactorInputBlockEntity master = part.master();
            if (master != null) return master;
        }
        // The auxiliary tank is directly below the formed output port, not an Atomic member BE.
        entity = level.getBlockEntity(pos.above());
        if (entity instanceof ReactorInputBlockEntity input) return input;
        if (entity instanceof ReactorPartBlockEntity part && part.formed()) {
            ReactorInputBlockEntity master = part.master();
            if (master != null) return master;
        }
        return null;
    }
}
