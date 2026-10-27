package com.oritong.superflatcore.machine;

import com.google.gson.JsonParser;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.oritong.superflatcore.recipe.ToolAssemblyRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class ToolWeldingAssemblerMachine extends SimpleTieredMachine {
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ToolWeldingAssemblerMachine.class, SimpleTieredMachine.MANAGED_FIELD_HOLDER);

    public ToolWeldingAssemblerMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        if (!super.beforeWorking(recipe)) {
            return false;
        }
        String transfer = recipe.data.getString(ToolAssemblyRecipes.CHARGE_INGREDIENT);
        String upgrade = recipe.data.getString(ToolAssemblyRecipes.UPGRADE_INGREDIENT);
        if (transfer.isEmpty() && upgrade.isEmpty()) {
            return true;
        }
        Ingredient input = Ingredient.fromJson(JsonParser.parseString(upgrade.isEmpty() ? transfer : upgrade));
        ItemStack source = ItemStack.EMPTY;
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            ItemStack stack = importItems.getStackInSlot(slot);
            if (input.test(stack)) {
                source = stack;
                break;
            }
        }
        if (source.isEmpty()) {
            return false;
        }
        List<Content> outputs = recipe.outputs.get(ItemRecipeCapability.CAP);
        if (outputs == null || outputs.size() != 1) {
            return false;
        }
        Content content = outputs.get(0);
        ItemStack[] stacks = ItemRecipeCapability.CAP.of(content.content).getItems();
        if (stacks.length != 1) {
            return false;
        }
        ItemStack result = stacks[0].copy();
        if (!upgrade.isEmpty()) {
            // Match vanilla smithing: keep damage, names, enchantments and the tool's NBT.
            result.setTag(source.hasTag() ? source.getTag().copy() : null);
        } else {
            IElectricItem electric = GTCapabilityHelper.getElectricItem(source);
            if (electric == null) {
                return false;
            }
            result.getOrCreateTag().putLong("MaxCharge", electric.getMaxCharge());
            result.getOrCreateTag().putLong("Charge", electric.getCharge());
        }
        recipe.outputs.put(ItemRecipeCapability.CAP, List.of(new Content(SizedIngredient.create(result),
                content.chance, content.maxChance, content.tierChanceBoost)));
        return true;
    }
}
