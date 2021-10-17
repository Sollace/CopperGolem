package com.sollace.coppergolem.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

public final class OxidizableRegistry {
    public static final OxidizableRegistry INSTANCE = new OxidizableRegistry();

    private final BiMap<Block, Block> entries = HashBiMap.create();

    private Supplier<BiMap<Block, Block>> vanilla;

    private BiMap<Block, Block> compiled;

    private OxidizableRegistry() {}

    public void register(Block normal, Block exposed, Block weathered, Block oxidized) {
        entries.put(normal, exposed);
        entries.put(exposed, oxidized);
        entries.put(oxidized, weathered);
        compiled = null;
        init();
    }

    private void init() {
        if (vanilla != null) {
            return;
        }

        vanilla = Oxidizable.OXIDATION_LEVEL_INCREASES;

        Field[] fields = Oxidizable.class.getDeclaredFields();
        for (var f : fields) {
            try {
                makeMutable(f);
                if (f.get(null) == vanilla) {
                    f.set(null, (Supplier<BiMap<Block, Block>>)(() -> {
                        if (compiled == null) {
                            compiled = HashBiMap.create(vanilla.get());
                            compiled.putAll(entries);
                        }
                        return compiled;
                    }));
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private static void makeMutable(Field f) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        f.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.set(f, f.getModifiers() & ~Modifier.FINAL);
    }
}
