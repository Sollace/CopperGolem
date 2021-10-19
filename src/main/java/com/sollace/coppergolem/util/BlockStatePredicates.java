package com.sollace.coppergolem.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.Tag;

import java.util.function.Predicate;

public interface BlockStatePredicates {

    public static Predicate<BlockState> forTag(Tag<Block> tag) {
        return state -> state.isIn(tag);
    }
}
