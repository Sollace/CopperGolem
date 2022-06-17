package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LearnedDuties {
    private final Map<Identifier, Entry> entries = new HashMap<>();

    public void learn(BlockState state, Duty duty) {

        if (state.isAir()) {
            return;
        }

        Identifier id = Registry.BLOCK.getId(state.getBlock());
        entries.computeIfAbsent(id, Entry::new).duties.add(duty);
    }

    public boolean isKnown(BlockState state) {
        return !state.isAir() && entries.containsKey(Registry.BLOCK.getId(state.getBlock()));
    }

    public Optional<Duty> getDuty(BlockState state, Random random) {
        Set<Duty> duties = entries.getOrDefault(Registry.BLOCK.getId(state.getBlock()), Entry.EMPTY).duties;
        return duties.size() == 0 ? Optional.empty() : Optional.of(List.copyOf(duties).get(random.nextInt(duties.size())));
    }

    public void readNbt(NbtCompound tag) {
        entries.clear();
        tag.getKeys().forEach(key -> {
            Identifier id = Identifier.tryParse(key);
            if (id != null) {
                Entry entry = new Entry(id);
                entry.readNbt(tag.getCompound(key));
                entries.put(id, entry);
            }
        });
    }

    public void writeNbt(NbtCompound tag) {
        entries.keySet().forEach(key -> {
            NbtCompound entry = new NbtCompound();
            entries.get(key).writeNbt(entry);
            tag.put(key.toString(), entry);
        });
    }

    static class Entry {
        static final Entry EMPTY = new Entry(new Identifier("air"));
        public final Identifier id;
        public final Set<Duty> duties = new HashSet<>();

        public Entry(Identifier id) {
            this.id = id;
        }

        public void readNbt(NbtCompound tag) {
            duties.clear();
            Arrays.stream(tag.getIntArray("duties")).filter(i -> i >= 0 && i < 3).distinct().forEach(index -> {
                duties.add(Duty.values()[index]);
            });
        }

        public void writeNbt(NbtCompound tag) {
            tag.putIntArray("duties", duties.stream().mapToInt(i -> i.ordinal()).toArray());
        }
    }

    public static interface Receiver {
        void learn(BlockState state, Duty duty);
    }

    public enum Duty {
        LEFT_CLICK,
        RIGHT_CLICK
    }
}
