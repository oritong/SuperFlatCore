package com.oritong.superflatcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;

import java.util.Map;
import java.util.WeakHashMap;

public final class TerraPlateMengerRenderer {
    public static final ResourceLocation MODEL_ID = SuperFlatCore.id("block/menger_sponge");

    private static final Map<TerrestrialAgglomerationPlateBlockEntity, Validation> VALIDATIONS = new WeakHashMap<>();

    private TerraPlateMengerRenderer() {}

    public static void render(TerrestrialAgglomerationPlateBlockEntity plate, float partialTicks,
                              PoseStack poseStack, MultiBufferSource buffers, int overlay) {
        Level level = plate.getLevel();
        if (level == null || !isFormed(plate, level)) {
            return;
        }

        double ticks = level.getGameTime() + partialTicks;
        float angle = (float) ((ticks * 1.2) % 360);
        float scale = (float) (2.125 + 0.375 * Math.sin(ticks / 30.0));
        int color = Mth.hsvToRgb((float) ((ticks / 240.0) % 1.0), 0.45F, 1.0F);
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(MODEL_ID);

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 3.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.ZP.rotationDegrees(45));
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, -0.5, -0.5);
            minecraft.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                    buffers.getBuffer(RenderType.translucent()), null, model,
                    red, green, blue, LightTexture.FULL_BRIGHT, overlay);
        } finally {
            poseStack.popPose();
        }
    }

    private static boolean isFormed(TerrestrialAgglomerationPlateBlockEntity plate, Level level) {
        long ticks = level.getGameTime();
        Validation validation = VALIDATIONS.get(plate);
        if (validation == null || ticks < validation.tick() || ticks - validation.tick() >= 10) {
            boolean formed = TerrestrialAgglomerationPlateBlockEntity.MULTIBLOCK.get()
                    .validate(level, plate.getBlockPos().below()) != null;
            validation = new Validation(ticks, formed);
            VALIDATIONS.put(plate, validation);
        }
        return validation.formed();
    }

    private record Validation(long tick, boolean formed) {}
}
