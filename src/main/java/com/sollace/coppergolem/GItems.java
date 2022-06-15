package com.sollace.coppergolem;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface GItems {
    static Item register(String name, Block block) {
        return Registry.register(Registry.ITEM, new Identifier("copper_golem", name), new BlockItem(block, new Item.Settings().group(ItemGroup.REDSTONE)));
    }

    static void bootstrap() {
        GBlocks.COPPER_BLOCKS.stream()
            .flatMap(entry -> entry.stream())
            .forEach(entry -> register(entry.getKey(), entry.getValue()));
    }

    interface Tags {
        TagKey<Item> COPPER_GOLEM_CAN_PICK_UP = register("copper_golem_can_pick_up");

        static TagKey<Item> register(String name) {
            return TagKey.of(Registry.ITEM_KEY, new Identifier("copper_golem", name));
        }
    }
}
