package com.sollace.coppergolem.client;

import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.sollace.coppergolem.Main;
import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.HashMap;
import java.util.Map;

public class CopperGolemEntityRenderer extends MobEntityRenderer<CopperGolemEntity, CopperGolemEntityRenderer.State, CopperGolemEntityModel> {
    private final Map<OxidationLevel, Identifier> textures = new HashMap<>();

    public CopperGolemEntityRenderer(Context ctx) {
        super(ctx, new CopperGolemEntityModel(CopperGolemEntityModel.getTexturedModelData().createModel()), 0.3F);
        addFeature(new HeldItemFeatureRenderer<>(this, ctx.getItemRenderer()));
    }

    @Override
    public Identifier getTexture(State state) {
        return textures.computeIfAbsent(state.degregationLevel, l -> Main.id("textures/entity/copper_golem/copper_golem_" + l.name().toLowerCase() + ".png"));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    public void updateRenderState(CopperGolemEntity entity, State state, float tickDelta) {
        if (entity.inanimate) {
            tickDelta = 0;
        }
        super.updateRenderState(entity, state, tickDelta);
        BipedEntityRenderer.updateBipedRenderState(entity, state, tickDelta);
        state.degregationLevel = entity.getDegradationLevel();
        state.inanimate = entity.inanimate;
        state.chasing = entity.isChasing();
        state.headSpinTime = entity.getHeadSpinTime() / 10F;
        state.yawDegrees *= 0.017453292F + MathHelper.lerp(state.headSpinTime, 0, MathHelper.TAU);
        state.pitch = state.headSpinTime > 0 ? 0 : -state.pitch * 0.017453292F;
        state.armsRoll = MathHelper.clamp((float)entity.getVelocity().y, 0, 0.5F);
        state.reachAmount = 1 - Math.min(1, (float)MathHelper.sin((entity.getReachAmount(tickDelta) / 200F) * MathHelper.PI) * 12);
        boolean noseWiggling = entity.isWigglingNose();

        state.noseRoll = noseWiggling && !state.inanimate ? MathHelper.sin(state.age) / 3F : 0;
        state.nosePitch = noseWiggling ? 0 : MathHelper.wrap(state.limbFrequency, 13F) * state.limbAmplitudeMultiplier;
    }

    public static class State extends BipedEntityRenderState {
        public OxidationLevel degregationLevel;
        public boolean inanimate;
        public boolean chasing;
        public float headSpinTime;
        public float armsRoll;
        public float reachAmount;

        public float nosePitch;
        public float noseRoll;
    }
}
