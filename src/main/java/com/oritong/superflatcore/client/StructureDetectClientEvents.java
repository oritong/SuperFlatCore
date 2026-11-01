package com.oritong.superflatcore.client;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.StructureDetectBehavior;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.portingdeadmods.cable_facades.events.GameClientEvents;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

/** Renders GTOCore-style blue boxes around the errors saved by {@link StructureDetectBehavior}. */
@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT)
public final class StructureDetectClientEvents {
    private static final float FILL_RED = 0.2F;
    private static final float FILL_GREEN = 0.2F;
    private static final float FILL_BLUE = 1.0F;
    private static final float FILL_ALPHA = 0.25F;
    private static final float FRAME_RED = 0.0F;
    private static final float FRAME_GREEN = 0.0F;
    private static final float FRAME_BLUE = 1.0F;
    private static final float FRAME_ALPHA = 0.5F;
    private static final float FRAME_WIDTH = 3.0F;

    private StructureDetectClientEvents() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return;
        }

        ItemStack heldStack = player.getMainHandItem();
        if (!StructureDetectBehavior.isItem(heldStack)) {
            return;
        }

        BlockPos[] positions = StructureDetectBehavior.getErrorPositions(heldStack);
        if (positions == null || positions.length == 0) {
            return;
        }

        renderHighlights(event.getCamera(), event.getPoseStack(), positions);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getEntity().level().isClientSide || !event.getEntity().isShiftKeyDown()
                || !StructureDetectBehavior.isItem(event.getItemStack())) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        boolean enabled = !GameClientEvents.facadeTransparency;
        GameClientEvents.facadeTransparency = enabled;

        ChatFormatting color = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        player.displayClientMessage(Component.translatable("cable_facades.message.transparency_enabled")
                .append(Component.translatable(enabled
                        ? "cable_facades.tooltip.enabled"
                        : "cable_facades.tooltip.disabled").withStyle(color)), true);
        player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.4F, enabled ? 0.01F : 0.09F);
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }

    private static void renderHighlights(Camera camera, PoseStack poseStack, BlockPos[] positions) {
        Vec3 cameraPosition = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        try {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
            for (BlockPos position : positions) {
                RenderBufferUtils.renderCubeFace(poseStack, buffer,
                        position.getX(), position.getY(), position.getZ(),
                        position.getX() + 1, position.getY() + 1, position.getZ() + 1,
                        FILL_RED, FILL_GREEN, FILL_BLUE, FILL_ALPHA, true);
            }
            tesselator.end();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(FRAME_WIDTH);
            for (BlockPos position : positions) {
                RenderBufferUtils.drawCubeFrame(poseStack, buffer,
                        position.getX(), position.getY(), position.getZ(),
                        position.getX() + 1, position.getY() + 1, position.getZ() + 1,
                        FRAME_RED, FRAME_GREEN, FRAME_BLUE, FRAME_ALPHA);
            }
            tesselator.end();
        } finally {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }
}
