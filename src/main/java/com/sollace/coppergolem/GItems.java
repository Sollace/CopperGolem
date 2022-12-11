package com.sollace.coppergolem;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;

public interface GItems {
    static Item register(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier("copper_golem", name), new BlockItem(block, new Item.Settings()));
    }

    static void bootstrap() {
        List<Item> pressurePlates = new ArrayList<>();
        List<Item> buttons = new ArrayList<>();
        GBlocks.COPPER_BLOCKS.stream()
            .flatMap(entry -> entry.stream())
            .forEach(entry -> {
                Block block = entry.getValue();
                Item item = register(entry.getKey(), block);

                if (block instanceof PressurePlateBlock) {
                    pressurePlates.add(item);
                } else {
                    buttons.add(item);
                }
            });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(event -> {
            event.addAfter(Items.STONE_BUTTON, buttons.stream().map(Item::getDefaultStack).toList());
            event.addAfter(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, pressurePlates.stream().map(Item::getDefaultStack).toList());
        });

    }

    interface Tags {
        TagKey<Item> COPPER_GOLEM_CAN_PICK_UP = register("copper_golem_can_pick_up");

        static TagKey<Item> register(String name) {
            return TagKey.of(RegistryKeys.ITEM, new Identifier("copper_golem", name));
        }
    }
}
