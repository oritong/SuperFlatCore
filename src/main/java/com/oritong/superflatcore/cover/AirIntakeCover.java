package com.oritong.superflatcore.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AirIntakeCover extends CoverBehavior {
    private static final int UPDATE_INTERVAL = 20;

    private final int transferRate;
    private final int batchTransferAmount;
    private TickableSubscription subscription;

    public AirIntakeCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide,
                          int transferRate) {
        super(definition, coverHolder, attachedSide);
        this.transferRate = transferRate;
        this.batchTransferAmount = toSecondBatchAmount(transferRate);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() &&
                FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getPos(), attachedSide).isPresent();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void update() {
        if (coverHolder.getOffsetTimer() % UPDATE_INTERVAL == 0) {
            FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getPos(), attachedSide)
                    .ifPresent(handler -> handler.fill(GTMaterials.Air.getFluid(batchTransferAmount),
                            IFluidHandler.FluidAction.EXECUTE));
        }
    }

    private static int toSecondBatchAmount(int transferRate) {
        if (transferRate > Integer.MAX_VALUE / UPDATE_INTERVAL) {
            return Integer.MAX_VALUE;
        }
        return transferRate * UPDATE_INTERVAL;
    }
}
