package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Keeps GTCEu's normal stone ore blocks while omitting alternate stone variants. */
@Mixin(value = GTMaterialBlocks.class, remap = false)
public abstract class GTMaterialBlocksOreVariantsMixin {
    @Redirect(
            method = "registerOreBlock",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"),
            remap = false)
    private static Set<Map.Entry<TagPrefix, TagPrefix.OreType>> superflatcore$onlyNormalOre(
            Map<TagPrefix, TagPrefix.OreType> orePrefixes) {
        return orePrefixes.entrySet().stream()
                .filter(entry -> entry.getKey() == TagPrefix.ore)
                .collect(Collectors.toUnmodifiableSet());
    }
}
