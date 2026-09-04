package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.integration.ezunclear.EzUnclearIntegration;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.shiory.atomicembers.feature.reactor.blockentity.ReactorInputBlockEntity", remap = false)
public abstract class AtomicReactorInputBlockEntityMixin {
    @Unique
    private static final Logger SUPER_FLAT_CORE_LOGGER =
            LogManager.getLogger("SuperFlatCore/AtomicReactorInput");

    @Unique
    private static final ThreadLocal<Set<BlockPos>> SUPER_FLAT_CORE_EXECUTING_ACCIDENTS =
            ThreadLocal.withInitial(HashSet::new);

    @Inject(method = "triggerAccident", at = @At("HEAD"), cancellable = true, remap = false)
    private void superflatcore$deferAccident(ServerLevel server, BlockPos center, boolean destroyPump,
                                             CallbackInfo ci) {
        Set<BlockPos> executingAccidents = SUPER_FLAT_CORE_EXECUTING_ACCIDENTS.get();
        if (executingAccidents.contains(center)) return;

        AtomicReactorInputBlockEntityInvoker reactor =
                (AtomicReactorInputBlockEntityInvoker) (Object) this;
        if (!EzUnclearIntegration.deferAccident(server, center,
                () -> superflatcore$runAccidentOnServerThread(reactor, server, center, destroyPump))) return;

        ci.cancel();
    }

    @Unique
    private static void superflatcore$runAccidentOnServerThread(
            AtomicReactorInputBlockEntityInvoker reactor, ServerLevel server,
            BlockPos center, boolean destroyPump) {
        Runnable accident = () -> superflatcore$runAccident(reactor, server, center, destroyPump);
        if (server.getServer().isSameThread()) {
            accident.run();
        } else {
            SUPER_FLAT_CORE_LOGGER.info(
                    "EzUnclear released Atomic reactor accident at {}; rescheduling on Server thread.", center);
            server.getServer().execute(accident);
        }
    }

    @Unique
    private static void superflatcore$runAccident(AtomicReactorInputBlockEntityInvoker reactor,
                                                   ServerLevel server, BlockPos center,
                                                   boolean destroyPump) {
        SUPER_FLAT_CORE_LOGGER.info("Executing Atomic reactor accident at {} on Server thread.", center);
        Set<BlockPos> executingAccidents = SUPER_FLAT_CORE_EXECUTING_ACCIDENTS.get();
        executingAccidents.add(center);
        try {
            reactor.superflatcore$triggerAccident(server, center, destroyPump);
        } finally {
            executingAccidents.remove(center);
            if (executingAccidents.isEmpty()) SUPER_FLAT_CORE_EXECUTING_ACCIDENTS.remove();
        }
    }
}
