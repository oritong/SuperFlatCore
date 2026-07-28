package com.oritong.superflatcore.mixin.client.sfm;

import ca.teamdman.sfm.client.jei.FallingAnvilFormRecipe;
import ca.teamdman.sfm.client.jei.FallingAnvilJEICategory;
import ca.teamdman.sfm.common.item.FormItem;
import ca.teamdman.sfm.common.util.SFMComponentUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(value = FallingAnvilJEICategory.class, remap = false)
public abstract class FallingAnvilJEICategoryMixin {
    @Unique
    private static final ResourceLocation SUPERFLATCORE$GTCEU_STEEL_BLOCK = new ResourceLocation("gtceu", "steel_block");

    @Inject(method = "setRecipeForFallingAnvilFormRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private static void superflatcore$showSteelBlockForForm(
            IRecipeLayoutBuilder builder,
            FallingAnvilFormRecipe formRecipe,
            List<ItemStack> anvil,
            CallbackInfo ci
    ) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 0, 0).addItemStacks(anvil);
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 18).addIngredients(formRecipe.PARENT.form());
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 36).addItemStack(superflatcore$steelBlockStack());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 50, 18)
                .addItemStacks(Arrays.stream(formRecipe.PARENT.form().getItems())
                        .map(FormItem::createFormFromReference)
                        .toList());
        ci.cancel();
    }

    @Unique
    private static ItemStack superflatcore$steelBlockStack() {
        Block steelBlock = ForgeRegistries.BLOCKS.getValue(SUPERFLATCORE$GTCEU_STEEL_BLOCK);
        ItemStack stack = new ItemStack(steelBlock == null ? Blocks.IRON_BLOCK : steelBlock);
        SFMComponentUtils.appendLore(
                stack,
                FallingAnvilJEICategory.Localization.FALLING_ANVIL_JEI_CONSUMED.getComponent()
        );
        return stack;
    }
}
