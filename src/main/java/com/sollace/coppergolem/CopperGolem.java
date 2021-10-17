package com.sollace.coppergolem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

public class CopperGolem implements ClientModInitializer {

    public static final Logger logger = LogManager.getLogger("CopperGolem");

    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        
    }
}
