package com.oritong.superflatcore.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.oritong.superflatcore.machine.WindGeneratorMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import mekanism.client.render.MekanismRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class WindGeneratorDynamicRender extends DynamicRender<WindGeneratorMachine, WindGeneratorDynamicRender> {
    public static final Codec<WindGeneratorDynamicRender> CODEC = Codec.unit(WindGeneratorDynamicRender::new);
    public static final DynamicRenderType<WindGeneratorMachine, WindGeneratorDynamicRender> TYPE =
            new DynamicRenderType<>(CODEC);

    private static float lastItemFrameTime;
    private static int itemAngle;

    private ModelWindGenerator model;

    @Override
    public DynamicRenderType<WindGeneratorMachine, WindGeneratorDynamicRender> getType() {
        return TYPE;
    }

    @Override
    public void render(WindGeneratorMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            model = new ModelWindGenerator(Minecraft.getInstance().getEntityModels());
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(poseStack, machine.getFrontFacing(), 0, 180, 90, 270);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        double angle = machine.getRotorAngle();
        if (machine.isRotorMoving()) {
            angle += (machine.getHolder().pos().getY() + 4.0) / TileEntityWindGenerator.SPEED_SCALED * partialTick;
        }
        model.render(poseStack, buffer, angle, packedLight, packedOverlay, false);
        poseStack.popPose();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            model = new ModelWindGenerator(Minecraft.getInstance().getEntityModels());
        }

        Minecraft minecraft = Minecraft.getInstance();
        float partialTick = minecraft.getFrameTime();
        if (lastItemFrameTime != partialTick) {
            if (minecraft.level != null) {
                itemAngle = (itemAngle + 2) % 360;
            }
            lastItemFrameTime = partialTick;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        model.render(poseStack, buffer, itemAngle, packedLight, packedOverlay, stack.hasFoil());
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(WindGeneratorMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WindGeneratorMachine machine) {
        BlockPos position = machine.getHolder().pos();
        return new AABB(position.offset(-2, 0, -2), position.offset(3, 7, 3));
    }
}
