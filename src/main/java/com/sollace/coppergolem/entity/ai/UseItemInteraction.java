package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * When holding an item, will attempt to use it on random blocks until it finds one that works.
 * - Then after that will prefer the blocks that work.
 * - If there are no known working blocks, goes back to searching
 * - Any further blocks found to work get added to it's memory
 */
class UseItemInteraction extends BlockInteraction {
    private final Set<Identifier> matchingBlocks = new HashSet<>();
    private final Set<Identifier> nonMatchingBlocks = new HashSet<>();

    public UseItemInteraction(CopperGolemEntity entity, int maxDistance) {
        super(entity, maxDistance);
    }

    @Override
    public void perform(CopperGolemEntity entity, BlockPos pos, BlockState state) {
        ItemStack stack = entity.getStackInHand(Hand.MAIN_HAND);

        if (stack.getItem() instanceof BlockItem) {
            return;
        }

        ActionResult result = stack.useOnBlock(new AutomaticItemPlacementContext(entity.getEntityWorld(), pos, entity.getHorizontalFacing(), stack, Direction.UP));

        if (result.shouldSwingHand()) {
            entity.swingHand(Hand.MAIN_HAND);
        }

        Identifier id = Registry.BLOCK.getId(state.getBlock());

        if (result.isAccepted()) {
            nonMatchingBlocks.remove(id);
            matchingBlocks.add(id);
            knownPositions.clear();
        } else {
            if (!matchingBlocks.contains(id)) {
                nonMatchingBlocks.add(id);
            }
            entity.expressDissappointment();
        }
    }

    @Override
    public boolean isValid(BlockState state) {

        Identifier id = Registry.BLOCK.getId(state.getBlock());

        if (state.isAir() || nonMatchingBlocks.contains(id)) {
            return false;
        }

        if (matchingBlocks.isEmpty()) {
            return true;
        }

        return matchingBlocks.contains(id);
    }

    @Override
    protected Stream<BlockPos> searchArea(int range) {
        return super.searchArea(matchingBlocks.isEmpty() ? 3 : range);
    }
}
