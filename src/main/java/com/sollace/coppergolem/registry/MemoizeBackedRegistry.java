package com.sollace.coppergolem.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sollace.coppergolem.Main;
import net.minecraft.block.Block;

import java.util.function.Supplier;

public class MemoizeBackedRegistry {
    private final BiMap<Block, Block> entries = HashBiMap.create();
    private final BiMap<Block, Block> compiled = HashBiMap.create();

    private Supplier<BiMap<Block, Block>> vanilla;

    private final Supplier<Supplier<BiMap<Block, Block>>> getter;

    protected MemoizeBackedRegistry(Supplier<Supplier<BiMap<Block, Block>>> getter) {
        this.getter = getter;
    }

    protected BiMap<Block, Block> getEntries() {
        compiled.clear();
        init();
        return entries;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        compiled.clear();

        if (vanilla != null) {
            return;
        }

        var replacing = getter.get();

        var c = replacing.getClass();

        var fields = c.getDeclaredFields();

        for (var f : fields) {
            try {
                f.setAccessible(true);
                if (f.getType() == com.google.common.base.Supplier.class) {
                    vanilla = (com.google.common.base.Supplier<BiMap<Block, Block>>) f.get(replacing);
                    if (vanilla == null) {
                        var data = replacing.get();
                        vanilla = () -> data;
                    }
                    f.set(replacing, (com.google.common.base.Supplier<BiMap<Block, Block>>) (() -> {
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
