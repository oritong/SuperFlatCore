package com.oritong.superflatcore.integration.mekanism;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.oritong.superflatcore.SuperFlatCore;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.StorageUtils;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class AtomicDisassemblerIntegration {
    private static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    // The player's base attack damage adds 1, for 128 total damage at full power.
    private static final double FULL_POWER_DAMAGE_BONUS = 127.0;

    private AtomicDisassemblerIntegration() {}

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getSlotType() != EquipmentSlot.MAINHAND || !stack.is(MekanismItems.ATOMIC_DISASSEMBLER.get())) {
            return;
        }

        for (AttributeModifier modifier : event.getOriginalModifiers().get(Attributes.ATTACK_DAMAGE)) {
            if (BASE_ATTACK_DAMAGE_UUID.equals(modifier.getId())) {
                IEnergyContainer container = StorageUtils.getEnergyContainer(stack, 0);
                FloatingLong energy = container == null ? FloatingLong.ZERO : container.getEnergy();
                double power = energy.divideToLevel(MekanismConfig.gear.disassemblerEnergyUsageWeapon.get());
                double minimumDamage = MekanismConfig.gear.disassemblerMinDamage.get();
                double damage = minimumDamage + (FULL_POWER_DAMAGE_BONUS - minimumDamage) * power;
                event.removeModifier(Attributes.ATTACK_DAMAGE, modifier);
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(modifier.getId(), modifier.getName(),
                        damage, modifier.getOperation()));
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(MekanismItems.ATOMIC_DISASSEMBLER.get()) || !event.getEntity().isShiftKeyDown()
                || event.getUseBlock() == Event.Result.DENY || !ToolHelper.is(stack, GTToolType.WRENCH)) {
            return;
        }

        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof MetaMachineBlock) {
            // Let the machine handle the tagged wrench, including grid selection, rotation and tool sounds.
            event.setUseBlock(Event.Result.ALLOW);
        }
    }
}
