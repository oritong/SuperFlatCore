package com.oritong.superflatcore.client.widget;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;

/** A fixed ability preview slot that opens a dedicated EMI list of valid parts. */
public final class AbilityCandidateSlotWidget extends SlotWidget {
    private final List<ItemStack> candidates;
    private final List<String> abilities;

    public AbilityCandidateSlotWidget(List<ItemStack> candidates, List<String> abilities, int x, int y) {
        super(new ItemStackHandler(1), 0, x, y, false, false);
        this.candidates = candidates.stream().map(ItemStack::copy).toList();
        this.abilities = List.copyOf(abilities);
        setItem(this.candidates.get(0).copy(), false);
        setCanTakeItems(false);
        setCanPutItems(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY) && !candidates.isEmpty()) {
            EmiApi.displayRecipe(new AvailableAbilityPartsRecipe(candidates, abilities));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** A short, paginated list page rendered by EMI's ingredient-browser recipe widget. */
    private static final class AvailableAbilityPartsRecipe extends EmiIngredientRecipe {
        private static final ResourceLocation CATEGORY_ID =
                new ResourceLocation("superflatcore", "available_ability_parts");
        private static final EmiRecipeCategory CATEGORY =
                new EmiRecipeCategory(CATEGORY_ID, EmiStack.of(Items.IRON_BARS));

        private final List<EmiStack> stacks;
        private final EmiIngredient ingredient;
        private final ResourceLocation id;

        private AvailableAbilityPartsRecipe(List<ItemStack> candidates, List<String> abilities) {
            this.stacks = candidates.stream()
                    .map(EmiStack::of)
                    .distinct()
                    .toList();
            this.ingredient = EmiIngredient.of(stacks.stream().map(stack -> (EmiIngredient) stack).toList());
            String identity = String.join("_", abilities);
            this.id = new ResourceLocation("superflatcore",
                    "available_ability_parts/" + Integer.toUnsignedString(Objects.hash(identity, stacks), 16));
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        protected EmiIngredient getIngredient() {
            return ingredient;
        }

        @Override
        protected List<EmiStack> getStacks() {
            return stacks;
        }

        @Override
        protected EmiRecipe getRecipeContext(EmiStack stack, int offset) {
            return null;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            super.addWidgets(widgets);
            widgets.addText(net.minecraft.network.chat.Component.translatable(
                    "superflatcore.multiblock.ability.available"), 2, 14, 0x404040, false);
        }
    }
}
