package com.sollace.coppergolem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.coppergolem.entity.GEntities;

import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("CopperGolem");

    @Override
    public void onInitialize() {
        GBlocks.bootstrap();
        GItems.bootstrap();
        GEntities.bootstrap();
        GSounds.bootstrap();
    }
}
