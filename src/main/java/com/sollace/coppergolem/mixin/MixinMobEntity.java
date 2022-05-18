package com.sollace.coppergolem.mixin;

import com.sollace.coppergolem.registry.MobEntityInitGoalsListener;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
abstract class MixinMobEntity extends LivingEntity {
    MixinMobEntity() {
        super(null, null);
    }

    @Shadow
    protected @Final GoalSelector goalSelector;
    @Shadow
    protected @Final GoalSelector targetSelector;

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void onInit(EntityType<? extends MobEntity> entityType, World world, CallbackInfo info) {
        if (world != null && !world.isClient) {
            MobEntityInitGoalsListener.EVENT.invoker().initGoals((MobEntity) (Object) this, goalSelector, targetSelector);
        }
    }
}
