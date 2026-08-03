package com.oritong.superflatcore.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.utils.IngredientEquality;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class ProportionalInputLogic {
    private static final String DATA_KEY = "superflatcore:proportional_input";

    private ProportionalInputLogic() {}

    public static GTRecipe applyModifier(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        GTRecipe modified = tagRecipe(recipe);
        if (!inputsAreProportional(holder, modified)) {
            modified.outputs.clear();
            modified.tickOutputs.clear();
        }
        return modified;
    }

    public static GTRecipe tagRecipe(GTRecipe recipe) {
        GTRecipe modified = recipe.copy();
        CompoundTag data = recipe.data == null ? new CompoundTag() : recipe.data.copy();
        data.putBoolean(DATA_KEY, true);
        modified.data = data;
        return modified;
    }

    public static boolean isProportionalInputRecipe(GTRecipe recipe) {
        return recipe != null && recipe.data != null && recipe.data.getBoolean(DATA_KEY);
    }

    public static boolean inputsAreProportional(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        List<Requirement> requirements = collectRequirements(recipe);
        if (requirements.size() < 2) {
            return true;
        }

        long multiplier = -1L;
        for (Requirement requirement : requirements) {
            long available = requirement.countAvailable(holder);
            if (available < requirement.requiredAmount()
                    || available % requirement.requiredAmount() != 0) {
                return false;
            }

            long currentMultiplier = available / requirement.requiredAmount();
            if (multiplier < 0) {
                multiplier = currentMultiplier;
            } else if (multiplier != currentMultiplier) {
                return false;
            }
        }

        return multiplier > 0;
    }

    public static GTRecipe withoutOutputs(GTRecipe recipe) {
        GTRecipe voidedRecipe = recipe.copy();
        voidedRecipe.outputs.clear();
        voidedRecipe.tickOutputs.clear();
        return voidedRecipe;
    }

    private static List<Requirement> collectRequirements(GTRecipe recipe) {
        List<Requirement> requirements = new ArrayList<>();

        for (Content content : recipe.inputs.getOrDefault(ItemRecipeCapability.CAP, List.of())) {
            if (content.chance == 0) {
                continue;
            }

            Ingredient ingredient = ItemRecipeCapability.CAP.of(content.getContent());
            Ingredient inner = unwrapItemIngredient(ingredient);
            if (inner instanceof IntCircuitIngredient) {
                continue;
            }

            int amount = itemAmount(ingredient);
            if (amount > 0) {
                addItemRequirement(requirements, ingredient, amount);
            }
        }

        for (Content content : recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, List.of())) {
            if (content.chance == 0) {
                continue;
            }

            FluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.getContent());
            int amount = ingredient.getAmount();
            if (amount > 0) {
                addFluidRequirement(requirements, ingredient, amount);
            }
        }

        return requirements;
    }

    private static void addItemRequirement(List<Requirement> requirements, Ingredient ingredient, int amount) {
        Ingredient comparable = unwrapItemIngredient(ingredient);
        for (Requirement requirement : requirements) {
            if (requirement instanceof ItemRequirement existing
                    && IngredientEquality.ingredientEquals(existing.comparableIngredient(), comparable)) {
                existing.addRequiredAmount(amount);
                return;
            }
        }
        requirements.add(new ItemRequirement(ingredient, comparable, amount));
    }

    private static void addFluidRequirement(List<Requirement> requirements, FluidIngredient ingredient, int amount) {
        for (Requirement requirement : requirements) {
            if (requirement instanceof FluidRequirement existing
                    && sameFluidIngredient(existing.ingredient(), ingredient)) {
                existing.addRequiredAmount(amount);
                return;
            }
        }
        requirements.add(new FluidRequirement(ingredient, amount));
    }

    private static Ingredient unwrapItemIngredient(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            return unwrapItemIngredient(sizedIngredient.getInner());
        }
        if (ingredient instanceof IntProviderIngredient providerIngredient) {
            return unwrapItemIngredient(providerIngredient.getInner());
        }
        return ingredient;
    }

    private static int itemAmount(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            return sizedIngredient.getAmount();
        }
        if (ingredient instanceof IntProviderIngredient providerIngredient) {
            return Math.max(providerIngredient.getSampledCount(),
                    providerIngredient.getCountProvider().getMaxValue());
        }
        return 1;
    }

    private static boolean sameFluidIngredient(FluidIngredient first, FluidIngredient second) {
        FluidIngredient firstCopy = first.copy();
        FluidIngredient secondCopy = second.copy();
        firstCopy.setAmount(1);
        secondCopy.setAmount(1);
        return firstCopy.equals(secondCopy);
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private abstract static class Requirement {
        private long requiredAmount;

        private Requirement(long requiredAmount) {
            this.requiredAmount = requiredAmount;
        }

        final long requiredAmount() {
            return requiredAmount;
        }

        final void addRequiredAmount(long amount) {
            requiredAmount = saturatedAdd(requiredAmount, amount);
        }

        abstract long countAvailable(IRecipeCapabilityHolder holder);
    }

    private static final class ItemRequirement extends Requirement {
        private final Ingredient ingredient;
        private final Ingredient comparableIngredient;

        private ItemRequirement(Ingredient ingredient, Ingredient comparableIngredient, long requiredAmount) {
            super(requiredAmount);
            this.ingredient = ingredient;
            this.comparableIngredient = comparableIngredient;
        }

        private Ingredient comparableIngredient() {
            return comparableIngredient;
        }

        @Override
        long countAvailable(IRecipeCapabilityHolder holder) {
            long amount = 0L;
            for (var handler : holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty() && ingredient.test(stack)) {
                        amount = saturatedAdd(amount, stack.getCount());
                    }
                }
            }
            return amount;
        }
    }

    private static final class FluidRequirement extends Requirement {
        private final FluidIngredient ingredient;

        private FluidRequirement(FluidIngredient ingredient, long requiredAmount) {
            super(requiredAmount);
            this.ingredient = ingredient;
        }

        private FluidIngredient ingredient() {
            return ingredient;
        }

        @Override
        long countAvailable(IRecipeCapabilityHolder holder) {
            long amount = 0L;
            for (var handler : holder.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof FluidStack stack && !stack.isEmpty() && ingredient.test(stack)) {
                        amount = saturatedAdd(amount, stack.getAmount());
                    }
                }
            }
            return amount;
        }
    }
}
