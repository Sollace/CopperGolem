package com.sollace.coppergolem;

import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Oxidizable;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface GBlocks {
    List<List<Map.Entry<String, Block>>> COPPER_BLOCKS = new ArrayList<>();

    static <T extends Block> T register(int setId, String name, T block) {
        if (setId >= COPPER_BLOCKS.size()) {
            COPPER_BLOCKS.add(new ArrayList<>());
        }
        COPPER_BLOCKS.get(setId).add(Map.entry(name, block));
        return Registry.register(Registries.BLOCK, new Identifier("copper_golem", name), block);
    }

    static void bootstrap() {
        var set = List.of(
            Map.entry(Oxidizable.OxidationLevel.UNAFFECTED, MapColor.ORANGE),
            Map.entry(Oxidizable.OxidationLevel.EXPOSED, MapColor.TERRACOTTA_LIGHT_GRAY),
            Map.entry(Oxidizable.OxidationLevel.WEATHERED, MapColor.DARK_AQUA),
            Map.entry(Oxidizable.OxidationLevel.OXIDIZED, MapColor.TEAL)
        );

        // generate buttons
        generateCopperBlocks(0, set, "button", o -> {
            return new OxidizableCopperButtonBlock(o.getKey(), AbstractBlock.Settings.create()
                    .mapColor(o.getValue()).requiresTool().strength(3, 6).sounds(BlockSoundGroup.COPPER));
        }, (o, settings) -> {
            return new CopperButtonBlock(o.getKey(), settings);
        });

        // generate pressure plates
        generateCopperBlocks(2, set, "pressure_plate", o -> {
            return new OxidizableCopperPressurePlateBlock(o.getKey(), AbstractBlock.Settings.create()
                    .mapColor(o.getValue()).noCollision().requiresTool().strength(0.5F).sounds(BlockSoundGroup.COPPER));
        }, (o, settings) -> {
            return new CopperPressurePlateBlock(o.getKey(), settings);
        });
    }

    static <T extends Block> void generateCopperBlocks(int setId, List<Map.Entry<Oxidizable.OxidationLevel, MapColor>> types, String name,
            Function<Map.Entry<Oxidizable.OxidationLevel, MapColor>, T> normalMapper,
            BiFunction<Map.Entry<Oxidizable.OxidationLevel, MapColor>, AbstractBlock.Settings, T> waxedMapper) {

        Block[] oxidizationStates = types.stream().map(o -> {
            var id = o.getKey() == Oxidizable.OxidationLevel.UNAFFECTED ? "" : o.getKey().name().toLowerCase() + "_";
            var normal = register(setId, id + "copper_" + name, normalMapper.apply(o));
            var waxed = register(setId + 1, "waxed_" + id + "copper_" + name, waxedMapper.apply(o, AbstractBlock.Settings.copy(normal)));
            OxidizableBlocksRegistry.registerWaxableBlockPair(normal, waxed);
            return normal;
        }).toArray(Block[]::new);

        OxidizableBlocksRegistry.registerOxidizableBlockPair(oxidizationStates[0], oxidizationStates[1]);
        OxidizableBlocksRegistry.registerOxidizableBlockPair(oxidizationStates[1], oxidizationStates[2]);
        OxidizableBlocksRegistry.registerOxidizableBlockPair(oxidizationStates[2], oxidizationStates[3]);
    }

    interface Tags {
        TagKey<Block> COPPER_GOLEM_MATERIALS = register("copper_golem_materials");
        TagKey<Block> COPPER_BUTTONS = register("copper_buttons");

        TagKey<Block> CONVENTIONAL_COPPER_LIGHTNING_RODS = TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "copper_lightning_rods"));

        static TagKey<Block> register(String name) {
            return TagKey.of(RegistryKeys.BLOCK, new Identifier("copper_golem", name));
        }
    }
}
