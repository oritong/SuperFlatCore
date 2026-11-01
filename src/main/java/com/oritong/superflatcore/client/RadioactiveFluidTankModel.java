package com.oritong.superflatcore.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.oritong.superflatcore.SuperFlatCore;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public final class RadioactiveFluidTankModel extends CTModel {
    private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

    private RadioactiveFluidTankModel(BakedModel originalModel, CTSpriteShiftEntry side,
                                      CTSpriteShiftEntry top, CTSpriteShiftEntry inner) {
        super(originalModel, new FluidTankCTBehaviour(side, top, inner));
    }

    public static RadioactiveFluidTankModel standard(BakedModel originalModel) {
        return new RadioactiveFluidTankModel(originalModel,
                shift("radioactive_fluid_tank"),
                shift("radioactive_fluid_tank_top"),
                shift("radioactive_fluid_tank_inner"));
    }

    private static CTSpriteShiftEntry shift(String name) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE,
                SuperFlatCore.id("block/" + name),
                SuperFlatCore.id("block/" + name + "_connected"));
    }

    @Override
    protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos,
                                      BlockState state, ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        CullData cullData = new CullData();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            cullData.setCulled(direction, ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
        }
        return builder.with(CULL_PROPERTY, cullData);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand,
                                    ModelData extraData, RenderType renderType) {
        if (side != null) {
            return Collections.emptyList();
        }
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!extraData.has(CULL_PROPERTY) || !extraData.get(CULL_PROPERTY).isCulled(direction)) {
                quads.addAll(super.getQuads(state, direction, rand, extraData, renderType));
            }
        }
        quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
        return quads;
    }

    private static final class CullData {
        private final boolean[] culledFaces = new boolean[4];

        private CullData() {
            Arrays.fill(culledFaces, false);
        }

        private void setCulled(Direction face, boolean cull) {
            if (face.getAxis().isVertical()) {
                return;
            }
            culledFaces[face.get2DDataValue()] = cull;
        }

        private boolean isCulled(Direction face) {
            return !face.getAxis().isVertical() && culledFaces[face.get2DDataValue()];
        }
    }
}
