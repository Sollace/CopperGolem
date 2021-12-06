package com.sollace.coppergolem.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface GEntities {

    static EntityType<CopperGolemEntity> COPPER_GOLEM = register("copper_golem", EntityType.Builder.create(CopperGolemEntity::new, SpawnGroup.CREATURE)
            .makeFireImmune()
            .setDimensions(0.6F, 0.99F));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registry.ENTITY_TYPE, new Identifier("copper_golem", name), builder.build(name));
    }

    public static void bootstrap() {
        FabricDefaultAttributeRegistry.register(COPPER_GOLEM, CopperGolemEntity.createMobAttributes());
    }
}
