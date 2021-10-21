package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PositionFinder {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MAX_SCAN_TICKS = 30;
    private static final int NEAR_SCAN_DISTANCE = 3;

    protected final CopperGolemEntity entity;

    protected int minWalkDistance;

    protected final Set<BlockPos> knownPositions = new HashSet<>();

    private int scanCounter = MAX_SCAN_TICKS;

    private final int maxScanDistance;

    public PositionFinder(CopperGolemEntity entity, int maxDistance) {
        this.entity = entity;
        this.maxScanDistance = maxDistance;
    }

    public Optional<BlockPos> pickAny(Predicate<BlockPos> filter) {
        List<BlockPos> positions = findPositions().collect(Collectors.toList());

        while (!positions.isEmpty()) {
            BlockPos pick = positions.remove(entity.getRandom().nextInt(positions.size()));
            if (filter.test(pick)) {
                minWalkDistance = entity.getRandom().nextInt(2);
                return Optional.of(pick);
            }
        }

        return Optional.empty();
    }

    private Stream<BlockPos> findPositions() {
        knownPositions.removeIf(p -> !entity.getEntityWorld().isChunkLoaded(p) || !isValid(toState(p)));

        if (scanCounter-- <= 0) {
            scanCounter = MAX_SCAN_TICKS;

            searchArea(maxScanDistance).forEach(knownPositions::add);
        } else {
            searchArea(NEAR_SCAN_DISTANCE).forEach(knownPositions::add);
        }

        return knownPositions.stream().sorted((a, b) -> Double.compare(entity.getBlockPos().getSquaredDistance(a), entity.getBlockPos().getSquaredDistance(b)));
    }

    protected Stream<BlockPos> searchArea(int range) {
        var stream = BlockPos.streamOutwards(entity.getBlockPos(), range, range, range)
                .filter(this::isExposed)
                .filter(p -> entity.getEntityWorld().isChunkLoaded(p) && isValid(toState(p)));

        if (minWalkDistance > 0) {
            stream = stream.filter(p -> knownPositions.stream().noneMatch(i -> p.isWithinDistance(p, minWalkDistance)));
        }
        return stream.map(BlockPos::toImmutable);
    }

    public abstract boolean isValid(BlockState state);

    public BlockState toState(BlockPos pos) {
        return entity.getEntityWorld().getBlockState(pos);
    }

    private boolean isExposed(BlockPos pos) {
        return Arrays.stream(DIRECTIONS).map(pos::offset).map(this::toState).anyMatch(BlockState::isAir);
    }
}
