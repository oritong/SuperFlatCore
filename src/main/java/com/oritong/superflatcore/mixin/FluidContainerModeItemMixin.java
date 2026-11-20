package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.item.QuantumTankMachineItem;
import com.oritong.superflatcore.integration.mekanism.ContainerBucketMode;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.smeltery.item.TankItem;

/** Reuses Mekanism's scroll handling, network packets and mode overlay. */
@Mixin(value = {TankItem.class, DrumMachineItem.class, QuantumTankMachineItem.class, ComponentItem.class}, remap = false)
public abstract class FluidContainerModeItemMixin implements IModeItem {
    @Override
    public boolean supportsSlotType(ItemStack stack, EquipmentSlot slot) {
        return ContainerBucketMode.supportsSlot(stack, slot);
    }

    @Override
    public void changeMode(Player player, ItemStack stack, int shift, DisplayChange display) {
        ContainerBucketMode.changeMode(player, stack, shift, display);
    }

    @Override
    public Component getScrollTextComponent(ItemStack stack) {
        return ContainerBucketMode.getScrollText(stack);
    }
}
