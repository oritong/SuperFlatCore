package com.oritong.superflatcore.mixin;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemFoodPropertiesAccessor {
    @Mutable
    @Accessor("foodProperties")
    void superflatcore$setFoodProperties(@Nullable FoodProperties foodProperties);
}
