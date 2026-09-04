package com.oritong.superflatcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.reactor.blockentity.ReactorInputBlockEntity", remap = false)
public interface AtomicReactorInputBlockEntityInvoker {
    @Invoker(value = "triggerAccident", remap = false)
    void superflatcore$triggerAccident(ServerLevel server, BlockPos center, boolean destroyPump);
}
