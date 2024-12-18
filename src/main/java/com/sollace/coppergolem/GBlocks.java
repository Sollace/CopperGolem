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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface GBlocks {
    List<List<Map.Entry<String, Block>>> COPPER_BLOCKS = new ArrayList<>();

    static <T extends Block> T register(int setId, String name, Function<AbstractBlock.Settings, T> blockFactory) {
        if (setId >= COPPER_BLOCKS.size()) {
            COPPER_BLOCKS.add(new ArrayList<>());
        }
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Main.id(name));
        T block = blockFactory.apply(AbstractBlock.Settings.create().registryKey(key));
        COPPER_BLOCKS.get(setId).add(Map.entry(name, block));
        return Registry.register(Registries.BLOCK, key, block);
    }

    static void bootstrap() {
        var set = Map.of(
            Oxidizable.OxidationLevel.UNAFFECTED, MapColor.ORANGE,
            Oxidizable.OxidationLevel.EXPOSED, MapColor.TERRACOTTA_LIGHT_GRAY,
            Oxidizable.OxidationLevel.WEATHERED, MapColor.DARK_AQUA,
            Oxidizable.OxidationLevel.OXIDIZED, MapColor.TEAL
        );

        // generate buttons
        generateCopperBlocks(set, "button",
                (color, settings) -> settings.requiresTool().strength(3, 6).sounds(BlockSoundGroup.COPPER),
                OxidizableCopperButtonBlock::new, CopperButtonBlock::new
        );

        // generate pressure plates
        generateCopperBlocks(set, "pressure_plate",
                (color, settings) -> settings.mapColor(color).noCollision().requiresTool().strength(0.5F).sounds(BlockSoundGroup.COPPER),
                OxidizableCopperPressurePlateBlock::new, CopperPressurePlateBlock::new
        );
    }

    static <T extends Block> void generateCopperBlocks(Map<Oxidizable.OxidationLevel, MapColor> types, String name,
            BiFunction<MapColor, AbstractBlock.Settings, AbstractBlock.Settings> settingsFunc,
            BiFunction<Oxidizable.OxidationLevel, AbstractBlock.Settings, T> normalMapper,
            BiFunction<Oxidizable.OxidationLevel, AbstractBlock.Settings, T> waxedMapper) {
        generateCopperBlocks(types, name,
                o -> settings -> normalMapper.apply(o.getKey(), settingsFunc.apply(o.getValue(), settings)),
                o -> settings -> waxedMapper.apply(o.getKey(), settingsFunc.apply(o.getValue(), settings))
        );
    }

    static <T extends Block> void generateCopperBlocks(Map<Oxidizable.OxidationLevel, MapColor> types, String name,
            Function<Map.Entry<Oxidizable.OxidationLevel, MapColor>, Function<AbstractBlock.Settings, T>> normalMapper,
            Function<Map.Entry<Oxidizable.OxidationLevel, MapColor>, Function<AbstractBlock.Settings, T>> waxedMapper) {

        int setId = COPPER_BLOCKS.size();
        Block[] oxidizationStates = types.entrySet().stream().map(o -> {
            var id = o.getKey() == Oxidizable.OxidationLevel.UNAFFECTED ? "" : o.getKey().name().toLowerCase() + "_";
            var normal = register(setId, id + "copper_" + name, normalMapper.apply(o));
            var waxed = register(setId + 1, "waxed_" + id + "copper_" + name, waxedMapper.apply(o));
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

        TagKey<Block> CONVENTIONAL_COPPER_LIGHTNING_RODS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("c", "copper_lightning_rods"));

        static TagKey<Block> register(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Main.id(name));
        }
    }
}
