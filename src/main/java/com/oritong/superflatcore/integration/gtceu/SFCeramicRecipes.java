package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.oritong.superflatcore.integration.gtceu.SFCeramicMaterials.NAQUADAH_OXIDE_CERAMIC;

public final class SFCeramicRecipes {
    private SFCeramicRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        TagPrefix rawCeramicBlock = TagPrefix.get("raw_ceramic_block");
        if (rawCeramicBlock == null) {
            throw new IllegalStateException("The pack's raw_ceramic_block tag prefix is missing");
        }
        ItemStack rawBlock = ChemicalHelper.get(rawCeramicBlock, NAQUADAH_OXIDE_CERAMIC);
        if (rawBlock.isEmpty()) {
            throw new IllegalStateException("Raw naquadah oxide ceramic block was not generated");
        }

        BLAST_RECIPES.recipeBuilder(SuperFlatCore.id("naquadah_oxide_ceramic_dust"))
                .inputItems(dust, Naquadah)
                .inputFluids(Oxygen.getFluid(2_000))
                .outputItems(dust, NAQUADAH_OXIDE_CERAMIC, 3)
                .EUt(VA[IV])
                .duration(200)
                .blastFurnaceTemp(4_500)
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder(SuperFlatCore.id("raw_naquadah_oxide_ceramic_block"))
                .inputItems(dust, NAQUADAH_OXIDE_CERAMIC, 9)
                .inputFluids(Polybenzimidazole.getFluid(1_000))
                .outputItems(rawBlock)
                .EUt(VA[IV])
                .duration(200)
                .save(provider);

        BLAST_RECIPES.recipeBuilder(SuperFlatCore.id("naquadah_oxide_ceramic_block"))
                .inputItems(rawBlock)
                .inputFluids(Helium.getFluid(500))
                .outputItems(block, NAQUADAH_OXIDE_CERAMIC)
                .EUt(VA[IV])
                .duration(600)
                .blastFurnaceTemp(5_400)
                .save(provider);

        // GT's standard block cutter recipe supplies nine plates per sintered
        // block. NO_WORKING prevents the direct ingot-to-plate alternatives.
    }
}
