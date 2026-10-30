package com.oritong.superflatcore.integration.gtceu;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFRegistrate;

@GTAddon
public class SteelColorGTAddon implements IGTAddon {
    @Override
    public GTRegistrate getRegistrate() {
        return SFRegistrate.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        GTMaterials.Steel.setMaterialARGB(0xB2B2B2);
    }

    @Override
    public String addonModId() {
        return SuperFlatCore.MODID;
    }
}
