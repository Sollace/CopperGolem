package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import com.sollace.coppergolem.CopperButtonBlock;
import com.sollace.coppergolem.entity.CopperGolemEntity;

class ClickButtonInteraction extends BlockInteraction {
    public ClickButtonInteraction(CopperGolemEntity entity, int maxDistance) {
        super(entity, maxDistance);
    }

    @Override
    public boolean perform(CopperGolemEntity entity, BlockPos pos, BlockState state) {
        entity.swingHand(Hand.MAIN_HAND);

        ((AbstractButtonBlock)state.getBlock()).powerOn(state, entity.getEntityWorld(), pos);
        entity.playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1, entity.getSoundPitch());
        return true;
    }

    @Override
    public boolean isValid(BlockState state) {
        return state.getBlock() instanceof CopperButtonBlock && !state.get(AbstractButtonBlock.POWERED);
    }
}
