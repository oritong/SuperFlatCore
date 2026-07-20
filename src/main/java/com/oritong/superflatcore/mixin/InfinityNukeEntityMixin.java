package com.oritong.superflatcore.mixin;

import com.buuz135.industrial.entity.InfinityNukeEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InfinityNukeEntity.class, remap = false)
public abstract class InfinityNukeEntityMixin {

    @Unique
    private static final ResourceLocation SUPERFLATCORE$DRAGON_STAR_ID =
            new ResourceLocation("ifeu", "dragon_star");

    @Shadow(remap = false)
    public abstract boolean isExploding();

    @Shadow(remap = false)
    public abstract boolean isArmed();

    @Shadow(remap = false)
    public abstract void setExploding(boolean exploding);

    @Inject(method = "m_6096_", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$replaceInfinityNukeIgniter(Player player, InteractionHand hand,
                                                          CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide || hand != InteractionHand.MAIN_HAND || isExploding() || !isArmed()) {
            return;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return;
        }

        if (held.is(Items.FLINT_AND_STEEL)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        Item dragonStar = ForgeRegistries.ITEMS.getValue(SUPERFLATCORE$DRAGON_STAR_ID);
        if (dragonStar != null && held.is(dragonStar)) {
            setExploding(true);
            held.shrink(1);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
