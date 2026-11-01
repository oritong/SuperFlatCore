package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.oritong.superflatcore.integration.radioactive.IRadioactiveFluidStorage;
import com.oritong.superflatcore.integration.radioactive.RadioactiveFluidPolicy;
import net.minecraftforge.fluids.FluidStack;
import java.util.List;

public final class RadioactiveFluidHatchPartMachine extends FluidHatchPartMachine {
    public static final int CAPACITY = 256000;
    private final IO io;

    public RadioactiveFluidHatchPartMachine(IMachineBlockEntity holder, IO io) {
        // Pass IO through the parent varargs: createTank is invoked from the
        // parent constructor before subclass fields have been initialized.
        super(holder, com.gregtechceu.gtceu.api.GTValues.IV, io, CAPACITY, 1, io);
        this.io = io;
    }

    @Override
    protected NotifiableFluidTank createTank(int ignoredCapacity, int slots, Object... args) {
        IO tankIo = args.length > 0 && args[0] instanceof IO configuredIo ? configuredIo : IO.NONE;
        return new NotifiableFluidTank(this,
                List.of(new RadioactiveTank(CAPACITY)), tankIo, tankIo);
    }

    private static final class RadioactiveTank extends CustomFluidTank implements IRadioactiveFluidStorage {
        private RadioactiveTank(int capacity) {
            super(capacity, RadioactiveFluidPolicy::isRadioactive);
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(RadioactiveFluidPolicy.isRadioactive(stack) ? stack : FluidStack.EMPTY);
        }
    }
}
