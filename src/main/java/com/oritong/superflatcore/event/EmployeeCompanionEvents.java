package com.oritong.superflatcore.event;

import cn.zbx1425.minopp.entity.EntityAutoPlayer;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.EmployeeCompanionItem;
import com.oritong.superflatcore.registry.SFItems;

import net.minecraft.world.InteractionResult;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID)
public final class EmployeeCompanionEvents {
    private EmployeeCompanionEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof EntityAutoPlayer autoPlayer)) {
            return;
        }
        if (!event.getItemStack().is(SFItems.EMPLOYEE_COMPANION.get())) {
            return;
        }

        if (!event.getEntity().level().isClientSide) {
            EmployeeCompanionItem.handleAutoPlayerInteraction(event.getEntity(), autoPlayer);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
