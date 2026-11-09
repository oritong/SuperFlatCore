package com.oritong.superflatcore.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;

/** A tiered structure block with idle and active acceleration rail models. */
public final class AccelerationRailBlock extends ActiveBlock {
    private final int tier;

    public AccelerationRailBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }
}
