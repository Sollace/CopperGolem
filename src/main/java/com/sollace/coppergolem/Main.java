package com.sollace.coppergolem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;

import com.sollace.coppergolem.entity.CopperGolemEntity;
import com.sollace.coppergolem.entity.GEntities;
import com.sollace.coppergolem.registry.MobEntityInitGoalsListener;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("CopperGolem");

    @Override
    public void onInitialize() {
        GBlocks.bootstrap();
        GItems.bootstrap();
        GEntities.bootstrap();
        GSounds.bootstrap();

        MobEntityInitGoalsListener.EVENT.register(this::initGoals);
    }

    private void initGoals(MobEntity mob, GoalSelector goals, GoalSelector targets) {
        if (mob instanceof CatEntity cat) {
            targets.add(1, new FleeEntityGoal<>(cat, CopperGolemEntity.class, 5F, 0.8D, 1) {
                public boolean canStart() {
                    return !cat.isSitting() && super.canStart();
                }

                @Override
                public boolean shouldContinue() {
                    return !cat.isSitting() && super.shouldContinue();
                }
            });
        }
    }
}
