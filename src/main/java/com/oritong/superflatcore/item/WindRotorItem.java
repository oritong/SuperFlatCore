package com.oritong.superflatcore.item;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.RotorProperty;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.component.IMaterialPartItem;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WindRotorItem extends TagPrefixItem implements IMaterialPartItem {
    public WindRotorItem(Item.Properties properties, TagPrefix tagPrefix,
                         com.gregtechceu.gtceu.api.data.chemical.material.Material material) {
        super(properties, tagPrefix, material);
    }

    @Override
    public com.gregtechceu.gtceu.api.data.chemical.material.Material getPartMaterial(ItemStack itemStack) {
        return material;
    }

    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        RotorProperty rotorProperty = getRotorProperty();
        return rotorProperty == null ? -1 : 800 * (int) Math.pow(rotorProperty.getDurability(), 0.65);
    }

    public int getRotorPower(ItemStack itemStack) {
        RotorProperty rotorProperty = getRotorProperty();
        return rotorProperty == null ? -1 : rotorProperty.getPower();
    }

    public int getRotorDurabilityPercent(ItemStack itemStack) {
        int maxDurability = getPartMaxDurability(itemStack);
        return maxDurability <= 0 ? 0 : 100 - 100 * getPartDamage(itemStack) / maxDurability;
    }

    public void applyRotorDamage(ItemStack itemStack, int damageApplied) {
        int maxDurability = getPartMaxDurability(itemStack);
        if (maxDurability <= 0) {
            return;
        }
        int resultingDamage = getPartDamage(itemStack) + damageApplied;
        if (resultingDamage >= maxDurability) {
            itemStack.shrink(1);
        } else {
            setPartDamage(itemStack, resultingDamage);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getPartMaxDurability(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxDurability = getPartMaxDurability(stack);
        if (maxDurability <= 0) {
            return 0;
        }
        return Math.round(getDurabilityForDisplay(stack) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float durability = Mth.clamp(getDurabilityForDisplay(stack), 0.0F, 1.0F);
        return Mth.hsvToRgb(durability / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        int maxDurability = getPartMaxDurability(stack);
        int damage = getPartDamage(stack);
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.durability",
                maxDurability - damage, maxDurability));
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.primary_material",
                getPartMaterial(stack).getLocalizedName()));
        tooltipComponents.add(Component.translatable("tooltip.superflatcore.wind_rotor.wind_output",
                getRotorPower(stack)));
    }

    private RotorProperty getRotorProperty() {
        return material.getProperty(PropertyKey.ROTOR);
    }
}
