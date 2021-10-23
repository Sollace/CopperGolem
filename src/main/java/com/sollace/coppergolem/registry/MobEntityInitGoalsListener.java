package com.sollace.coppergolem.registry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;

public interface MobEntityInitGoalsListener {
    static Event<MobEntityInitGoalsListener> EVENT = EventFactory.createArrayBacked(MobEntityInitGoalsListener.class, handlers -> {
       return (mob, goals, targets) -> {
           for (var i : handlers) i.initGoals(mob, goals, targets);
       };
    });

    void initGoals(MobEntity mob, GoalSelector goals, GoalSelector targets);
}
