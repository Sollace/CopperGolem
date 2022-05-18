package com.sollace.coppergolem.entity.ai;

import com.sollace.coppergolem.entity.CopperGolemEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public abstract class BlockInteraction extends PositionFinder {
    public BlockInteraction(CopperGolemEntity entity, int maxDistance) {
        super(entity, maxDistance);
    }

    public abstract boolean perform(CopperGolemEntity entity, BlockPos pos, BlockState state);

    public static BlockInteraction create(CopperGolemEntity golem, int maxDistance) {
        return golem.getStackInHand(Hand.MAIN_HAND).isEmpty()
            ? new ClickButtonInteraction(golem, maxDistance)
            : new UseItemInteraction(golem, maxDistance);
    }

    public static BlockInteraction fromNbt(CopperGolemEntity golem, NbtCompound tag) {
        var instance = "use_item".equals(tag.getString("id"))
            ? new UseItemInteraction(golem, 0)
            : new ClickButtonInteraction(golem, 0);
        instance.readNbt(tag);
        return instance;
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        this.writeNbt(tag);
        tag.putString("id", this instanceof ClickButtonInteraction ? "click_button" : "use_item");
        return tag;
    }
}
