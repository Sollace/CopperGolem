package com.sollace.coppergolem;

import com.sollace.coppergolem.entity.CopperGolemEntity;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperButtonBlock extends AbstractButtonBlock implements CustomDurationButton {
    protected final Oxidizable.OxidationLevel oxidizationLevel;

    protected CopperButtonBlock(Oxidizable.OxidationLevel oxidizationLevel, Settings settings) {
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

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);

        if (!moved && newState.getBlock() instanceof CopperButtonBlock && newState.get(POWERED)) {
            world.createAndScheduleBlockTick(pos, newState.getBlock(), ((CopperButtonBlock) newState.getBlock()).getCustomPressTicks());
        }
    }

    public void powerOn(BlockState state, World world, BlockPos pos) {
        super.powerOn(state, world, pos);
        CopperGolemEntity.tryBuild(world, pos.offset(state.get(FACING).getOpposite()));
    }
}
