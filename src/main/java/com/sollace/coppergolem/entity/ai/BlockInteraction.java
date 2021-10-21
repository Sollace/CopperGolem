package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import com.sollace.coppergolem.entity.CopperGolemEntity;

public abstract class BlockInteraction extends PositionFinder {
    public BlockInteraction(CopperGolemEntity entity, int maxDistance) {
        super(entity, maxDistance);
    }

    public abstract void perform(CopperGolemEntity entity, BlockPos pos, BlockState state);

    public static BlockInteraction create(CopperGolemEntity golem, int maxDistance) {
        return golem.getStackInHand(Hand.MAIN_HAND).isEmpty()
                ? new ClickButtonInteraction(golem, maxDistance)
                : new UseItemInteraction(golem, maxDistance);
    }
}
