package com.oritong.superflatcore.integration.atomic;

import com.oritong.superflatcore.registry.SFBlocks;
import com.shiory.atomicembers.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Standard Atomic reactor layout used by SFC. A is below the output port. */
public final class AtomicReactorStructureIntegration {
    private AtomicReactorStructureIntegration() {}

    public static boolean validateStandard(ServerLevel level, BlockPos master, Direction facing) {
        if (!matches(level, at(master, facing, 0, 0, 0), ModBlocks.REACTOR_INPUT.get())
                || !matches(level, at(master, facing, 5, 0, 0), ModBlocks.REACTOR_OUTPUT.get())
                || !matches(level, at(master, facing, 5, -1, 0), SFBlocks.RADIOACTIVE_FLUID_TANK.get())) return false;

        // x=1 and x=4: "CCC", "CDC", "CCC"
        for (int x : new int[] {1, 4}) {
            if (!all(level, master, facing, x, -1, block("mekanismgenerators:fission_reactor_casing"))) return false;
            if (!all(level, master, facing, x, 1, block("mekanismgenerators:fission_reactor_casing"))) return false;
            if (!matches(level, at(master, facing, x, 0, -1), block("mekanismgenerators:fission_reactor_casing"))
                    || !matches(level, at(master, facing, x, 0, 0), block("embers:dawnstone_block"))
                    || !matches(level, at(master, facing, x, 0, 1), block("mekanismgenerators:fission_reactor_casing"))) return false;
        }

        // x=2 and x=3: "CCC", "EFE", "CGC"
        for (int x : new int[] {2, 3}) {
            if (!all(level, master, facing, x, -1, block("mekanismgenerators:fission_reactor_casing"))) return false;
            if (!matches(level, at(master, facing, x, 0, -1), block("mekanismgenerators:reactor_glass"))
                    || !matches(level, at(master, facing, x, 0, 0), block("mekanismgenerators:fission_fuel_assembly"))
                    || !matches(level, at(master, facing, x, 0, 1), block("mekanismgenerators:reactor_glass"))
                    || !matches(level, at(master, facing, x, 1, -1), block("mekanismgenerators:fission_reactor_casing"))
                    || !matches(level, at(master, facing, x, 1, 0), block("mekanismgenerators:control_rod_assembly"))
                    || !matches(level, at(master, facing, x, 1, 1), block("mekanismgenerators:fission_reactor_casing"))) return false;
        }

        // x=5: " A ", " B ", "   "; A is below B in the requested layout.
        return matches(level, at(master, facing, 5, -1, 0), SFBlocks.RADIOACTIVE_FLUID_TANK.get())
                && matches(level, at(master, facing, 5, 0, 0), ModBlocks.REACTOR_OUTPUT.get());
    }

    public static boolean auxiliaryTankIntact(ServerLevel level, BlockPos master, Direction facing) {
        return matches(level, at(master, facing, 5, -1, 0), SFBlocks.RADIOACTIVE_FLUID_TANK.get());
    }

    private static boolean all(ServerLevel level, BlockPos master, Direction facing,
                               int x, int y, Block block) {
        return matches(level, at(master, facing, x, y, -1), block)
                && matches(level, at(master, facing, x, y, 0), block)
                && matches(level, at(master, facing, x, y, 1), block);
    }

    private static boolean matches(ServerLevel level, BlockPos pos, Block block) {
        return !block.defaultBlockState().isAir() && level.getBlockState(pos).is(block);
    }

    private static Block block(String id) {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(id));
        return block == null ? Blocks.AIR : block;
    }

    private static BlockPos at(BlockPos master, Direction facing, int x, int y, int z) {
        Direction right = facing.getClockWise();
        return master.offset(facing.getStepX() * x + right.getStepX() * z, y,
                facing.getStepZ() * x + right.getStepZ() * z);
    }
}
