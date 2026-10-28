package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.oritong.superflatcore.recipe.ComponentAssemblyRecipeRules;
import com.oritong.superflatcore.recipe.ComponentAssemblyRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SteelComponentAssemblyLineMachine extends WorkableElectricMultiblockMachine {
    public static final int MAX_PARALLELS = 32;
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteelComponentAssemblyLineMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public SteelComponentAssemblyLineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private MufflerPartMachine getMuffler() {
        for (var part : getParts()) {
            if (part instanceof MufflerPartMachine muffler) {
                return muffler;
            }
        }
        return null;
    }

    @Override
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        // Assembler recipes can make MV or higher components at LV EU/t; gate by the outputs themselves.
        Component failure = getMufflerFailure(recipe);
        if (failure != null) {
            RecipeLogic.putFailureReason(this, recipe, failure);
            return null;
        }
        return super.getRealRecipe(recipe);
    }

    public static ModifierFunction parallelModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof SteelComponentAssemblyLineMachine line)) {
            return ModifierFunction.NULL;
        }
        int parallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, MAX_PARALLELS);
        long recipeEUt = recipe.getInputEUt().getTotalEU();
        if (recipeEUt > 0) {
            // The component-tier gate is independent of the combined power drawn by parallel operations.
            long availableEUt = line.getParallelSupplyVoltage();
            parallels = (int) Math.min(parallels, availableEUt / recipeEUt);
        }
        if (parallels <= 0) {
            return ModifierFunction.NULL;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }

    private long getParallelSupplyVoltage() {
        return super.getOverclockVoltage();
    }

    private Component getMufflerFailure() {
        MufflerPartMachine muffler = getMuffler();
        if (muffler == null || !muffler.isFrontFaceFree()) {
            return Component.translatable("gtceu.multiblock.universal.muffler_obstructed");
        }
        var inventory = muffler.getInventory();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || stack.getCount() < Math.min(stack.getMaxStackSize(), inventory.getSlotLimit(slot))) {
                return null;
            }
        }
        return Component.translatable("superflatcore.multiblock.muffler_full");
    }

    private Component getMufflerFailure(GTRecipe recipe) {
        Component failure = getMufflerFailure();
        if (failure != null || recipe == null) {
            return failure;
        }
        MufflerPartMachine muffler = getMuffler();
        int requiredTier = ComponentAssemblyRecipes.getRequiredComponentTier(recipe);
        if (!ComponentAssemblyRecipeRules.isMufflerTierSufficient(muffler.getTier(), requiredTier)) {
            return Component.translatable("superflatcore.multiblock.insufficient_component_muffler_tier",
                    GTValues.VNF[requiredTier]);
        }
        return null;
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        Component failure = getMufflerFailure(recipe);
        if (failure != null) {
            // The hatch may change between recipe search and setup; keep the reason in the idle failure list.
            RecipeLogic.putFailureReason(this, recipe, failure);
            return false;
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean keepSubscribing() {
        // Muffler recovery slots are not recipe inputs and do not notify the controller when emptied.
        return isFormed();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        // Ordinary recipe matching deliberately combines all buses without assembly-line ordering.
        return new RecipeLogic(this) {
            @Override
            public void handleRecipeWorking() {
                Component failure = getMufflerFailure(lastRecipe);
                if (failure != null) {
                    // Pause before tick IO: retain the batch and progress, and consume no EU while full.
                    setWaiting(failure);
                    return;
                }
                super.handleRecipeWorking();
            }
        };
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("superflatcore.multiblock.fixed_parallel", MAX_PARALLELS));
        }
    }
}
