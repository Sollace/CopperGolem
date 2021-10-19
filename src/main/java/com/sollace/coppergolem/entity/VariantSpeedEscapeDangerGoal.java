package com.sollace.coppergolem.entity;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class VariantSpeedEscapeDangerGoal extends EscapeDangerGoal {
    public VariantSpeedEscapeDangerGoal(PathAwareEntity mob) {
        super(mob, 0);
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(targetX, targetY, targetZ, mob.getMovementSpeed());
        active = true;
    }
}
