package com.oritong.superflatcore.mixin.client;

import dev.emi.emi.platform.EmiAgnos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Prevents EMI from starting its JEI bridge, which can wait forever for JEI runtime callbacks. */
@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker", remap = false)
public abstract class EmiJemiBridgeMixin {
    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/emi/emi/platform/EmiAgnos;isModLoaded(Ljava/lang/String;)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean superflatcore$skipJemiBridge(String modId) {
        return !"jei".equals(modId) && EmiAgnos.isModLoaded(modId);
    }
}
