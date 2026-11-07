package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public final class SFCeramicMaterials {
    // Material flags compare by name. Reuse the pack's raw_ceramic_block prefix
    // without registering a second prefix or depending on script load order.
    private static final MaterialFlag GENERATE_RAW_CERAMIC_BLOCK = new MaterialFlag.Builder("generate_raw_ceramic_block")
            .requireProps(PropertyKey.INGOT)
            .build();

    public static Material NAQUADAH_OXIDE_CERAMIC;

    private SFCeramicMaterials() {}

    public static void registerMaterials(MaterialEvent event) {
        NAQUADAH_OXIDE_CERAMIC = new Material.Builder(GTCEu.id("naquadah_oxide_ceramic"))
                .ingot()
                .color(0x26382B)
                .iconSet(MaterialIconSet.BRIGHT)
                .components(Naquadah, 1, Oxygen, 1)
                .formula("NqO", false)
                .flags(GENERATE_PLATE, GENERATE_RAW_CERAMIC_BLOCK, DISABLE_DECOMPOSITION,
                        NO_SMELTING, NO_WORKING, EXCLUDE_BLOCK_CRAFTING_RECIPES)
                .buildAndRegister();
    }
}
