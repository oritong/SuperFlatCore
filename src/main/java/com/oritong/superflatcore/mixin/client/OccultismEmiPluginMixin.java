package com.oritong.superflatcore.mixin.client;

import com.klikli_dev.occultism.integration.emi.OccultismEmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = OccultismEmiPlugin.class, remap = false)
public abstract class OccultismEmiPluginMixin {
    private static final ResourceLocation MINER_CATEGORY = new ResourceLocation("occultism", "miner");

    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/emi/emi/api/EmiRegistry;addRecipe(Ldev/emi/emi/api/recipe/EmiRecipe;)V"
            ),
            remap = false
    )
    private void superflatcore$hideOutdatedMinerRecipes(EmiRegistry registry, EmiRecipe recipe) {
        if (!MINER_CATEGORY.equals(recipe.getCategory().getId())) {
            registry.addRecipe(recipe);
        }
    }
}
