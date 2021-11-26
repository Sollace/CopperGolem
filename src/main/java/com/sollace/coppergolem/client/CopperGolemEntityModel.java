package com.sollace.coppergolem.client;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import com.sollace.coppergolem.entity.CopperGolemEntity;

public class CopperGolemEntityModel extends SinglePartEntityModel<CopperGolemEntity> {

    private final ModelPart root;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart nose;

    public final ModelPart rightArm;
    public final ModelPart leftArm;

    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CopperGolemEntityModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.head = body.getChild(EntityModelPartNames.HEAD);
        this.nose = head.getChild(EntityModelPartNames.NOSE);
        this.rightArm = body.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = body.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
     }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
                .uv(16, 16).cuboid(-5.75F, -9, -4, 12, 8, 6), ModelTransform.pivot(0, -5, 0));

        body.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-6.25F, -4, -5, 13, 6, 10)
                        .uv(36, 0).cuboid(-0.75F, -9, -1, 2, 5, 2)
                        .uv(0, 22).cuboid(-1.75F, -12, -2, 4, 4, 4), ModelTransform.pivot(0, -11, -1))
                .addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-1.25F, -1, -1.5F, 3, 4, 2), ModelTransform.pivot(0, 0, -5));

        body.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create()
                .uv(48, 0).cuboid(-1.75F, -2, -2, 4, 13, 4), ModelTransform.pivot(-8, -7, -1));

        body.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create()
                .uv(48, 0).cuboid(-2, -2, -2, 4, 13, 4), ModelTransform.pivot(8, -7, -1));
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create()
                .uv(0, 30).cuboid(-2.85F, -1, -3, 6, 6, 6), ModelTransform.pivot(-3, -5, -1));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create()
                .uv(0, 30).cuboid(-3.15F, -1, -3, 6, 6, 6), ModelTransform.pivot(3, -5, -1));

        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(CopperGolemEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

        if (entity.inanimate) {
            limbAngle = entity.limbAngle - entity.limbDistance;
            animationProgress = 1;
        }

        root.pivotY = 24;

        float headSpinTime = (float)entity.getHeadSpinTime() / 10;

        float maxRotation = 2 * (float)Math.PI;

        head.yaw = headYaw * 0.017453292F + MathHelper.lerp(headSpinTime, 0, maxRotation);
        head.pitch = headSpinTime > 0 ? 0 : headPitch * 0.017453292F;

        body.pitch = MathHelper.lerp(handSwingProgress, 0, 0.25F);

        if (riding) {
            rightLeg.pitch = -1.5F;
            rightLeg.yaw = 0.5F;
            leftLeg.pitch = -1.5F;
            leftLeg.yaw = -0.5F;

            rightArm.pitch = -1.25F - 1.125F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
            leftArm.pitch = -1.25F - 1.125F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
        } else {
            rightLeg.yaw = 0;
            rightLeg.pitch = -1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
            leftLeg.yaw = 0;
            leftLeg.pitch = 1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;

            if (entity.isChasing()) {
                rightArm.pitch = -1.25F + (float)MathHelper.sin(animationProgress / 2) / 10F;
                leftArm.pitch = -1.25F + (float)MathHelper.cos(animationProgress / 2) / 10F;
            } else {
                rightArm.pitch = 1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
                leftArm.pitch = -1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
            }
        }

        if (!entity.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
            leftArm.pitch = 0.5F - MathHelper.lerp(handSwingProgress, 0, 1.5F);
        } else {
            rightArm.pitch -=  MathHelper.lerp(handSwingProgress, 0, 1.5F);
            leftArm.pitch -=  MathHelper.lerp(handSwingProgress, 0, 1.5F);
        }

        if (!entity.isWigglingNose()) {
            nose.roll = 0;
            nose.pitch = MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
        } else if (!entity.inanimate) {
            nose.roll = MathHelper.sin(entity.age) / 3;
        }
    }
}
