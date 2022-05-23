package com.sollace.coppergolem.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.TagKey;

import java.util.function.Predicate;

public interface BlockStatePredicates {

    static Predicate<BlockState> forTag(TagKey<Block> tag) {
        return state -> state.isIn(tag);
    }
}
