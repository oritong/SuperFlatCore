package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep the material's ceramic suffix out of the raw-block prefix. */
@Mixin(value = TagPrefix.class, remap = false)
public abstract class GTCeramicTagPrefixMixin {
    @Shadow @Final public String name;

    @Inject(method = "getUnlocalizedName()Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void superflatcore$rawCeramicName(CallbackInfoReturnable<String> cir) {
        if ("raw_ceramic_block".equals(name)) {
            // Use a core-owned key so KubeJS's old "%s ceramic blank"
            // translation cannot add a second ceramic suffix.
            cir.setReturnValue("tagprefix.superflatcore.raw_ceramic_block");
        }
    }
}
