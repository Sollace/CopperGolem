package com.sollace.coppergolem;

import net.minecraft.block.Oxidizable;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class CopperPressurePlateBlock extends PressurePlateBlock {

    protected final Oxidizable.OxidationLevel oxidizationLevel;

    public CopperPressurePlateBlock(Oxidizable.OxidationLevel oxidizationLevel, ActivationRule type, Settings settings) {
        super(type, settings);
        this.oxidizationLevel = oxidizationLevel;
    }

    @Override
    protected void playPressSound(WorldAccess world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.9F / (oxidizationLevel.ordinal() + 1));
    }

    @Override
    protected void playDepressSound(WorldAccess world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.75F / (oxidizationLevel.ordinal() + 1));
    }

    @Override
    protected int getTickRate() {
        return 30 * (oxidizationLevel.ordinal() + 1);
    }

}
