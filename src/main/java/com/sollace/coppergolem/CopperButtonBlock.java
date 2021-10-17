package com.sollace.coppergolem;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class CopperButtonBlock extends AbstractButtonBlock implements CustomDurationButton {
    protected final Oxidizable.OxidizationLevel oxidizationLevel;

    protected CopperButtonBlock(Oxidizable.OxidizationLevel oxidizationLevel, Settings settings) {
        super(false, settings);
        this.oxidizationLevel = oxidizationLevel;
    }

    protected SoundEvent getClickSound(boolean powered) {
        return powered ? SoundEvents.BLOCK_COPPER_PLACE : SoundEvents.BLOCK_COPPER_HIT;
    }

    @Override
    public int getCustomPressTicks() {
        return 30 * (oxidizationLevel.ordinal() + 1);
    }
}
