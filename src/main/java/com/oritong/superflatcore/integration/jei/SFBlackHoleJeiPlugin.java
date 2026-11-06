package com.oritong.superflatcore.integration.jei;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.registry.SFBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** JEI information for the black-hole block and its in-world acquisition ritual. */
public final class SFBlackHoleJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return SuperFlatCore.id("jei_black_hole");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addItemStackInfo(SFBlocks.BLACK_HOLE.asStack(),
                Component.literal("当你使用下落高度超过167格的铁砧同时撞击在同一个方块内的8个“破碎的时空结晶”、8个“奇点”、64个“物质球”掉落物后，它就会在原地生成，并持续吸收周围的方块。"),
                Component.literal("碰到它的大部分生物都会被秒杀。"),
                Component.literal("你可以用纸箱打包带走它。"));
    }
}
