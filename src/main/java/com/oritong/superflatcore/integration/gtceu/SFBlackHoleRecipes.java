package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFBlocks;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/** GregTech recipes that recover the black-hole block from Mekanism's cardboard box. */
public final class SFBlackHoleRecipes {
    private static final String CARDBOARD_BLOCK_ID = "mekData.data.blockState.Name";
    private static final String BLACK_HOLE_ID = "superflatcore:black_hole";
    private static final int RECIPE_DURATION_TICKS = 32_500 * 20;

    private SFBlackHoleRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        ItemStack displayedCardboardBox = cardboardBoxContainingBlackHole();
        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder(SuperFlatCore.id("extractor/cardboard_box_black_hole"))
                // Keep the predicate as the matcher while putting the stored block state on the
                // representative stack so JEI renders a cardboard box containing the black hole.
                .inputItemNbtPredicate(displayedCardboardBox,
                        NBTPredicates.eqString(CARDBOARD_BLOCK_ID, BLACK_HOLE_ID))
                .outputItems(SFBlocks.BLACK_HOLE.asStack())
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(RECIPE_DURATION_TICKS)
                .EUt(12)
                .save(provider);
    }

    private static ItemStack cardboardBoxContainingBlackHole() {
        ItemStack stack = new ItemStack(SFMineralProcessingRecipes.item("mekanism:cardboard_box"));
        CompoundTag mekData = stack.getOrCreateTagElement("mekData");
        CompoundTag data = new CompoundTag();
        CompoundTag blockState = new CompoundTag();
        blockState.putString("Name", BLACK_HOLE_ID);
        data.put("blockState", blockState);
        mekData.put("data", data);
        return stack;
    }
}
