package com.oritong.superflatcore.mixin.botania;

import com.google.common.base.Suppliers;
import com.oritong.superflatcore.integration.botania.TerraPlateStructure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.patchouli.api.IMultiblock;

import java.util.function.Supplier;

@Mixin(value = TerrestrialAgglomerationPlateBlockEntity.class, remap = false)
public abstract class TerrestrialAgglomerationPlateBlockEntityMixin extends BlockEntity {
    @Shadow
    @Final
    @Mutable
    public static Supplier<IMultiblock> MULTIBLOCK;

    protected TerrestrialAgglomerationPlateBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void superflatcore$replaceStructure(CallbackInfo ci) {
        MULTIBLOCK = Suppliers.memoize(TerraPlateStructure::createMultiblock);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).expandTowards(0, 5, 0).inflate(2.5);
    }
}
