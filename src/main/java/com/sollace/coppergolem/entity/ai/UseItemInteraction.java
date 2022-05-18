package com.sollace.coppergolem.entity.ai;

import com.sollace.coppergolem.entity.CopperGolemEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.Objects;
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
    public boolean perform(CopperGolemEntity entity, BlockPos pos, BlockState state) {
        ItemStack stack = entity.getStackInHand(Hand.MAIN_HAND);

        if (stack.getItem() instanceof BlockItem) {
            return false;
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
            return true;
        }

        if (!matchingBlocks.contains(id)) {
            nonMatchingBlocks.add(id);
        }
        entity.expressDissappointment();
        return false;
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

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        readSet(matchingBlocks, tag.getList("matching", NbtElement.STRING_TYPE));
        readSet(nonMatchingBlocks, tag.getList("nonmatching", NbtElement.STRING_TYPE));
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("matching", writeSet(matchingBlocks));
        tag.put("nonmatching", writeSet(nonMatchingBlocks));
    }

    private static NbtList writeSet(Set<Identifier> set) {
        NbtList list = new NbtList();
        set.stream().map(Identifier::toString).map(NbtString::of).forEach(list::add);
        return list;
    }

    private static void readSet(Set<Identifier> set, NbtList list) {
        set.clear();
        list.stream().map(NbtElement::asString).map(Identifier::tryParse).filter(Objects::nonNull).forEach(set::add);
    }
}
