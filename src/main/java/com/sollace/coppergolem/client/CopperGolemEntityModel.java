package com.sollace.coppergolem.client;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CopperGolemEntityModel extends EntityModel<CopperGolemEntityRenderer.State> implements ModelWithArms {

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart nose;

    public final ModelPart rightArm;
    public final ModelPart leftArm;

    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CopperGolemEntityModel(ModelPart root) {
        super(root);
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
                .uv(0, 13).cuboid(-4, -7, -3, 8, 7, 5), ModelTransform.origin(0, 20, 0));

        body.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                        .uv(36, 31).cuboid(-1, -1, -1.5F, 2, 2, 2)
                        .uv(0, 0).cuboid(-4, -5, -4, 8, 5, 7)
                        .uv(26, 16).cuboid(-1, -7, -1.5F, 2, 2, 2)
                        .uv(0, 32).cuboid(-1.5F, -10, -2, 3, 3, 3), ModelTransform.origin(0, -6, 0))
                .addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create()
                        .uv(36, 26).cuboid(-1.5F, -1, 0, 2, 3, 2), ModelTransform.origin(0.5F, -1, 3));
        body.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create()
                .uv(26, 26).cuboid(0, -1, -2, 2, 10, 3), ModelTransform.origin(4, -6, 0));
        body.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create()
                .uv(16, 26).cuboid(0, -1, -2, 2, 10, 3), ModelTransform.origin(-6, -6, 0));
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create()
                .uv(26, 9).cuboid(-2, 0, -3, 4, 3, 4)
                .uv(23, 0).cuboid(-2, 3, -3, 4, 1, 5), ModelTransform.origin(-2, 20, 0));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create()
                .uv(0, 25).cuboid(-2, 0, -2, 4, 3, 4)
                .uv(21, 20).cuboid(-2, 3, -2, 4, 1, 5), ModelTransform.origin(2, 20, -1));

        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public void setAngles(CopperGolemEntityRenderer.State state) {
        super.setAngles(state);
        root.yaw = -MathHelper.PI;
        head.yaw = state.relativeHeadYaw;
        head.pitch = state.pitch;

        float sinAngle = (float)Math.sin(state.handSwingProgress * MathHelper.PI);

        if (state.isInPose(EntityPose.SITTING)) {
            rightLeg.pitch = 1.5F;
            rightLeg.yaw = -0.5F;
            leftLeg.pitch = 1.5F;
            leftLeg.yaw = 0.5F;

            rightArm.pitch = 1.25F + 1.125F * MathHelper.wrap(state.limbSwingAnimationProgress, 13) * state.limbSwingAmplitude;
            leftArm.pitch = 1.25F + 1.125F * MathHelper.wrap(state.limbSwingAnimationProgress, 13) * state.limbSwingAmplitude;
        } else {
            body.pitch = -MathHelper.clamp(sinAngle, 0, 0.25F);

            rightLeg.yaw = 0;
            rightLeg.pitch = MathHelper.cos(state.limbSwingAnimationProgress * 0.6662F) * 1.4F * state.limbSwingAmplitude;
            leftLeg.yaw = 0;
            leftLeg.pitch = MathHelper.cos(state.limbSwingAnimationProgress * 0.6662F + (float)Math.PI) * 1.4F * state.limbSwingAmplitude;

            if (state.chasing) {
                rightArm.pitch = 1.25F + MathHelper.sin(state.age / 2) / 10F;
                leftArm.pitch = 1.25F + MathHelper.cos(state.age / 2) / 10F;
            } else {
                rightArm.pitch = 1.5F * MathHelper.wrap(state.limbSwingAnimationProgress, 13) * state.limbSwingAmplitude;
                rightArm.yaw = -0.5F * MathHelper.wrap(state.limbSwingAnimationProgress, 13) * state.limbSwingAmplitude;
                leftArm.pitch = -1.5F * MathHelper.wrap(state.limbSwingAnimationProgress, 13) * state.limbSwingAmplitude;
                leftArm.yaw = 0.5F * MathHelper.wrap(state.limbSwingAnimationProgress, 13) * state.limbSwingAmplitude;
            }
        }

        if (!state.getMainHandItemState().isEmpty()) {
            rightArm.pitch += 0.5F - MathHelper.clamp(sinAngle, 0, 1.5F);
        } else {
            rightArm.pitch += MathHelper.clamp(sinAngle, 0, 1.5F);
            leftArm.pitch += MathHelper.clamp(sinAngle, 0, 1.5F);
        }

        if (state.reachAmount < 1) {
            float ani = 1 - state.reachAmount;
            float armReachAmnt = ani * 2.5F;
            float wobble = ani * ((float)Math.sin(state.age / 9F) / 9F);

            leftArm.pitch *= state.reachAmount;
            leftArm.pitch += armReachAmnt + wobble;
            rightArm.pitch *= state.reachAmount;
            rightArm.pitch += armReachAmnt - wobble;
            leftArm.roll *= state.reachAmount;
            leftArm.roll += wobble;
            rightArm.roll *= state.reachAmount;
            rightArm.roll -= wobble;
            head.pitch *= state.reachAmount;
            head.pitch += ani * 0.8F;
            head.roll *= state.reachAmount;
            head.roll += ani * ((float)Math.cos(state.age / 9F) / 9F);

            body.pitch *= state.reachAmount;
            body.pitch += ani * 0.3F;
            leftLeg.pitch *= state.reachAmount;
            leftLeg.pitch -= ani * 0.1F;
        } else {
            leftArm.roll = state.armsRoll;
            rightArm.roll = -state.armsRoll;
        }

        nose.roll = state.noseRoll;
        nose.pitch = state.nosePitch;
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-180));
        matrices.translate(0, 1.3, 0);
        rightArm.applyTransform(matrices);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
    }
}
