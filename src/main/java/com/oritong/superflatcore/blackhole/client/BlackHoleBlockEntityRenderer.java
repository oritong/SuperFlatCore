package com.oritong.superflatcore.blackhole.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oritong.superflatcore.blackhole.BlackHoleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Renders the block currently being consumed while the skill compositor renders the hole itself. */
public final class BlackHoleBlockEntityRenderer implements BlockEntityRenderer<BlackHoleBlockEntity> {
    public BlackHoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlackHoleBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState pulled = entity.currentlyPulling;
        if (pulled == null || entity.currentlyPullingFrom == null || entity.getLevel() == null) {
            return;
        }
        long now = entity.getLevel().getGameTime();
        if (entity.pullTime <= 0 || now >= entity.pullingStartedAt + entity.pullTime || pulled.isAir()) {
            return;
        }

        float progress = (float) Math.pow(
                (now + partialTick - entity.pullingStartedAt) / (float) entity.pullTime, 1.3F);
        progress = Math.max(0.0F, Math.min(1.0F, progress));
        Vec3 source = Vec3.atLowerCornerOf(entity.currentlyPullingFrom);
        Vec3 target = entity.getBlockPos().getCenter();
        Vec3 offset = target.subtract(source).scale(1.0 - progress);
        float rotation = progress * entity.pullTime * 3.0F;

        poseStack.pushPose();
        poseStack.translate(.5, .5, .5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-offset.x, -offset.y, -offset.z);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        float scale = 1.0F - progress;
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                pulled, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
