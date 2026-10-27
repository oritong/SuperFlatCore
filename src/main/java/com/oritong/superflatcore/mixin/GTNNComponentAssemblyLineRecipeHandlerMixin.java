package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.recipe.ComponentAssemblyRecipeRules;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.arbor.gtnn.data.recipes.handler.ComponentAssemblyLineRecipeHandler", remap = false)
public abstract class GTNNComponentAssemblyLineRecipeHandlerMixin {
    @Shadow @Final private static Object2IntLinkedOpenHashMap<Item> componentItems;

    @Inject(method = "initComponentList", at = @At("TAIL"), remap = false)
    private void superflatcore$restoreComponentTiers(CallbackInfo ci) {
        // CraftingComponent.get falls back to LV at missing tiers, so GTNN's later puts overwrite LV with MAX.
        for (var entry : componentItems.object2IntEntrySet()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.getKey());
            int tier = ComponentAssemblyRecipeRules.getComponentTier(id.getNamespace(), id.getPath());
            if (tier >= 0) {
                entry.setValue(tier);
            }
        }
    }
}
