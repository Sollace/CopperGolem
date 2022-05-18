package com.sollace.coppergolem.client;

import com.sollace.coppergolem.entity.GEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(GEntities.COPPER_GOLEM, CopperGolemEntityRenderer::new);
    }
}
