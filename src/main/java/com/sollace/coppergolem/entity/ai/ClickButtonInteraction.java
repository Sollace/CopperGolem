package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.ButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import com.sollace.coppergolem.CopperButtonBlock;
import com.sollace.coppergolem.GSounds;
import com.sollace.coppergolem.entity.CopperGolemEntity;

class ClickButtonInteraction extends BlockInteraction {
    public ClickButtonInteraction(CopperGolemEntity entity, int maxDistance) {
        super(entity, maxDistance);
    }

    @Override
    public boolean perform(CopperGolemEntity entity, BlockPos pos, BlockState state) {

        if (pos.getY() > entity.getY()) {
            if (pos.getY() > entity.getY() + 1) {
                entity.setReachDirection(CopperGolemEntity.REACHING_UP);
                entity.getNavigation().stop();
                return false;
            }
        }

        entity.swingHand(Hand.MAIN_HAND);

        ((ButtonBlock)state.getBlock()).powerOn(state, entity.getEntityWorld(), pos);
        if (entity.getRandom().nextInt(10) < 2) {
            entity.playSound(GSounds.ENTITY_COPPER_GOLEM_AMBIENT, 1, entity.getSoundPitch());
        }
        return true;
    }

    @Override
    public boolean isValid(BlockState state) {
        return state.getBlock() instanceof CopperButtonBlock && !state.get(ButtonBlock.POWERED);
    }
}
