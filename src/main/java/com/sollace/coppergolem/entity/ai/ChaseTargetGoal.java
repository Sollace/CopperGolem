package com.sollace.coppergolem.entity.ai;

import com.sollace.coppergolem.entity.CopperGolemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ChaseTargetGoal extends Goal {

    private final CopperGolemEntity mob;

    @Nullable
    private LivingEntity target;

    public ChaseTargetGoal(CopperGolemEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean shouldContinue() {
        return (target != null && target.isAlive() && mob.squaredDistanceTo(target) <= 225) && (!mob.getNavigation().isIdle() || canStart());
    }

    public void stop() {
        target = null;
        mob.setChasing(false);
        mob.getNavigation().stop();
    }

    public void tick() {
        mob.getLookControl().lookAt(target, 30, 30);

        double entityBaseArea = mob.getWidth() * 2 * mob.getWidth() * 2;
        double distance = mob.squaredDistanceTo(target.getX(), target.getY(), target.getZ());

        double speed = 0.8D;

        boolean isNear = distance > entityBaseArea && distance < 16;

        if (isNear) {
            speed = 1.33D; // speed factor when in reach
        } else if (distance < 225) {
            speed = 0.6D; // speed factor when out of reach
        }

        mob.setChasing(isNear);
        mob.getNavigation().startMovingTo(target, speed * mob.getMovementSpeed());
    }
}
