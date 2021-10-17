package com.sollace.coppergolem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class OxidizableCopperButtonBlock extends CopperButtonBlock implements Oxidizable {

    protected OxidizableCopperButtonBlock(Oxidizable.OxidizationLevel oxidizationLevel, Settings settings) {
        super(oxidizationLevel, settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        tickDegradation(state, world, pos, random);
    }

    @Override
    public Oxidizable.OxidizationLevel getDegradationLevel() {
       return this.oxidizationLevel;
    }
}
