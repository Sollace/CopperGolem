package com.sollace.coppergolem.entity;

import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class VariantSpeedWanderAroundFarGoal extends WanderAroundFarGoal {
    public VariantSpeedWanderAroundFarGoal(PathAwareEntity mob) {
        super(mob, 0);
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(targetX, targetY, targetZ, mob.getMovementSpeed());
    }
}
