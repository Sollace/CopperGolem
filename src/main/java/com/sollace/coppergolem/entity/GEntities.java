package com.sollace.coppergolem.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

import com.sollace.coppergolem.Main;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;

public interface GEntities {

    EntityType<CopperGolemEntity> COPPER_GOLEM = register("copper_golem", EntityType.Builder.create(CopperGolemEntity::new, SpawnGroup.CREATURE)
            .makeFireImmune()
            .dimensions(0.6F, 0.99F));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Main.id(name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    static void bootstrap() {
        FabricDefaultAttributeRegistry.register(COPPER_GOLEM, CopperGolemEntity.createGolemAttributes());
    }
}
