package com.sollace.coppergolem.entity.ai;

import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PressButtonGoal extends Goal {

    private final CopperGolemEntity entity;

    private final int maxIdleTicks;

    private int idleTicks;

    private Optional<BlockPos> target = Optional.empty();

    private final Map<BlockPos, Long> visitedPositions = new HashMap<>();

    private final int maxDistance;

    public PressButtonGoal(CopperGolemEntity entity, int maxDistance, int maxIdleTicks) {
        this.entity = entity;
        this.maxDistance = maxDistance;
        this.maxIdleTicks = maxIdleTicks;
        setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {

        if (entity.isPreoccupied()) {
            return false;
        }

        if (idleTicks-- > 0) {
            return false;
        }
        idleTicks = MathHelper.clamp(entity.getRandom().nextInt(maxIdleTicks / 2), 2, maxIdleTicks);

        return entity.getNavigation().isIdle();
    }

    @Override
    public boolean shouldContinue() {
        return !entity.isPreoccupied() && entity.getNavigation().isFollowingPath();
    }

    @Override
    public void start() {
        if (entity.isPreoccupied()) {
            return;
        }

        target = entity.getFinder(maxDistance).pickAny(pos -> {
            Path path = entity.getNavigation().findPathTo(pos, 1);
            if (path == null || recentlyVisited(pos)) {
                return false;
            }

            entity.getNavigation().startMovingAlong(path, getWalkSpeedTo(path.getTarget()));
            return true;
        });
    }

    @Override
    public void stop() {
        target = Optional.empty();
        entity.getNavigation().stop();
    }

    protected double getWalkSpeedTo(BlockPos pos) {
        double distance = entity.getBlockPos().getSquaredDistance(pos);

        if (distance < 2) {
            return entity.getMovementSpeed() * 0.8;
        }

        return entity.getMovementSpeed() * 1.3;
    }

    public void tick() {
        if (entity.isPreoccupied()) {
            return;
        }

        BlockInteraction finder = entity.getFinder(maxDistance);

        target.ifPresent(pos -> {
            target.map(finder::toState).filter(finder::isValid).ifPresentOrElse(state -> {
                entity.getNavigation().setSpeed(getWalkSpeedTo(pos));
                entity.getLookControl().lookAt(pos.getX(), pos.getY(), pos.getZ());
                entity.lookAt(EntityAnchor.FEET, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

                if (entity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 2) {
                    if (finder.perform(entity, pos, state)) {
                        BlockPos entityPos = entity.getBlockPos();

                        entity.setReachDirection(entityPos.getY() < pos.getY() ? CopperGolemEntity.REACHING_UP : CopperGolemEntity.REACHING_DOWN);
                    }
                    stop();
                } else {
                    Path path = entity.getNavigation().findPathTo(pos, 1);
                    if (path != null) {
                        entity.getNavigation().startMovingAlong(path, getWalkSpeedTo(path.getTarget()));
                    }
                }
            }, () -> {
                entity.expressDissappointment();
                stop();
            });
        });
    }

    private boolean recentlyVisited(BlockPos pos) {
        visitedPositions.values().removeIf(l -> l < entity.age);

        boolean visited = visitedPositions.containsKey(pos);
        long visitTime = visitedPositions.computeIfAbsent(pos, p -> (long)entity.age + 10 + entity.getRandom().nextInt(130));

        return visited && visitTime >= entity.age;
    }
}
