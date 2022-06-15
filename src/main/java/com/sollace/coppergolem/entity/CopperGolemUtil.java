package com.sollace.coppergolem.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import com.sollace.coppergolem.entity.ai.LearnedDuties;

import java.util.Optional;

public class CopperGolemUtil {

    public static Optional<BlockPos> getGolemAtPos(World world, BlockPos pos) {
        Box box = new Box(pos, new BlockPos(pos.getX(), world.getTopY(), pos.getZ())).expand(3.0);
        return Optional.of(world.getEntitiesByType(GEntities.COPPER_GOLEM, box, golem -> {
            return golem != null && golem.isAlive()
                && golem.getDegradationLevel() == OxidationLevel.OXIDIZED
                && world.isSkyVisible(golem.getBlockPos());
        }))
            .filter(golems -> !golems.isEmpty())
            .map(golems -> golems.get(world.random.nextInt(golems.size())).getBlockPos());
    }

    public static void broadCastLesson(World world, BlockState state, BlockPos center, ItemStack item, LearnedDuties.Duty duty) {
        Box box = new Box(center).expand(3);
        world.getEntitiesByType(GEntities.COPPER_GOLEM, box, golem -> {
            return golem != null && golem.isAlive() && golem.getMainHandStack().isItemEqual(item);
        }).forEach(golem -> {
           golem.teachInteraction(item, state, duty);
        });
    }
}
