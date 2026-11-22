package com.oritong.superflatcore.mixin.client;

import com.mojang.logging.LogUtils;
import com.oritong.superflatcore.client.DiamondContinentTemplate;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Arrays;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow private boolean recreated;
    @Shadow public abstract WorldCreationUiState getUiState();
    @Shadow public abstract void popScreen();

    @Unique private boolean superflatcore$templateDefaultsSet;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void superflatcore$setTemplateDefaults(CallbackInfo ci) {
        if (!recreated && !superflatcore$templateDefaultsSet) {
            DiamondContinentTemplate.setDefaults(getUiState());
            superflatcore$templateDefaultsSet = true;
        }
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;addTabs([Lnet/minecraft/client/gui/components/tabs/Tab;)Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;"))
    private Tab[] superflatcore$removeWorldTab(Tab[] tabs) {
        return Arrays.stream(tabs)
                .filter(tab -> !(tab.getTabTitle().getContents() instanceof TranslatableContents title
                        && title.getKey().equals("createWorld.tab.world.title")))
                .toArray(Tab[]::new);
    }

    @Redirect(method = "createNewWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;createLevelFromExistingSettings(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/ReloadableServerResources;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/world/level/storage/WorldData;)V"))
    private void superflatcore$openWorld(WorldOpenFlows flows, LevelStorageSource.LevelStorageAccess access,
                                         ReloadableServerResources resources, LayeredRegistryAccess<RegistryLayer> registries,
                                         WorldData data) {
        if (!recreated) {
            try {
                DiamondContinentTemplate.install(access, data.overworldData());
            } catch (IOException error) {
                try {
                    access.close();
                } catch (IOException closeError) {
                    error.addSuppressed(closeError);
                }
                LogUtils.getLogger().error("Failed to install Diamond Continent template for {}", access.getLevelId(), error);
                SystemToast.onWorldAccessFailure(minecraft, access.getLevelId());
                popScreen();
                return;
            }
        }
        flows.createLevelFromExistingSettings(access, resources, registries, data);
    }
}
