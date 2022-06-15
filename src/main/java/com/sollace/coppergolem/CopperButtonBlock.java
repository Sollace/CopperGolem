package com.sollace.coppergolem;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import org.jetbrains.annotations.Nullable;

import com.sollace.coppergolem.entity.CopperGolemEntity;

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
    protected void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, boolean powered) {
        world.playSound(powered ? player : null, pos, getClickSound(powered), SoundCategory.BLOCKS, 0.3F, (powered ? 0.6F : 0.5F) / (oxidizationLevel.ordinal() + 1));
    }

    @Override
    public int getCustomPressTicks() {
        return 30 * (oxidizationLevel.ordinal() + 1);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);

        if (!moved && newState.getBlock() instanceof CopperButtonBlock && newState.get(POWERED)) {
            world.createAndScheduleBlockTick(pos, newState.getBlock(), ((CopperButtonBlock)newState.getBlock()).getCustomPressTicks());
        }
    }

    public void powerOn(BlockState state, World world, BlockPos pos) {
        super.powerOn(state, world, pos);
        CopperGolemEntity.tryBuild(world, pos.offset(state.get(FACING).getOpposite()));
    }
}
