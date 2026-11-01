package com.oritong.superflatcore.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShapelessRecipe.class)
public abstract class OccultismOtherstoneTradeMixin {
    private static final ResourceLocation OTHERSTONE_TRADE =
            new ResourceLocation("occultism", "spirit_trade/stone_to_otherstone");
    private static final ResourceLocation OTHERWORLD_SAPLING_TRADE =
            new ResourceLocation("occultism", "spirit_trade/otherworld_sapling");
    private static final ResourceLocation PURE_DAISY = new ResourceLocation("botania", "pure_daisy");

    @Shadow
    @Final
    @Mutable
    private NonNullList<Ingredient> ingredients;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void superflatcore$requireBlackstone(ResourceLocation id, String group, CraftingBookCategory category,
                                                  ItemStack result, NonNullList<Ingredient> ingredients,
                                                  CallbackInfo ci) {
        if (OTHERSTONE_TRADE.equals(id)) {
            this.ingredients = NonNullList.withSize(1, Ingredient.of(Items.BLACKSTONE));
        } else if (OTHERWORLD_SAPLING_TRADE.equals(id)) {
            this.ingredients = NonNullList.withSize(1,
                    Ingredient.of(ForgeRegistries.ITEMS.getValue(PURE_DAISY)));
        }
    }
}
