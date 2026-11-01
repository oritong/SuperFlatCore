package com.oritong.superflatcore.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.oritong.superflatcore.client.TerraPlateMengerRenderer;

import net.minecraft.client.renderer.MultiBufferSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.render.block_entity.TerrestrialAgglomerationPlateBlockEntityRenderer;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;

@Mixin(value = TerrestrialAgglomerationPlateBlockEntityRenderer.class, remap = false)
public abstract class TerrestrialAgglomerationPlateRendererMixin {
    @Inject(method = "render(Lvazkii/botania/common/block/block_entity/TerrestrialAgglomerationPlateBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("TAIL"), remap = false)
    private void superflatcore$renderMengerSponge(TerrestrialAgglomerationPlateBlockEntity plate, float partialTicks,
                                                 PoseStack poseStack, MultiBufferSource buffers,
                                                 int light, int overlay, CallbackInfo ci) {
        TerraPlateMengerRenderer.render(plate, partialTicks, poseStack, buffers, overlay);
    }
}
