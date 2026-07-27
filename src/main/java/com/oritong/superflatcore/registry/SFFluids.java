package com.oritong.superflatcore.registry;

import com.oritong.superflatcore.fluid.LatexFluidType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SFFluids {
    public static final String ORITONG_NAMESPACE = "oritong";
    private static final ResourceLocation GTCEU_ITEM_TAB = new ResourceLocation("gtceu", "item");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            ForgeRegistries.Keys.FLUID_TYPES, ORITONG_NAMESPACE);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
            ORITONG_NAMESPACE);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            ORITONG_NAMESPACE);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            ORITONG_NAMESPACE);

    public static final RegistryObject<FluidType> LATEX_TYPE = FLUID_TYPES.register("latex", LatexFluidType::new);
    public static final RegistryObject<FlowingFluid> LATEX = FLUIDS.register("latex",
            () -> new ForgeFlowingFluid.Source(latexProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_LATEX = FLUIDS.register("flowing_latex",
            () -> new ForgeFlowingFluid.Flowing(latexProperties()));
    public static final RegistryObject<LiquidBlock> LATEX_BLOCK = BLOCKS.register("latex",
            () -> new LiquidBlock(LATEX, net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WATER)
                    .noLootTable()));
    public static final RegistryObject<BucketItem> LATEX_BUCKET = ITEMS.register("latex_bucket",
            () -> new BucketItem(LATEX, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private SFFluids() {}

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(SFFluids::addCreativeTabItems);
    }

    private static ForgeFlowingFluid.Properties latexProperties() {
        return new ForgeFlowingFluid.Properties(LATEX_TYPE, LATEX, FLOWING_LATEX)
                .block(LATEX_BLOCK)
                .bucket(LATEX_BUCKET)
                .tickRate(25)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .explosionResistance(100.0f);
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (GTCEU_ITEM_TAB.equals(event.getTabKey().location())) {
            event.accept(LATEX_BUCKET.get());
        }
    }
}
