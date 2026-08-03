package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.botania.DominionWandBotaniaIntegration;
import com.oritong.superflatcore.registry.SFItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 100)
public abstract class ItemStackMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void superflatcore$appendDominionWandBotaniaMode(CallbackInfoReturnable<Component> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!DominionWandBotaniaIntegration.isDominionWand(self)) {
            return;
        }

        cir.setReturnValue(DominionWandBotaniaIntegration.getNameWithBotaniaMode(self, cir.getReturnValue()));
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void superflatcore$renameDontTouchMeTooltipModName(Player player, TooltipFlag flag,
                                                               CallbackInfoReturnable<List<Component>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!self.is(SFItems.DONT_TOUCH_ME.get())) {
            return;
        }

        List<Component> tooltip = cir.getReturnValue();
        if (tooltip == null || tooltip.isEmpty()) {
            return;
        }

        Component replacement = Component.literal("\u522b\u78b0\u6211")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
        for (int i = tooltip.size() - 1; i >= 1; i--) {
            String line = tooltip.get(i).getString();
            if ("Dont Touch Me".equalsIgnoreCase(line)
                    || "Don't Touch Me".equalsIgnoreCase(line)
                    || "dont_touch_me".equalsIgnoreCase(line)
                    || "SuperFlatCore".equalsIgnoreCase(line)
                    || "Superflatcore".equalsIgnoreCase(line)) {
                tooltip.set(i, replacement);
                return;
            }
        }

        tooltip.add(replacement);
    }
}
