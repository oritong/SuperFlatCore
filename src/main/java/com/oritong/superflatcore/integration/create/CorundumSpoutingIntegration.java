package com.oritong.superflatcore.integration.create;

import com.oritong.superflatcore.block.CorundumClusterBlock;
import com.oritong.superflatcore.integration.mana.ManaFluidPolicy;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fluids.FluidStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class CorundumSpoutingIntegration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MANA_PER_INJECTION = 325;
    private static final float[] GROWTH_CHANCES = {0.75F, 0.65F, 0.55F};
    private static final List<ElementDefinition> DEFINITIONS = List.of(
            new ElementDefinition("water", "botania:metamorphic_swamp_stone", "botania:manasteel_block"),
            new ElementDefinition("wood", "botania:metamorphic_forest_stone", "botania:mana_diamond_block"),
            new ElementDefinition("fire", "botania:metamorphic_desert_stone", "tconstruct:pig_iron_block"),
            new ElementDefinition("earth", "botania:metamorphic_fungal_stone", "embers:dawnstone_block"),
            new ElementDefinition("metal", "botania:metamorphic_mountain_stone", "ars_nouveau:source_gem_block"));

    private CorundumSpoutingIntegration() {}

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CorundumSpoutingIntegration::registerBehaviours);
    }

    public static void startSpoutProcessIfNeeded(SpoutBlockEntity spout) {
        Level level = spout.getLevel();
        if (level == null || level.isClientSide || spout.isVirtual() || spout.processingTicks != -1) {
            return;
        }

        BlockPos targetPos = spout.getBlockPos().below(2);
        BlockSpoutingBehaviour behaviour = BlockSpoutingBehaviour.get(level, targetPos);
        if (!(behaviour instanceof CorundumSpoutingBehaviour)) {
            return;
        }

        FluidStack fluid = spout.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP)
                .resolve()
                .map(handler -> handler.getFluidInTank(0))
                .orElse(FluidStack.EMPTY);
        if (fluid.isEmpty() || behaviour.fillBlock(level, targetPos, spout, fluid.copy(), true) <= 0) {
            return;
        }

        spout.processingTicks = 20;
        spout.customProcess = behaviour;
        spout.notifyUpdate();
    }

    private static void registerBehaviours() {
        List<Element> elements = new ArrayList<>();
        for (ElementDefinition definition : DEFINITIONS) {
            Element element = resolve(definition);
            if (element == null) {
                continue;
            }
            elements.add(element);
            CorundumSpoutingBehaviour behaviour = new CorundumSpoutingBehaviour(List.of(element), false);
            BlockSpoutingBehaviour.BY_BLOCK.register(element.substrate(), behaviour);
        }

        if (!elements.isEmpty()) {
            CorundumSpoutingBehaviour behaviour = new CorundumSpoutingBehaviour(elements, true);
            BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.BLACKSTONE, behaviour);
        }
        LOGGER.info("Registered Create corundum spouting for {} substrates", elements.size() * 2);
    }

    private static Element resolve(ElementDefinition definition) {
        Block substrate = block(definition.substrateId());
        Block base = block(definition.baseId());
        Block[] stages = new Block[CorundumClusterBlock.Size.values().length];
        for (CorundumClusterBlock.Size size : CorundumClusterBlock.Size.values()) {
            String name = size == CorundumClusterBlock.Size.CLUSTER
                    ? definition.element() + "_corundum_cluster"
                    : size.name().toLowerCase() + "_" + definition.element() + "_corundum_cluster";
            stages[size.ordinal()] = block("superflatcore:" + name);
        }
        boolean missingStage = false;
        for (Block stage : stages) {
            missingStage |= stage == null;
        }
        if (substrate == null || base == null || missingStage) {
            LOGGER.error("Skipping Create corundum spouting for {} because one or more blocks are missing",
                    definition.element());
            return null;
        }
        return new Element(substrate, base, stages);
    }

    private static Block block(String id) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
    }

    private static String blockId(Block block) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id == null ? "unknown" : id.toString();
    }

    private static String blockId(BlockState state) {
        return blockId(state.getBlock());
    }

    private record ElementDefinition(String element, String substrateId, String baseId) {}

    private record Element(Block substrate, Block base, Block[] stages) {}

    private static final class CorundumSpoutingBehaviour implements BlockSpoutingBehaviour {
        private final List<Element> elements;
        private final boolean blackstoneSubstrate;

        private CorundumSpoutingBehaviour(List<Element> elements, boolean blackstoneSubstrate) {
            this.elements = List.copyOf(elements);
            this.blackstoneSubstrate = blackstoneSubstrate;
        }

        @Override
        public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack fluid, boolean doFill) {
            if (level.isClientSide) {
                return 0;
            }

            BlockState substrateState = level.getBlockState(pos);
            BlockState baseState = level.getBlockState(pos.below());
            BlockPos clusterPos = pos.above();
            BlockState clusterState = level.getBlockState(clusterPos);
            Element element = findElement(substrateState, baseState, clusterState);
            if (element == null) {
                return 0;
            }
            boolean mana = ManaFluidPolicy.isMana(fluid);
            if (!mana) {
                return 0;
            }
            if (fluid.getAmount() < MANA_PER_INJECTION) {
                return 0;
            }

            int currentStage = -1;
            if (clusterState.isAir()) {
                if (blackstoneSubstrate) {
                    return 0;
                }
            } else {
                currentStage = findStage(element, clusterState);
                if (currentStage < 0 || currentStage >= GROWTH_CHANCES.length
                        || clusterState.getValue(CorundumClusterBlock.FACING) != Direction.UP) {
                    return 0;
                }
            }

            if (doFill) {
                return MANA_PER_INJECTION;
            }

            if (clusterState.isAir()) {
                placeCluster(level, element, clusterPos, 0, false);
            } else {
                float roll = level.random.nextFloat();
                if (roll < GROWTH_CHANCES[currentStage]) {
                    boolean waterlogged = clusterState.getValue(CorundumClusterBlock.WATERLOGGED);
                    placeCluster(level, element, clusterPos, currentStage + 1, waterlogged);
                }
            }

            if (!blackstoneSubstrate && level.random.nextFloat() < 0.22F) {
                level.setBlock(pos, Blocks.BLACKSTONE.defaultBlockState(), Block.UPDATE_ALL);
            }
            return MANA_PER_INJECTION;
        }

        private Element findElement(BlockState substrateState, BlockState baseState, BlockState clusterState) {
            if (!blackstoneSubstrate) {
                return elements.stream()
                        .filter(element -> substrateState.is(element.substrate()) && baseState.is(element.base()))
                        .findFirst()
                        .orElse(null);
            }
            if (!substrateState.is(Blocks.BLACKSTONE) || clusterState.isAir()) {
                return null;
            }
            return elements.stream()
                    .filter(element -> baseState.is(element.base()))
                    .filter(element -> findStage(element, clusterState) >= 0
                            && findStage(element, clusterState) < GROWTH_CHANCES.length)
                    .filter(element -> clusterState.getValue(CorundumClusterBlock.FACING) == Direction.UP)
                    .findFirst()
                    .orElse(null);
        }

        private int findStage(Element element, BlockState state) {
            for (int stage = 0; stage < element.stages().length; stage++) {
                if (state.is(element.stages()[stage])) {
                    return stage;
                }
            }
            return -1;
        }

        private void placeCluster(Level level, Element element, BlockPos pos, int stage, boolean waterlogged) {
            BlockState state = element.stages()[stage].defaultBlockState()
                    .setValue(CorundumClusterBlock.FACING, Direction.UP)
                    .setValue(CorundumClusterBlock.WATERLOGGED, waterlogged);
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }
}
