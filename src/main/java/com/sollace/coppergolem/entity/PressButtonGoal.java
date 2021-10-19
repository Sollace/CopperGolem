package com.sollace.coppergolem.entity;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import com.sollace.coppergolem.CopperButtonBlock;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PressButtonGoal extends Goal {

    private final CopperGolemEntity entity;

    private final int maxDistance;
    private final int maxIdleTicks;

    private int idleTicks;

    private Optional<BlockPos> targetButton = Optional.empty();

    private final Set<BlockPos> knownButtonPositions = new HashSet<>();
    private final Map<BlockPos, Long> visitedButtons = new HashMap<>();

    private final int maxScanTicks = 30;
    private int buttonScanCounter = maxScanTicks;

    public PressButtonGoal(CopperGolemEntity entity, int maxDistance, int maxIdleTicks) {
        this.entity = entity;
        this.maxDistance = maxDistance;
        this.maxIdleTicks = maxIdleTicks;
        setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {

        if (entity.getHeadSpinTime() > 0) {
            return false;
        }

        if (idleTicks-- > 0) {
            return false;
        }
        idleTicks = maxIdleTicks;

        return entity.getNavigation().isIdle();
    }

    @Override
    public boolean shouldContinue() {
        return entity.getHeadSpinTime() <= 0 && entity.getNavigation().isFollowingPath();
    }

    @Override
    public void start() {
        if (entity.getHeadSpinTime() > 0) {
            return;
        }

        targetButton = pickAnyButton(pos -> {
           Path path = entity.getNavigation().findPathTo(pos, 1);
           if (path == null || path.getLength() == 1 || recentlyVisited(pos)) {
               return false;
           }

           entity.getNavigation().startMovingAlong(path, getWalkSpeedTo(path.getTarget()));

           return true;
        });
    }

    @Override
    public void stop() {
        targetButton = Optional.empty();
        entity.getNavigation().stop();
    }

    protected double getWalkSpeedTo(BlockPos pos) {
        double distance = entity.getBlockPos().getSquaredDistance(pos);

        if (distance < 5) {
            return entity.getMovementSpeed() * 0.8;
        }

        return entity.getMovementSpeed();
    }

    public void tick() {
        if (entity.getHeadSpinTime() > 0) {
            return;
        }

        targetButton.ifPresent(pos -> {
            targetButton.map(this::toState).filter(this::canPress).ifPresentOrElse(state -> {

                entity.getNavigation().setSpeed(getWalkSpeedTo(pos));
                entity.getLookControl().lookAt(pos.getX(), pos.getY(), pos.getZ());

                if (entity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 0.5) {
                    entity.swingHand(Hand.MAIN_HAND);

                    ((AbstractButtonBlock)state.getBlock()).powerOn(state, entity.getEntityWorld(), pos);
                    stop();
                }
            }, this::stop);
        });
    }

    private Optional<BlockPos> pickAnyButton(Predicate<BlockPos> filter) {
        List<BlockPos> positions = findButtons().collect(Collectors.toList());

        while (!positions.isEmpty()) {
            BlockPos pick = positions.remove(entity.getRandom().nextInt(positions.size()));
            if (filter.test(pick)) {
                return Optional.of(pick);
            }
        }

        return Optional.empty();
    }

    private Stream<BlockPos> findButtons() {

        knownButtonPositions.removeIf(p -> !entity.getEntityWorld().isChunkLoaded(p) || !canPress(toState(p)));

        if (buttonScanCounter-- <= 0) {
            buttonScanCounter = maxScanTicks;

            searchArea(maxDistance).forEach(knownButtonPositions::add);
        } else {
            searchArea(2).forEach(knownButtonPositions::add);
        }

        return knownButtonPositions.stream();
    }

    private Stream<BlockPos> searchArea(int range) {
        return BlockPos.streamOutwards(entity.getBlockPos(), range, range, range)
                .filter(p -> entity.getEntityWorld().isChunkLoaded(p) && canPress(toState(p)))
                .map(BlockPos::toImmutable);
    }

    private boolean recentlyVisited(BlockPos pos) {
        visitedButtons.values().removeIf(l -> l < entity.age);

        boolean visited = visitedButtons.containsKey(pos);
        long visitTime = visitedButtons.computeIfAbsent(pos, p -> (long)entity.age + 10 + entity.getRandom().nextInt(130));

        return visited && visitTime >= entity.age;
    }

    private BlockState toState(BlockPos pos) {
        return entity.getEntityWorld().getBlockState(pos);
    }

    protected boolean canPress(BlockState state) {
        return state.getBlock() instanceof CopperButtonBlock && !state.get(AbstractButtonBlock.POWERED);
    }
}
