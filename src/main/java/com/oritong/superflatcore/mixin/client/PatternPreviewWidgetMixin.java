package com.oritong.superflatcore.mixin.client;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.oritong.superflatcore.client.widget.AbilityCandidateSlotWidget;
import com.oritong.superflatcore.mixin.AbilityPredicateMetadata;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Adds right-click panning and fixed ability previews to GTCEu's structure viewer. */
@OnlyIn(Dist.CLIENT)
@Mixin(value = PatternPreviewWidget.class, remap = false)
public abstract class PatternPreviewWidgetMixin extends WidgetGroup {
    @Shadow
    @Final
    private SceneWidget sceneWidget;

    @Shadow
    @Final
    private List<SimplePredicate> predicates;

    @Shadow
    private SlotWidget[] candidates;

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 1 && sceneWidget.isMouseOverElement(mouseX, mouseY)) {
            double viewSize = Math.max(1.0D,
                    Math.min(sceneWidget.getSizeWidth(), sceneWidget.getSizeHeight()));
            double panScale = sceneWidget.getZoom() / viewSize;
            dragX *= panScale;
            dragY *= panScale;

            double pitch = Math.toRadians(sceneWidget.getRotationPitch());
            double yaw = Math.toRadians(sceneWidget.getRotationYaw());
            float moveX = -(float) (dragY * Math.sin(yaw) * Math.cos(pitch) + dragX * Math.sin(pitch));
            float moveY = (float) (dragY * Math.cos(yaw));
            float moveZ = (float) (-dragY * Math.sin(yaw) * Math.sin(pitch) + dragX * Math.cos(pitch));
            Vector3f center = sceneWidget.getCenter();
            sceneWidget.setCenter(center.add(moveX, moveY, moveZ));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /** Keep ordinary block predicates unchanged and replace only GTCEu ability slots. */
    @Inject(method = "onPosSelected", at = @At("TAIL"))
    private void superflatcore$replaceAbilityCandidates(BlockPos pos, Direction facing, CallbackInfo ci) {
        if (predicates == null || predicates.isEmpty() || candidates == null || candidates.length == 0) {
            return;
        }

        List<SlotWidget> replacement = new ArrayList<>(candidates.length);
        int slotIndex = 0;
        for (SimplePredicate predicate : predicates) {
            if (predicate == null || predicate.candidates == null) {
                continue;
            }

            List<ItemStack> predicateCandidates = predicate.getCandidates().stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            if (predicateCandidates.isEmpty() || slotIndex >= candidates.length) {
                continue;
            }

            SlotWidget original = candidates[slotIndex++];
            Set<String> abilityNames = Set.copyOf(AbilityPredicateMetadata.get(predicate));

            // Only predicates created by Predicates.abilities/ability are replaced.
            // Ordinary block predicates (including a casing.or(ability) branch) keep
            // GTCEu's original cycling preview.
            if (abilityNames.isEmpty()) {
                replacement.add(original);
                continue;
            }

            removeWidget(original);
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("superflatcore.multiblock.ability.available"));
            abilityNames.stream().map(PatternPreviewWidgetMixin::superflatcore$abilityName).forEach(tooltip::add);
            tooltip.addAll(predicate.getToolTips(null));
            tooltip.add(Component.translatable("superflatcore.multiblock.ability.count", predicateCandidates.size()));
            tooltip.add(Component.translatable("superflatcore.multiblock.ability.click"));

            AbilityCandidateSlotWidget slot = new AbilityCandidateSlotWidget(
                    predicateCandidates,
                    List.copyOf(abilityNames),
                    original.getSelfPositionX(),
                    original.getSelfPositionY());
            slot.setBackgroundTexture(original.getBackgroundTexture());
            slot.setOnAddedTooltips((ignored, list) -> list.addAll(tooltip));
            replacement.add(slot);
            addWidget(slot);
        }
        candidates = replacement.toArray(SlotWidget[]::new);
    }

    @Unique
    private static Component superflatcore$abilityName(String abilityName) {
        return Component.translatable("superflatcore.multiblock.ability." + abilityName);
    }
}
