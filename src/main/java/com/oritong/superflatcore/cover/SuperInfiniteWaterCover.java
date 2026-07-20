package com.oritong.superflatcore.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SuperInfiniteWaterCover extends CoverBehavior {
    private TickableSubscription subscription;

    public SuperInfiniteWaterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
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
        if (coverHolder.getOffsetTimer() % 20 == 0) {
            FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getPos(), attachedSide)
                    .ifPresent(handler -> handler.fill(new FluidStack(Fluids.WATER, Integer.MAX_VALUE),
                            IFluidHandler.FluidAction.EXECUTE));
        }
    }
}
