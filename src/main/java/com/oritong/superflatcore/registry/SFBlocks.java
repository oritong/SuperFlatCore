package com.oritong.superflatcore.registry;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.block.RadioactiveFluidTankBlock;
import com.oritong.superflatcore.block.CorundumClusterBlock;
import com.oritong.superflatcore.block.WindGeneratorCollisionBlock;
import com.oritong.superflatcore.blackhole.BlackHoleBlock;
import com.simibubi.create.content.fluids.tank.FluidTankGenerator;
import com.oritong.superflatcore.item.RadioactiveFluidTankItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.oritong.superflatcore.client.RadioactiveFluidTankModel;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.ArrayList;
import java.util.List;

/** Blocks used by SuperFlatCore multiblock structures. */
public final class SFBlocks {
    private static final TagKey<Block> MINEABLE_WITH_WRENCH = TagKey.create(Registries.BLOCK,
            new ResourceLocation("forge", "mineable/wrench"));
    public static final TagKey<Block> BLACK_HOLE_BLACKLIST = TagKey.create(Registries.BLOCK,
            SuperFlatCore.id("blackhole_blacklist"));

    public static final BlockEntry<BlackHoleBlock> BLACK_HOLE = SFRegistrate.REGISTRATE
            .block("black_hole", BlackHoleBlock::new)
            .initialProperties(() -> Blocks.END_PORTAL)
            .properties(properties -> properties.noOcclusion().noLootTable().lightLevel(state -> 12)
                    .forceSolidOn())
            .blockstate((context, provider) -> provider.simpleBlock(context.getEntry(),
                    provider.models().getExistingFile(SuperFlatCore.id("block/black_hole_block"))))
            .tag(BLACK_HOLE_BLACKLIST)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .defaultLoot()
            .item(BlockItem::new)
            .model((context, provider) -> provider.withExistingParent(context.getName(),
                    SuperFlatCore.id("block/black_hole_block")))
            .build()
            .register();

    public static final BlockEntry<WindGeneratorCollisionBlock> WIND_GENERATOR_COLLISION = SFRegistrate.REGISTRATE
            .block("wind_generator_collision", WindGeneratorCollisionBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(properties -> properties.noOcclusion().noLootTable()
                    .isViewBlocking((state, level, pos) -> false))
            .register();

    public static final BlockEntry<Block> SUPERCRITICAL_TURBINE_CASING = SFRegistrate.REGISTRATE
            .block("supercritical_turbine_casing", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(properties -> properties.isValidSpawn((state, level, pos, entityType) -> false))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((context, provider) -> provider.simpleBlock(context.getEntry(),
                    provider.models().cubeAll(context.getName(),
                            SuperFlatCore.id("block/supercritical_turbine_casing"))))
            .tag(MINEABLE_WITH_WRENCH, BlockTags.MINEABLE_WITH_PICKAXE)
            .defaultLoot()
            .item(BlockItem::new)
            .build()
            .register();

    public static final BlockEntry<RadioactiveFluidTankBlock> RADIOACTIVE_FLUID_TANK = SFRegistrate.REGISTRATE
            .block("radioactive_fluid_tank", RadioactiveFluidTankBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(properties -> properties.noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> true))
            .transform(pickaxeOnly())
            .blockstate(new FluidTankGenerator()::generate)
            .onRegister(CreateRegistrate.blockModel(() -> RadioactiveFluidTankModel::standard))
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(MINEABLE_WITH_WRENCH, BlockTags.MINEABLE_WITH_PICKAXE)
            .defaultLoot()
            .item(RadioactiveFluidTankItem::new)
            .model(AssetLookup.customBlockItemModel("_", "block_single_window"))
            .build()
            .register();

    public static final List<BlockEntry<CorundumClusterBlock>> ELEMENTAL_CORUNDUM_CLUSTERS =
            registerElementalCorundumClusters();

    private static List<BlockEntry<CorundumClusterBlock>> registerElementalCorundumClusters() {
        List<BlockEntry<CorundumClusterBlock>> clusters = new ArrayList<>();
        registerElementalClusters(clusters, "earth", "brown");
        registerElementalClusters(clusters, "fire", "red");
        registerElementalClusters(clusters, "metal", "yellow");
        registerElementalClusters(clusters, "wood", "green");
        registerElementalClusters(clusters, "water", "blue");
        return List.copyOf(clusters);
    }

    private static void registerElementalClusters(List<BlockEntry<CorundumClusterBlock>> clusters,
                                                   String element, String color) {
        for (CorundumClusterBlock.Size size : CorundumClusterBlock.Size.values()) {
            String name = size == CorundumClusterBlock.Size.CLUSTER
                    ? element + "_corundum_cluster"
                    : size.name().toLowerCase() + "_" + element + "_corundum_cluster";
            String textureName = color + "_corundum_cluster_" + size.name().toLowerCase();
            clusters.add(SFRegistrate.REGISTRATE.block(name,
                            properties -> new CorundumClusterBlock(properties, size,
                                    size == CorundumClusterBlock.Size.CLUSTER
                                            ? new ResourceLocation("gtceu", element + "_corundum_gem")
                                            : null))
                    .initialProperties(() -> Blocks.AMETHYST_CLUSTER)
                    .properties(properties -> properties.noLootTable().noOcclusion()
                            .lightLevel(state -> 11))
                    .addLayer(() -> RenderType::cutout)
                    .blockstate((context, provider) -> {
                        var model = provider.models().cross(context.getName(), SuperFlatCore.id("block/" + textureName));
                        provider.getVariantBuilder(context.getEntry()).forAllStates(state -> {
                            Direction facing = state.getValue(BlockStateProperties.FACING);
                            int x = facing == Direction.DOWN ? 180 : facing == Direction.UP ? 0 : 90;
                            int y = switch (facing) {
                                case EAST -> 90;
                                case SOUTH -> 180;
                                case WEST -> 270;
                                default -> 0;
                            };
                            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
                        });
                    })
                    .item(BlockItem::new)
                    .model((context, provider) -> provider.withExistingParent(context.getName(),
                            SuperFlatCore.id("block/" + context.getName())))
                    .build()
                    .register());
        }
    }

    private SFBlocks() {}

    /** Forces static block registration during mod construction. */
    public static void init() {}
}
