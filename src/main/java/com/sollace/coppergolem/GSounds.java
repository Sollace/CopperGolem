package com.sollace.coppergolem;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface GSounds {
    SoundEvent ENTITY_COPPER_GOLEM_STEP = register("entity.copper_golem.step");
    SoundEvent ENTITY_COPPER_GOLEM_AMBIENT = register("entity.copper_golem.ambient");
    SoundEvent ENTITY_COPPER_GOLEM_HURT = register("entity.copper_golem.hurt");
    SoundEvent ENTITY_COPPER_GOLEM_DEATH = register("entity.copper_golem.death");
    SoundEvent ENTITY_COPPER_GOLEM_NO = register("entity.copper_golem.no");

    SoundEvent ENTITY_COPPER_GOLEM_WHIRL = register("entity.copper_golem.whirl");

    static SoundEvent register(String name) {
        Identifier id = new Identifier("copper_golem", name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    static void bootstrap() {
    }
}
