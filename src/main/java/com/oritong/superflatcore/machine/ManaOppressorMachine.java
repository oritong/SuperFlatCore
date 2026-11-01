package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.recipe.SFRecipeTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManaOppressorMachine extends WorkableMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine {
    public static final String MANA_DATA_KEY = "superflatcore:mana";
    private static final ResourceTexture MANA_BAR_TEXTURE = new ResourceTexture(
            new ResourceLocation("botania", "textures/gui/mana_hud.png"));
    private static final ResourceTexture MANA_BAR_FRAME = MANA_BAR_TEXTURE.getSubTexture(
            0, 0, 102.0 / 256.0, 5.0 / 256.0);
    private static final ResourceTexture MANA_BAR_EMPTY = MANA_BAR_TEXTURE.getSubTexture(
            0, 5.0 / 256.0, 100.0 / 256.0, 3.0 / 256.0).setColor(0xBF26333D);
    private static final ResourceTexture MANA_BAR_FILL = MANA_BAR_TEXTURE.getSubTexture(
            0, 5.0 / 256.0, 100.0 / 256.0, 3.0 / 256.0).setColor(0xBF55B9FF);
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ManaOppressorMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    public ManaOppressorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof ManaOppressorMachine manaMachine)) {
            return ModifierFunction.NULL;
        }

        if (manaMachine.isOppressionMode()) {
            ModifierFunction parallelModifier = GTRecipeModifiers.PARALLEL_HATCH.getModifier(machine, recipe);
            if (parallelModifier == ModifierFunction.NULL) {
                return ModifierFunction.NULL;
            }
            return modified -> {
                GTRecipe result = parallelModifier.apply(modified);
                if (result == null) {
                    return null;
                }
                long mana = Math.max(0L, recipe.data.getLong(MANA_DATA_KEY));
                long runs = Math.max(1, result.getTotalRuns());
                result.data.putLong(MANA_DATA_KEY, saturatedMultiply(mana, runs));
                return result;
            };
        }

        ManaInfusionRecipe sourceRecipe = manaMachine.findInfusionRecipe(recipe);
        if (sourceRecipe == null || manaMachine.getManaFluid() == null
                || manaMachine.getParallelHatch().isPresent()) {
            return ModifierFunction.NULL;
        }
        return modified -> {
            int parallel = ParallelLogic.getParallelAmountWithoutEU(machine, modified, Integer.MAX_VALUE);
            if (parallel <= 0) {
                return null;
            }
            GTRecipe result = modified.copy(ContentModifier.multiplier(parallel), false);
            result.duration = 40;
            result.data.putLong(MANA_DATA_KEY,
                    saturatedMultiply(sourceRecipe.getManaToConsume(), parallel));
            return result;
        };
    }

    @Override
    public GTRecipeType getRecipeType() {
        return isOppressionMode()
                ? SFRecipeTypes.MANA_OPPRESSION_RECIPES
                : SFRecipeTypes.MANA_INFUSION_RECIPES;
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        if (!super.beforeWorking(recipe)) {
            return false;
        }
        return consumeMana(recipe == null ? 0L : recipe.data.getLong(MANA_DATA_KEY));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 190, 125);
        group.addWidget(new LabelWidget(6, 6, Component.translatable("block.superflatcore.mana_oppressor")));
        group.addWidget(new LabelWidget(6, 24, () -> getModeText().getString()));
        group.addWidget(new LabelWidget(6, 44, this::getManaMeterLabelText));
        group.addWidget(new ImageWidget(6, 60, 102, 5, MANA_BAR_FRAME));
        group.addWidget(new ProgressWidget(this::getManaProgress, 7, 61, 100, 3,
                new ProgressTexture(MANA_BAR_EMPTY, MANA_BAR_FILL)
                        .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT)));
        group.addWidget(new LabelWidget(6, 80, () -> getProgressText().getString()));
        return group;
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 208, this, player)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        textList.add(getModeText());
        textList.add(Component.literal(getManaMeterText()));
        textList.add(getProgressText());
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    @Override
    public List<com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider> getSubTabs() {
        return getParts().stream()
                .filter(com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider.class::isInstance)
                .map(com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider.class::cast)
                .toList();
    }

    @Override
    public void attachTooltips(com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    public boolean isOppressionMode() {
        return getBaseState().is(BotaniaBlocks.manaVoid);
    }

    public static boolean isOppressionMode(MultiblockState state) {
        MetaMachine controller = MetaMachine.getMachine(state.getWorld(), state.controllerPos);
        return controller instanceof ManaOppressorMachine manaMachine && manaMachine.isOppressionMode();
    }

    public BlockState getBaseState() {
        Level level = getLevel();
        if (level == null) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockPos controller = getPos();
        net.minecraft.core.Direction facing = getFrontFacing();
        net.minecraft.core.Direction[] directions = {
                facing,
                facing.getOpposite(),
                facing.getClockWise(),
                facing.getCounterClockWise()
        };
        for (net.minecraft.core.Direction direction : directions) {
            for (int verticalOffset : new int[] { 1, -1 }) {
                BlockState candidate = level.getBlockState(
                        controller.relative(direction, 2).offset(0, verticalOffset, 0));
                if (isValidBaseBlock(candidate, level)) {
                    return candidate;
                }
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    public boolean acceptsBaseState(BlockState state) {
        return isValidBaseBlock(state, getLevel());
    }

    public boolean isInfusionRecipeAllowed(GTRecipe candidate) {
        ManaInfusionRecipe infusionRecipe = findInfusionRecipe(candidate);
        if (infusionRecipe == null) {
            return false;
        }

        BlockState base = getBaseState();
        if (base.is(BotaniaBlocks.manaDiamondBlock)) {
            return infusionRecipe.getRecipeCatalyst() == null;
        }

        if (base.is(BotaniaBlocks.manaVoid) || base.isAir()) {
            return false;
        }

        boolean hasMatchingCatalystRecipe = false;
        for (ItemStack stack : getInputItemStacks()) {
            if (!infusionRecipe.matches(stack)) {
                continue;
            }
            for (ManaInfusionRecipe recipe : getInfusionRecipes()) {
                StateIngredient recipeCatalyst = recipe.getRecipeCatalyst();
                if (recipe.matches(stack) && recipeCatalyst != null && recipeCatalyst.test(base)) {
                    hasMatchingCatalystRecipe = true;
                    break;
                }
            }
            break;
        }

        StateIngredient catalyst = infusionRecipe.getRecipeCatalyst();
        if (catalyst != null) {
            return catalyst.test(base);
        }
        return !hasMatchingCatalystRecipe;
    }

    public static boolean isValidBaseBlock(BlockState state, Level level) {
        if (state.is(BotaniaBlocks.manaVoid) || state.is(BotaniaBlocks.manaDiamondBlock)
                || state.is(BotaniaBlocks.alchemyCatalyst)
                || state.is(BotaniaBlocks.conjurationCatalyst)) {
            return true;
        }
        Block otherstone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("occultism", "otherstone"));
        if (otherstone != null && state.is(otherstone)) {
            return true;
        }
        if (level == null) {
            return false;
        }
        for (ManaInfusionRecipe recipe : BotaniaRecipeTypes.getRecipes(level,
                BotaniaRecipeTypes.MANA_INFUSION_TYPE).values()) {
            StateIngredient catalyst = recipe.getRecipeCatalyst();
            if (catalyst != null && catalyst.test(state)) {
                return true;
            }
        }
        return false;
    }

    public long getStoredMana() {
        Fluid manaFluid = getManaFluid();
        if (manaFluid == null) {
            return 0;
        }
        long total = 0;
        for (ManaFluidHatchPartMachine hatch : getManaHatches()) {
            for (int tank = 0; tank < hatch.tank.getTanks(); tank++) {
                FluidStack fluid = hatch.tank.getFluidInTank(tank);
                if (fluid.getFluid() == manaFluid) {
                    total += fluid.getAmount();
                }
            }
        }
        return total;
    }

    public long getManaCapacity() {
        long total = 0;
        for (ManaFluidHatchPartMachine hatch : getManaHatches()) {
            for (int tank = 0; tank < hatch.tank.getTanks(); tank++) {
                total += hatch.tank.getTankCapacity(tank);
            }
        }
        return total;
    }

    private boolean consumeMana(long required) {
        if (required <= 0) {
            return true;
        }
        Fluid manaFluid = getManaFluid();
        if (manaFluid == null || required > Integer.MAX_VALUE || getStoredMana() < required) {
            return false;
        }

        int remaining = (int) required;
        for (ManaFluidHatchPartMachine hatch : getManaHatches()) {
            for (int tank = 0; tank < hatch.tank.getTanks() && remaining > 0; tank++) {
                FluidStack stored = hatch.tank.getFluidInTank(tank);
                if (stored.getFluid() != manaFluid) {
                    continue;
                }
                FluidStack drained = hatch.tank.drainInternal(new FluidStack(manaFluid, remaining),
                        IFluidHandler.FluidAction.EXECUTE);
                remaining -= drained.getAmount();
            }
        }
        return remaining == 0;
    }

    private ManaInfusionRecipe findInfusionRecipe(GTRecipe recipe) {
        Level level = getLevel();
        if (level == null || recipe == null) {
            return null;
        }
        return level.getRecipeManager().byKey(recipe.id)
                .filter(ManaInfusionRecipe.class::isInstance)
                .map(ManaInfusionRecipe.class::cast)
                .orElse(null);
    }

    private List<ManaInfusionRecipe> getInfusionRecipes() {
        Level level = getLevel();
        return level == null ? List.of() : new ArrayList<>(BotaniaRecipeTypes.getRecipes(level,
                BotaniaRecipeTypes.MANA_INFUSION_TYPE).values());
    }

    public List<GTRecipe> getInfusionGTRecipes() {
        return getInfusionRecipes().stream()
                .map(recipe -> {
                    GTRecipe converted = SFRecipeTypes.MANA_INFUSION_RECIPES.toGTrecipe(recipe.getId(), recipe);
                    converted.data.putLong(MANA_DATA_KEY, recipe.getManaToConsume());
                    return converted;
                })
                .toList();
    }

    private List<ItemStack> getInputItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<IRecipeHandler<?>>> handlers =
                getCapabilitiesFlat().getOrDefault(IO.IN, Map.of());
        List<IRecipeHandler<?>> itemHandlers = handlers.getOrDefault(
                com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP, List.of());
        for (IRecipeHandler<?> handler : itemHandlers) {
            for (Object content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    private List<ManaFluidHatchPartMachine> getManaHatches() {
        return getParts().stream()
                .filter(ManaFluidHatchPartMachine.class::isInstance)
                .map(ManaFluidHatchPartMachine.class::cast)
                .filter(ManaFluidHatchPartMachine::isInputHatch)
                .toList();
    }

    private Fluid getManaFluid() {
        return ForgeRegistries.FLUIDS.getValue(new net.minecraft.resources.ResourceLocation(
                "technicalflowers", "mana"));
    }

    private Component getModeText() {
        return Component.translatable("gui.superflatcore.mana_oppressor.mode",
                Component.translatable(isOppressionMode()
                        ? "gui.superflatcore.mana_oppressor.mode.oppression"
                        : "gui.superflatcore.mana_oppressor.mode.infusion").getString());
    }

    private String getManaMeterText() {
        long current = getStoredMana();
        long capacity = getManaCapacity();
        int percent = capacity <= 0 ? 0 : (int) Math.min(100, current * 100 / capacity);
        return Component.translatable("gui.superflatcore.mana_oppressor.mana").getString()
                + ": " + String.format(Locale.ROOT, "%d/%d mB (%d%%)", current, capacity, percent);
    }

    private String getManaMeterLabelText() {
        return getManaMeterText().replace("%", "%%");
    }

    private double getManaProgress() {
        long capacity = getManaCapacity();
        return capacity <= 0 ? 0.0 : Math.min(1.0, (double) getStoredMana() / capacity);
    }

    private Component getProgressText() {
        RecipeLogic logic = getRecipeLogic();
        if (!isFormed()) {
            return Component.translatable("gtceu.multiblock.invalid_structure").withStyle(ChatFormatting.RED);
        }
        if (logic.isActive()) {
            return Component.translatable("gtceu.multiblock.progress",
                    (int) (logic.getProgressPercent() * 100));
        }
        return Component.translatable("gtceu.multiblock.idling");
    }

    private static long saturatedMultiply(long value, long multiplier) {
        if (value <= 0 || multiplier <= 0) {
            return 0;
        }
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }
}
