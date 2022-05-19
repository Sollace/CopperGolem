package com.sollace.coppergolem;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface GItems {
    Item OXIDIZED_COPPER_BUTTON = register("oxidized_copper_button", GBlocks.OXIDIZED_COPPER_BUTTON);
    Item WEATHERED_COPPER_BUTTON = register("weathered_copper_button", GBlocks.WEATHERED_COPPER_BUTTON);
    Item EXPOSED_COPPER_BUTTON = register("exposed_copper_button", GBlocks.EXPOSED_COPPER_BUTTON);
    Item COPPER_BUTTON = register("copper_button", GBlocks.COPPER_BUTTON);

    Item WAXED_OXIDIZED_COPPER_BUTTON = register("waxed_oxidized_copper_button", GBlocks.WAXED_OXIDIZED_COPPER_BUTTON);
    Item WAXED_WEATHERED_COPPER_BUTTON = register("waxed_weathered_copper_button", GBlocks.WAXED_WEATHERED_COPPER_BUTTON);
    Item WAXED_EXPOSED_COPPER_BUTTON = register("waxed_exposed_copper_button", GBlocks.WAXED_EXPOSED_COPPER_BUTTON);
    Item WAXED_COPPER_BUTTON = register("waxed_copper_button", GBlocks.WAXED_COPPER_BUTTON);

    static Item register(String name, Block block) {
        return Registry.register(Registry.ITEM, new Identifier("copper_golem", name), new BlockItem(block, new Item.Settings().group(ItemGroup.REDSTONE)));
    }

    static void bootstrap() {}

    public interface Tags {
        TagKey<Item> COPPER_GOLEM_CAN_PICK_UP = register("copper_golem_can_pick_up");

        static TagKey<Item> register(String name) {
            return TagKey.of(Registry.ITEM_KEY, new Identifier("copper_golem", name));
        }
    }
}
