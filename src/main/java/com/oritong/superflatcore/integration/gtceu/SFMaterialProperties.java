package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Naquadria;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Netherite;

public final class SFMaterialProperties {
    private SFMaterialProperties() {}

    public static void registerProperties(MaterialEvent event) {
        Netherite.setProperty(PropertyKey.BLAST, new BlastProperty(2000));
        // Required by the giant fusion structure; GTCEu does not generate this frame by default.
        Naquadria.addFlags(GENERATE_FRAME);
    }
}
