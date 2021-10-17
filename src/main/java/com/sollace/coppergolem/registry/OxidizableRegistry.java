package com.sollace.coppergolem.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sollace.coppergolem.Main;

import java.util.function.Supplier;

public final class OxidizableRegistry {
    public static final OxidizableRegistry INSTANCE = new OxidizableRegistry();

    private final BiMap<Block, Block> entries = HashBiMap.create();
    private final BiMap<Block, Block> compiled = HashBiMap.create();

    private Supplier<BiMap<Block, Block>> vanilla;

    private OxidizableRegistry() {}

    public void register(Block normal, Block exposed, Block weathered, Block oxidized) {
        entries.put(normal, exposed);
        entries.put(exposed, weathered);
        entries.put(weathered, oxidized);
        compiled.clear();
        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        if (vanilla != null) {
            return;
        }

        var replacing = Oxidizable.OXIDATION_LEVEL_INCREASES;

        var c = replacing.getClass();

        var fields = c.getDeclaredFields();

        for (var f : fields) {
            try {
                f.setAccessible(true);
                if (f.getType() == com.google.common.base.Supplier.class) {
                    vanilla = (com.google.common.base.Supplier<BiMap<Block, Block>>)f.get(replacing);
                    if (vanilla == null) {
                        var data = replacing.get();
                        vanilla = () -> data;
                    }
                    f.set(replacing, (com.google.common.base.Supplier<BiMap<Block, Block>>)(() -> {
                        if (compiled.isEmpty()) {
                            compiled.putAll(vanilla.get());
                            compiled.putAll(entries);
                        }
                        return compiled;
                    }));
                } else if ("initialized".equals(f.getName())) {
                    f.set(replacing, false);
                }
            } catch (Throwable e) {
                Main.LOGGER.error(e);
            }
        }
    }
}
