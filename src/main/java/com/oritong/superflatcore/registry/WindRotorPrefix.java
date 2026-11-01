package com.oritong.superflatcore.registry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.WindRotorItem;

public final class WindRotorPrefix {
    private static final MaterialIconType ICON_TYPE = new MaterialIconType("windRotor");

    public static TagPrefix WIND_ROTOR;

    private WindRotorPrefix() {}

    public static boolean canGenerateFor(Material material) {
        return material.hasProperty(PropertyKey.ROTOR)
                && material.hasFlags(MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_PLATE)
                && !material.hasProperty(PropertyKey.GEM);
    }

    public static void register() {
        if (WIND_ROTOR != null) {
            return;
        }
        WIND_ROTOR = new TagPrefix("wind_rotor")
                .idPattern("%s_wind_rotor")
                .defaultTagPath("wind_rotors/%s")
                .unformattedTagPath("wind_rotors")
                .langValue("%s Wind Rotor")
                .materialAmount(GTValues.M * 8)
                .maxStackSize(1)
                .materialIconType(ICON_TYPE)
                .unificationEnabled(true)
                .generateItem(true)
                .itemConstructor(WindRotorItem::new)
                .generationCondition(WindRotorPrefix::canGenerateFor);
    }
}
