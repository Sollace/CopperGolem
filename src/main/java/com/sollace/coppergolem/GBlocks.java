package com.sollace.coppergolem;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.Oxidizable;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import com.sollace.coppergolem.registry.OxidizableRegistry;

public interface GBlocks {
    Block OXIDIZED_COPPER_BUTTON = register("oxidized_copper_button",
            new OxidizableCopperButtonBlock(Oxidizable.OxidizationLevel.OXIDIZED, AbstractBlock.Settings.of(Material.METAL, MapColor.TEAL).requiresTool().strength(3, 6).sounds(BlockSoundGroup.COPPER))
    );
    Block WEATHERED_COPPER_BUTTON = register("weathered_copper_button",
            new OxidizableCopperButtonBlock(Oxidizable.OxidizationLevel.WEATHERED, AbstractBlock.Settings.of(Material.METAL, MapColor.DARK_AQUA).requiresTool().strength(3, 6).sounds(BlockSoundGroup.COPPER))
    );
    Block EXPOSED_COPPER_BUTTON = register("exposed_copper_button",
            new OxidizableCopperButtonBlock(Oxidizable.OxidizationLevel.EXPOSED, AbstractBlock.Settings.of(Material.METAL, MapColor.TERRACOTTA_LIGHT_GRAY).requiresTool().strength(3, 6).sounds(BlockSoundGroup.COPPER))
    );
    Block COPPER_BUTTON = register("copper_button",
            new OxidizableCopperButtonBlock(Oxidizable.OxidizationLevel.UNAFFECTED, AbstractBlock.Settings.of(Material.METAL, MapColor.ORANGE).requiresTool().strength(3, 6).sounds(BlockSoundGroup.COPPER))
    );

    Block WAXED_OXIDIZED_COPPER_BUTTON = register("waxed_oxidized_copper_button",
            new CopperButtonBlock(Oxidizable.OxidizationLevel.OXIDIZED, AbstractBlock.Settings.copy(OXIDIZED_COPPER_BUTTON))
    );
    Block WAXED_WEATHERED_COPPER_BUTTON = register("waxed_weathered_copper_button",
            new CopperButtonBlock(Oxidizable.OxidizationLevel.WEATHERED, AbstractBlock.Settings.copy(WEATHERED_COPPER_BUTTON))
    );
    Block WAXED_EXPOSED_COPPER_BUTTON = register("waxed_exposed_copper_button",
            new CopperButtonBlock(Oxidizable.OxidizationLevel.EXPOSED, AbstractBlock.Settings.copy(EXPOSED_COPPER_BUTTON))
    );
    Block WAXED_COPPER_BUTTON = register("waxed_copper_button",
            new CopperButtonBlock(Oxidizable.OxidizationLevel.UNAFFECTED, AbstractBlock.Settings.copy(COPPER_BUTTON))
    );

    static <T extends Block> T register(String name, T block) {
        return Registry.register(Registry.BLOCK, new Identifier("copper_golem", name), block);
    }

    static void bootstrap() {
        OxidizableRegistry.INSTANCE.register(COPPER_BUTTON, EXPOSED_COPPER_BUTTON, WEATHERED_COPPER_BUTTON, OXIDIZED_COPPER_BUTTON);
    }
}