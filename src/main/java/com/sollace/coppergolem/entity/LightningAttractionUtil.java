package com.sollace.coppergolem.entity;

import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Optional;

public class LightningAttractionUtil {

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

}
