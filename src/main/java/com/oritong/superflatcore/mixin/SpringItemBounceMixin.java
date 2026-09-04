package com.oritong.superflatcore.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class SpringItemBounceMixin {
    private static final TagKey<Item> SPRINGS = ItemTags.create(new ResourceLocation("forge", "springs"));
    private static final TagKey<Item> SMALL_SPRINGS = ItemTags.create(new ResourceLocation("forge", "small_springs"));

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;updateEntityAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"))
    private void superflatcore$bounceSpringItems(Block block, BlockGetter level, Entity entity) {
        if (entity instanceof ItemEntity item
                && (item.getItem().is(SPRINGS) || item.getItem().is(SMALL_SPRINGS))) {
            Vec3 velocity = entity.getDeltaMovement();
            if (velocity.y < 0.0D) {
                entity.setDeltaMovement(velocity.x, -velocity.y, velocity.z);
                return;
            }
        }
        block.updateEntityAfterFallOn(level, entity);
    }
}
