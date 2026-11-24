package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    @Mutable
    @Shadow
    @Final
    public static int[] DUAL_HATCH_TIERS;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void superflatcore$expandDualHatchTiers(CallbackInfo ci) {
        DUAL_HATCH_TIERS = GTValues.tiersBetween(GTValues.LV, GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UHV);
    }

    @Inject(method = "workableTiered", at = @At("RETURN"), remap = false)
    private static void superflatcore$twoAmpGeneratorTooltip(int tier, long voltage, long energyCapacity,
                                                            GTRecipeType recipeType, long tankCapacity, boolean input,
                                                            CallbackInfoReturnable<Component[]> cir) {
        if (input || !recipeType.registryName.getNamespace().equals("gtceu")) {
            return;
        }
        boolean twoAmpGenerator = switch (recipeType.registryName.getPath()) {
            case "combustion_generator", "gas_turbine", "steam_turbine", "rocket_engine", "naquadah_reactor" -> true;
            default -> false;
        };
        if (twoAmpGenerator) {
            // GT and GTNN share this tooltip builder. Report the two-amp output
            // in the existing EU/t line, retaining the physical voltage tier.
            Component[] tooltips = cir.getReturnValue();
            tooltips[0] = Component.translatable("gtceu.universal.tooltip.voltage_out",
                    FormattingUtil.formatNumbers(voltage * 2), GTValues.VNF[tier]);
        }
    }
}
