package com.sollace.coppergolem;

import net.minecraft.block.ButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
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

public class CopperButtonBlock extends ButtonBlock {
    protected final Oxidizable.OxidationLevel oxidizationLevel;

    protected CopperButtonBlock(Oxidizable.OxidationLevel oxidizationLevel, Settings settings) {
        super(settings, BlockSetType.IRON, 0, false);
        this.oxidizationLevel = oxidizationLevel;
    }

    @Override
    protected void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, boolean powered) {
        world.playSound(powered ? player : null, pos, getClickSound(powered), SoundCategory.BLOCKS, 0.3F, (powered ? 0.6F : 0.5F) / (oxidizationLevel.ordinal() + 1));
    }

    @Override
    protected SoundEvent getClickSound(boolean powered) {
        return powered ? SoundEvents.BLOCK_COPPER_HIT : SoundEvents.BLOCK_COPPER_PLACE;
    }

    public int getCustomPressTicks() {
        return 30 * (oxidizationLevel.ordinal() + 1);
    }

    @Override
    public void powerOn(BlockState state, World world, BlockPos pos) {
        world.setBlockState(pos, (BlockState)state.with(POWERED, true), Block.NOTIFY_ALL);
        world.updateNeighborsAlways(pos, this);
        world.updateNeighborsAlways(pos.offset(ButtonBlock.getDirection(state).getOpposite()), this);
        world.scheduleBlockTick(pos, this, getCustomPressTicks());
        CopperGolemEntity.tryBuild(world, pos.offset(state.get(FACING).getOpposite()));
    }
}
