package com.oritong.superflatcore.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SuperFlatCoreMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String QUICK_MATH_MOD_ID = "pattern_encoding_terminal_quick_math";
    private static final String PROCESSING_PATTERN_AMOUNT_MIXIN =
            "com.oritong.superflatcore.mixin.diamondcore.ae2.MixinSetProcessingPatternAmountScreen";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return "superflatcore.refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!PROCESSING_PATTERN_AMOUNT_MIXIN.equals(mixinClassName)) {
            return true;
        }
        LoadingModList loadingModList = FMLLoader.getLoadingModList();
        return loadingModList == null || loadingModList.getModFileById(QUICK_MATH_MOD_ID) == null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
