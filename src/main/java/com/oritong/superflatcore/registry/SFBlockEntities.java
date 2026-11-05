package com.oritong.superflatcore.registry;

import com.oritong.superflatcore.blackhole.BlackHoleBlockEntity;
import com.oritong.superflatcore.block.entity.RadioactiveFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public final class SFBlockEntities {
    public static final BlockEntityEntry<BlackHoleBlockEntity> BLACK_HOLE =
            SFRegistrate.REGISTRATE.blockEntity("black_hole", BlackHoleBlockEntity::new)
                    .validBlocks(SFBlocks.BLACK_HOLE)
                    .register();

    public static final BlockEntityEntry<RadioactiveFluidTankBlockEntity> RADIOACTIVE_FLUID_TANK =
            SFRegistrate.REGISTRATE.blockEntity("radioactive_fluid_tank", RadioactiveFluidTankBlockEntity::new)
                    .validBlocks(SFBlocks.RADIOACTIVE_FLUID_TANK)
                    .renderer(() -> FluidTankRenderer::new)
                    .register();

    private SFBlockEntities() {}

    public static void init() {}
}
