package com.sollace.coppergolem.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.HoneycombItem;

public interface MemoizeRegistries {
    OxidizableRegistry OXIDIZABLE = new OxidizableRegistry();
    HoneycombRegistry HONEYCOMB = new HoneycombRegistry();

    final class OxidizableRegistry extends MemoizeBackedRegistry {
        private OxidizableRegistry() {
            super(() -> Oxidizable.OXIDATION_LEVEL_INCREASES);
        }

        public <T extends Block> T[] register(T[] stages) {
            register(stages[0], stages[1], stages[2], stages[3]);
            return stages;
        }

        public void register(Block normal, Block exposed, Block weathered, Block oxidized) {
            getEntries().put(normal, exposed);
            getEntries().put(exposed, weathered);
            getEntries().put(weathered, oxidized);
        }
    }

    final class HoneycombRegistry extends MemoizeBackedRegistry {
        private HoneycombRegistry() {
            super(() -> HoneycombItem.UNWAXED_TO_WAXED_BLOCKS);
        }

        public void register(Block normal, Block waxed) {
            getEntries().put(normal, waxed);
        }
    }
}
