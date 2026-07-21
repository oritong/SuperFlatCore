package com.oritong.superflatcore.machine;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultipleRecipesLogic extends RecipeLogic {
    private static final int MAX_SELECTED_RECIPES = 64;
    private static final int MAX_SCANNED_RECIPES = 1024;
    private static final int MIN_SYNTHETIC_DURATION = 20;
    private static final int MAX_SYNTHETIC_DURATION = 20 * 500;
    private static final ResourceLocation SYNTHETIC_RECIPE_ID = SuperFlatCore.id("multiple_recipe_batch");

    public MultipleRecipesLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        lastRecipe = null;
        lastOriginRecipe = null;
        failureReasonMap.clear();
        chanceCaches.clear();

        if (!machine.hasCapabilityProxies()) {
            setIdle();
            recipeDirty = false;
            return;
        }

        Iterator<GTRecipe> iterator = searchRecipe();
        if (iterator == null) {
            setIdle();
            recipeDirty = false;
            return;
        }

        BatchAccumulator accumulator = new BatchAccumulator();
        List<Selection> selections = new ArrayList<>();
        int scanned = 0;

        while (iterator.hasNext()
                && scanned++ < MAX_SCANNED_RECIPES
                && selections.size() < MAX_SELECTED_RECIPES) {
            GTRecipe origin = iterator.next();
            Selection selection = createSelection(origin);
            if (selection == null || !accumulator.canAcceptEnergy(selection)) {
                continue;
            }

            ActionResult individualResult = RecipeHelper.matchRecipe(machine, selection.modified());
            if (!individualResult.isSuccess()) {
                putFailureReason(this, origin, individualResult.reason());
                addLastFailedMatch(origin);
                continue;
            }

            BatchAccumulator preview = accumulator.copy();
            preview.accept(selection);
            // Pattern buffer slots are distinct handlers, so only outputs may be checked as one merged recipe.
            GTRecipe previewRecipe = preview.createOutputPreviewRecipe();
            ActionResult outputResult = RecipeHelper.matchRecipe(machine, previewRecipe);
            if (!outputResult.isSuccess()) {
                putFailureReason(this, origin, outputResult.reason());
                addLastFailedMatch(origin);
                continue;
            }

            accumulator = preview;
            selections.add(selection);
            if (accumulator.energyDuration() > MAX_SYNTHETIC_DURATION) {
                break;
            }
        }

        if (selections.isEmpty()) {
            setIdle();
            recipeDirty = false;
            return;
        }

        GTRecipe previewRecipe = accumulator.createSyntheticRecipe(true);
        ActionResult tickResult = RecipeHelper.matchTickRecipe(machine, previewRecipe);
        if (!tickResult.isSuccess()) {
            for (Selection selection : selections) {
                addLastFailedMatch(selection.origin());
                putFailureReason(this, selection.origin(), tickResult.reason());
            }
            setIdle();
            recipeDirty = false;
            return;
        }

        if (!machine.beforeWorking(previewRecipe)) {
            for (Selection selection : selections) {
                addLastFailedMatch(selection.origin());
            }
            setIdle();
            recipeDirty = false;
            return;
        }

        BatchAccumulator consumed = new BatchAccumulator();
        for (Selection selection : selections) {
            ActionResult result = handleRecipeIO(selection.modified(), IO.IN);
            if (result.isSuccess()) {
                consumed.accept(selection);
            } else {
                addLastFailedMatch(selection.origin());
                putFailureReason(this, selection.origin(), result.reason());
            }
        }

        if (consumed.selectedRecipes == 0) {
            setIdle();
            recipeDirty = false;
            return;
        }

        setupSyntheticRecipe(consumed.createSyntheticRecipe(false));
        recipeDirty = false;
    }

    // The vanilla failed-match retry path would start one normal recipe and bypass the batch.
    @Override
    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        return false;
    }

    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            runAttempt = 0;
            runDelay = 0;
            consecutiveRecipes++;
            handleRecipeIO(lastRecipe, IO.OUT);
        }

        lastRecipe = null;
        lastOriginRecipe = null;

        if (suspendAfterFinish) {
            setStatus(Status.SUSPEND);
            progress = 0;
            duration = 0;
            isActive = false;
            return;
        }

        setIdle();
        findAndHandleRecipe();
    }

    private Selection createSelection(GTRecipe origin) {
        if (origin == null) {
            return null;
        }

        GTRecipe modified = machine.fullModifyRecipe(origin.copy());
        if (modified == null || hasUnsupportedTickIO(modified)) {
            return null;
        }

        ActionResult conditionResult = RecipeHelper.checkConditions(modified, this);
        if (!conditionResult.isSuccess()) {
            putFailureReason(this, origin, conditionResult.reason());
            return null;
        }

        long inputEnergy = recipeEnergy(modified.getInputEUt(), modified.duration);
        long outputEnergy = recipeEnergy(modified.getOutputEUt(), modified.duration);
        if (inputEnergy > 0 && outputEnergy > 0) {
            return null;
        }

        return new Selection(origin, modified, inputEnergy, outputEnergy,
                Math.max(1, modified.getTotalRuns()));
    }

    private void setupSyntheticRecipe(GTRecipe synthetic) {
        failureReasonMap.clear();
        lastRecipe = synthetic;
        lastOriginRecipe = null;
        setStatus(Status.WORKING);
        progress = 0;
        duration = synthetic.duration;
        isActive = true;
    }

    private void setIdle() {
        setStatus(Status.IDLE);
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastRecipe = null;
        lastOriginRecipe = null;
    }

    private void addLastFailedMatch(GTRecipe recipe) {
        if (recipe == null) {
            return;
        }
        if (lastFailedMatches == null) {
            lastFailedMatches = new ArrayList<>();
        }
        lastFailedMatches.add(recipe);
    }

    private long maxEnergyPerTick() {
        long voltage;
        if (machine instanceof IOverclockMachine overclockMachine) {
            voltage = overclockMachine.getOverclockVoltage();
        } else if (machine instanceof ITieredMachine tieredMachine) {
            voltage = tieredMachine.getMaxVoltage();
        } else {
            voltage = machine.getDisplayRecipeVoltage();
        }
        return voltage > 0 ? voltage : Long.MAX_VALUE;
    }

    private static boolean hasUnsupportedTickIO(GTRecipe recipe) {
        return hasUnsupportedTickCapabilities(recipe.tickInputs)
                || hasUnsupportedTickCapabilities(recipe.tickOutputs);
    }

    private static boolean hasUnsupportedTickCapabilities(
            Map<RecipeCapability<?>, List<Content>> contents) {
        for (RecipeCapability<?> capability : contents.keySet()) {
            if (capability != EURecipeCapability.CAP) {
                return true;
            }
        }
        return false;
    }

    private static long recipeEnergy(EnergyStack energyStack, int duration) {
        if (energyStack == null || energyStack.isEmpty() || duration <= 0) {
            return 0L;
        }
        return saturatedMultiply(energyStack.getTotalEU(), duration);
    }

    private static long saturatedMultiply(long left, long right) {
        if (left <= 0 || right <= 0) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static long saturatedAdd(long left, long right) {
        if (right <= 0) {
            return left;
        }
        if (left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static int saturatedAddInt(int left, int right) {
        long sum = (long) left + right;
        return sum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }

    private static long ceilDiv(long value, long divisor) {
        if (value <= 0) {
            return 0L;
        }
        if (divisor <= 0) {
            return Long.MAX_VALUE;
        }
        return 1L + (value - 1L) / divisor;
    }

    private static void addContents(Map<RecipeCapability<?>, List<Content>> target,
                                    Map<RecipeCapability<?>, List<Content>> source) {
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : source.entrySet()) {
            RecipeCapability<?> capability = entry.getKey();
            List<Content> targetContents = target.computeIfAbsent(capability, ignored -> new ArrayList<>());
            for (Content content : entry.getValue()) {
                targetContents.add(content.copy(capability));
            }
        }
    }

    private static Map<RecipeCapability<?>, List<Content>> copyContents(
            Map<RecipeCapability<?>, List<Content>> source) {
        Map<RecipeCapability<?>, List<Content>> copy = new HashMap<>();
        addContents(copy, source);
        return copy;
    }

    private record Selection(GTRecipe origin, GTRecipe modified, long inputEnergy,
                             long outputEnergy, int totalRuns) {
        EnergyMode energyMode() {
            if (inputEnergy > 0) {
                return EnergyMode.INPUT;
            }
            if (outputEnergy > 0) {
                return EnergyMode.OUTPUT;
            }
            return EnergyMode.NONE;
        }
    }

    private enum EnergyMode {
        NONE,
        INPUT,
        OUTPUT
    }

    private final class BatchAccumulator {
        private final Map<RecipeCapability<?>, List<Content>> inputs;
        private final Map<RecipeCapability<?>, List<Content>> outputs;
        private long inputEnergy;
        private long outputEnergy;
        private int selectedRecipes;
        private int totalRuns;
        private int maxDuration;
        private EnergyMode energyMode;
        private GTRecipe displayRecipe;

        private BatchAccumulator() {
            this(new HashMap<>(), new HashMap<>(), 0L, 0L, 0, 0, 0, EnergyMode.NONE, null);
        }

        private BatchAccumulator(Map<RecipeCapability<?>, List<Content>> inputs,
                                 Map<RecipeCapability<?>, List<Content>> outputs,
                                 long inputEnergy,
                                 long outputEnergy,
                                 int selectedRecipes,
                                 int totalRuns,
                                 int maxDuration,
                                 EnergyMode energyMode,
                                 GTRecipe displayRecipe) {
            this.inputs = inputs;
            this.outputs = outputs;
            this.inputEnergy = inputEnergy;
            this.outputEnergy = outputEnergy;
            this.selectedRecipes = selectedRecipes;
            this.totalRuns = totalRuns;
            this.maxDuration = maxDuration;
            this.energyMode = energyMode;
            this.displayRecipe = displayRecipe;
        }

        private BatchAccumulator copy() {
            return new BatchAccumulator(copyContents(inputs), copyContents(outputs), inputEnergy, outputEnergy,
                    selectedRecipes, totalRuns, maxDuration, energyMode, displayRecipe);
        }

        private boolean canAcceptEnergy(Selection selection) {
            EnergyMode selectionMode = selection.energyMode();
            return energyMode == EnergyMode.NONE
                    || selectionMode == EnergyMode.NONE
                    || energyMode == selectionMode;
        }

        private void accept(Selection selection) {
            addContents(inputs, selection.modified().inputs);
            addContents(outputs, selection.modified().outputs);
            inputEnergy = saturatedAdd(inputEnergy, selection.inputEnergy());
            outputEnergy = saturatedAdd(outputEnergy, selection.outputEnergy());
            selectedRecipes++;
            totalRuns = saturatedAddInt(totalRuns, selection.totalRuns());
            maxDuration = Math.max(maxDuration, selection.modified().duration);
            if (displayRecipe == null) {
                displayRecipe = selection.modified();
            }
            if (energyMode == EnergyMode.NONE) {
                energyMode = selection.energyMode();
            }
        }

        private GTRecipe createOutputPreviewRecipe() {
            int recipeDuration = syntheticDuration();
            Map<RecipeCapability<?>, List<Content>> tickInputs = new HashMap<>();
            Map<RecipeCapability<?>, List<Content>> tickOutputs = new HashMap<>();

            if (energyMode == EnergyMode.INPUT && inputEnergy > 0) {
                EURecipeCapability.putEUContent(tickInputs,
                        new EnergyStack(Math.max(1L, ceilDiv(inputEnergy, recipeDuration))));
            } else if (energyMode == EnergyMode.OUTPUT && outputEnergy > 0) {
                EURecipeCapability.putEUContent(tickOutputs,
                        new EnergyStack(Math.max(1L, ceilDiv(outputEnergy, recipeDuration))));
            }

            GTRecipe recipe = new GTRecipe(machine.getRecipeType(), SYNTHETIC_RECIPE_ID,
                    new HashMap<>(),
                    copyContents(outputs),
                    tickInputs,
                    tickOutputs,
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    new CompoundTag(),
                    recipeDuration,
                    displayRecipe == null ? null : displayRecipe.recipeCategory,
                    displayRecipe == null ? 0 : displayRecipe.groupColor);
            recipe.parallels = Math.max(1, totalRuns);
            recipe.subtickParallels = 1;
            recipe.batchParallels = 1;
            return recipe;
        }

        private long energyDuration() {
            long totalEnergy = energyMode == EnergyMode.OUTPUT ? outputEnergy : inputEnergy;
            if (totalEnergy <= 0) {
                return 0L;
            }
            return ceilDiv(totalEnergy, maxEnergyPerTick());
        }

        private GTRecipe createSyntheticRecipe(boolean includeInputs) {
            int recipeDuration = syntheticDuration();
            Map<RecipeCapability<?>, List<Content>> tickInputs = new HashMap<>();
            Map<RecipeCapability<?>, List<Content>> tickOutputs = new HashMap<>();

            if (energyMode == EnergyMode.INPUT && inputEnergy > 0) {
                EURecipeCapability.putEUContent(tickInputs,
                        new EnergyStack(Math.max(1L, ceilDiv(inputEnergy, recipeDuration))));
            } else if (energyMode == EnergyMode.OUTPUT && outputEnergy > 0) {
                EURecipeCapability.putEUContent(tickOutputs,
                        new EnergyStack(Math.max(1L, ceilDiv(outputEnergy, recipeDuration))));
            }

            GTRecipe recipe = new GTRecipe(machine.getRecipeType(), SYNTHETIC_RECIPE_ID,
                    includeInputs ? copyContents(inputs) : new HashMap<>(),
                    copyContents(outputs),
                    tickInputs,
                    tickOutputs,
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    new HashMap<RecipeCapability<?>, ChanceLogic>(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    new CompoundTag(),
                    recipeDuration,
                    displayRecipe == null ? null : displayRecipe.recipeCategory,
                    displayRecipe == null ? 0 : displayRecipe.groupColor);
            recipe.parallels = Math.max(1, totalRuns);
            recipe.subtickParallels = 1;
            recipe.batchParallels = 1;
            return recipe;
        }

        private int syntheticDuration() {
            long totalEnergy = energyMode == EnergyMode.OUTPUT ? outputEnergy : inputEnergy;
            if (totalEnergy <= 0) {
                return Math.max(MIN_SYNTHETIC_DURATION, maxDuration);
            }
            long ticks = Math.max(MIN_SYNTHETIC_DURATION, ceilDiv(totalEnergy, maxEnergyPerTick()));
            return ticks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ticks;
        }
    }
}
