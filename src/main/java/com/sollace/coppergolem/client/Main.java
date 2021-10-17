package com.sollace.coppergolem.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

import com.sollace.coppergolem.entity.GEntities;

public class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(GEntities.COPPER_GOLEM, CopperGolemEntityRenderer::new);
    }
}
