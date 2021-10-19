package com.sollace.coppergolem.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.HoneycombItem;

public interface MemoizeRegistries {
    OxidizableRegistry OXIDIZABLE = new OxidizableRegistry();
    HoneycombRegistry HONEYCOMB = new HoneycombRegistry();

    final class OxidizableRegistry extends MemoizeBackedRegistry {
        protected OxidizableRegistry() {
            super(() -> Oxidizable.OXIDATION_LEVEL_INCREASES);
        }

        public void register(Block normal, Block exposed, Block weathered, Block oxidized) {
            getEntries().put(normal, exposed);
            getEntries().put(exposed, weathered);
            getEntries().put(weathered, oxidized);
        }
    }

    final class HoneycombRegistry extends MemoizeBackedRegistry {
        protected HoneycombRegistry() {
            super(() -> HoneycombItem.UNWAXED_TO_WAXED_BLOCKS);
        }

        public void register(Block normal, Block waxed) {
            getEntries().put(normal, waxed);
        }
    }
}
