package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public final class SFMineralProcessingMaterials {
    private static final MaterialFlag GENERATE_MILLED = new MaterialFlag.Builder("generate_milled").build();
    private static final MaterialIconType MILLED_ICON = new MaterialIconType("milled");
    private static TagPrefix milledPrefix;

    private SFMineralProcessingMaterials() {}

    public static void registerMilledPrefix() {
        if (milledPrefix != null) {
            return;
        }
        milledPrefix = new TagPrefix("milled")
                .idPattern("milled_%s")
                .defaultTagPath("milled/%s")
                .unformattedTagPath("milled")
                .langValue("Milled %s")
                .materialAmount(GTValues.M)
                .materialIconType(MILLED_ICON)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(material -> material.hasFlag(GENERATE_MILLED));
    }

    public static void registerMaterials(MaterialEvent event) {
        Nickel.addFlags(GENERATE_MILLED);
        Platinum.addFlags(GENERATE_MILLED);
        Almandine.addFlags(GENERATE_MILLED);
        Grossular.addFlags(GENERATE_MILLED);
        Pyrope.addFlags(GENERATE_MILLED);
        Spessartine.addFlags(GENERATE_MILLED);
        Chalcopyrite.addFlags(GENERATE_MILLED);
        Pentlandite.addFlags(GENERATE_MILLED);
        Sphalerite.addFlags(GENERATE_MILLED);
        Monazite.addFlags(GENERATE_MILLED);
        Redstone.addFlags(GENERATE_MILLED);
        NaquadahEnriched.addFlags(GENERATE_MILLED);
        if (!NaquadahEnriched.hasProperty(PropertyKey.ORE)) {
            NaquadahEnriched.setProperty(PropertyKey.ORE, new OreProperty());
        }
        NaquadahEnriched.getProperty(PropertyKey.ORE).setOreByProducts(Naquadah, Sulfur);

        new Material.Builder(GTCEu.id("sodium_ethylxanthate"))
                .dust()
                .color(0xcdad00)
                .components(Carbon, 3, Hydrogen, 5, Sodium, 1, Oxygen, 1, Sulfur, 2)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();

        new Material.Builder(GTCEu.id("potassium_ethylxanthate"))
                .dust()
                .color(0xcdc8b1)
                .components(Carbon, 3, Hydrogen, 5, Potassium, 1, Oxygen, 1, Sulfur, 2)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();

        new Material.Builder(GTCEu.id("turpentine"))
                .fluid()
                .components(Carbon, 10, Hydrogen, 16)
                .color(0x9acd32)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .iconSet(MaterialIconSet.FLUID)
                .buildAndRegister();

        registerFront("almandine_front", Almandine, 0xb22222);
        registerFront("chalcopyrite_front", Chalcopyrite, 0xcdaa7d);
        registerFront("grossular_front", Grossular, 0xd2691e);
        registerFront("monazite_front", Monazite, 0x838b83);
        registerFront("nickel_front", Nickel, 0xc1cdcd);
        registerFront("platinum_front", Platinum, 0xcdc9a5);
        registerFront("pyrope_front", Pyrope, 0x8b0000);
        registerFront("redstone_front", Redstone, 0xee0000);
        registerFront("spessartine_front", Spessartine, 0xee5c42);
        registerFront("sphalerite_front", Sphalerite, 0xeee9e9);
        registerFront("pentlandite_front", Pentlandite, 0xcdaa7d);
        registerFront("enriched_naquadah_front", NaquadahEnriched, 0x58d00f);

        new Material.Builder(GTCEu.id("red_mud"))
                .fluid()
                .color(0x972903)
                .iconSet(MaterialIconSet.FLUID)
                .formula("HCl?", false)
                .buildAndRegister();
    }

    private static Material registerFront(String name, Material oreMaterial, int color) {
        return new Material.Builder(GTCEu.id(name))
                .fluid()
                .components(oreMaterial, 1)
                .color(color)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .iconSet(MaterialIconSet.FLUID)
                .buildAndRegister();
    }
}
