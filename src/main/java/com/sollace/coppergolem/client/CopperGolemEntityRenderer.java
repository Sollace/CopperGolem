package com.sollace.coppergolem.client;

import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.HashMap;
import java.util.Map;

public class CopperGolemEntityRenderer extends MobEntityRenderer<CopperGolemEntity, CopperGolemEntityModel> {
    private final Map<OxidationLevel, Identifier> textures = new HashMap<>();

    public CopperGolemEntityRenderer(Context ctx) {
        super(ctx, new CopperGolemEntityModel(CopperGolemEntityModel.getTexturedModelData().createModel()), 0.3F);
        addFeature(new HeldItem(this, ctx.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(CopperGolemEntity entity) {
        return textures.computeIfAbsent(entity.getDegradationLevel(), l -> new Identifier("copper_golem", "textures/entity/copper_golem/copper_golem_" + l.name().toLowerCase() + ".png"));
    }

    public static class HeldItem extends FeatureRenderer<CopperGolemEntity, CopperGolemEntityModel> {

        private final HeldItemRenderer heldItemRenderer;

        public HeldItem(FeatureRendererContext<CopperGolemEntity, CopperGolemEntityModel> context, HeldItemRenderer heldItemRenderer) {
            super(context);
            this.heldItemRenderer = heldItemRenderer;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, CopperGolemEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

            ItemStack item = entity.getEquippedStack(EquipmentSlot.MAINHAND);
            if (item.isEmpty()) {
                return;
            }

            matrices.push();

            getContextModel().getPart().rotate(matrices);
            getContextModel().rightArm.rotate(matrices);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

            boolean isLeft = true;
            matrices.translate((float)(isLeft ? -1 : 1) / 16F, 1 / 16F, 5 / 16F);

            heldItemRenderer.renderItem(entity, item, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, false, matrices, vertices, light);
            matrices.pop();
        }
    }
}
