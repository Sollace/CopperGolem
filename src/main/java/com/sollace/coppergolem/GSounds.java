package com.sollace.coppergolem;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface GSounds {
    SoundEvent ENTITY_COPPER_GOLEM_STEP = SoundEvents.BLOCK_COPPER_STEP;//register("entity.copper_golem.step");
    SoundEvent ENTITY_COPPER_GOLEM_AMBIENT = SoundEvents.ENTITY_VILLAGER_AMBIENT;//register("entity.copper_golem.ambient");
    SoundEvent ENTITY_COPPER_GOLEM_HURT = SoundEvents.ENTITY_IRON_GOLEM_HURT;//register("entity.copper_golem.hurt");
    SoundEvent ENTITY_COPPER_GOLEM_DEATH = SoundEvents.ENTITY_IRON_GOLEM_DEATH;//register("entity.copper_golem.death");

    SoundEvent ENTITY_COPPER_GOLEM_WHIRL = register("entity.copper_golem.whirl");

    static SoundEvent register(String name) {
        Identifier id = new Identifier("copper_golem");
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }
}
