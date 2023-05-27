package com.sollace.coppergolem.entity.ai;

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
import net.minecraft.registry.Registries;
import net.minecraft.world.GameRules;

import com.sollace.coppergolem.entity.CopperGolemEntity;
import com.sollace.coppergolem.entity.ai.LearnedDuties.Duty;

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
class UseItemInteraction extends BlockInteraction implements LearnedDuties.Receiver {
    private final LearnedDuties learnedDuties = new LearnedDuties();
    private final Set<Identifier> matchingBlocks = new HashSet<>();
    private final Set<Identifier> nonMatchingBlocks = new HashSet<>();

    public UseItemInteraction(CopperGolemEntity entity, int maxDistance) {
        super(entity, maxDistance);
    }

    @Override
    public void learn(BlockState state, Duty duty) {
        learnedDuties.learn(state, duty);
    }

    @Override
    public boolean perform(CopperGolemEntity entity, BlockPos pos, BlockState state) {
        ItemStack stack = entity.getStackInHand(Hand.MAIN_HAND);

        if (!entity.getWorld().getGameRules().get(GameRules.DO_MOB_GRIEFING).get()) {
            return false;
        }

        if (stack.getItem() instanceof BlockItem) {
            return false;
        }

        LearnedDuties.Duty duty = learnedDuties.getDuty(state, entity.getRandom()).orElse(Duty.RIGHT_CLICK);

        ActionResult result;
        switch (duty) {
            case LEFT_CLICK:
                entity.swingHand(Hand.MAIN_HAND);
                entity.startMining(state, pos);
                result = ActionResult.SUCCESS;
                break;
            default:
                result = stack.useOnBlock(new AutomaticItemPlacementContext(entity.getEntityWorld(), pos, entity.getHorizontalFacing(), stack, Direction.UP));
        }

        if (result.shouldSwingHand()) {
            entity.swingHand(Hand.MAIN_HAND);
        }

        Identifier id = Registries.BLOCK.getId(state.getBlock());

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

        if (learnedDuties.isKnown(state)) {
            return true;
        }

        Identifier id = Registries.BLOCK.getId(state.getBlock());

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
        if (tag.contains("learnedDuties", NbtElement.COMPOUND_TYPE)) {
            learnedDuties.readNbt(tag.getCompound("learnedDuties"));
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("matching", writeSet(matchingBlocks));
        tag.put("nonmatching", writeSet(nonMatchingBlocks));
        NbtCompound duties = new NbtCompound();
        learnedDuties.writeNbt(duties);
        tag.put("learnedDuties", duties);
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
