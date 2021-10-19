package com.sollace.coppergolem.client;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

import com.sollace.coppergolem.entity.CopperGolemEntity;

public class CopperGolemEntityModel extends SinglePartEntityModel<CopperGolemEntity> {

    private final ModelPart root;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart nose;

    private final ModelPart rightArm;
    private final ModelPart leftArm;

    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CopperGolemEntityModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.head = body.getChild(EntityModelPartNames.HEAD);
        this.nose = head.getChild(EntityModelPartNames.NOSE);
        this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
     }

     public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
                .uv(0, 13).cuboid(-4, -7, -3, 8, 7, 5), ModelTransform.NONE)
                .addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                        .uv(36, 31).cuboid(-1, -1, -1.5F, 2, 2, 2)
                        .uv(0, 0).cuboid(-4, -5, -4, 8, 5, 7)
                        .uv(26, 16).cuboid(-1, -7, -1.5F, 2, 2, 2)
                        .uv(0, 32).cuboid(-1.5F, -9, -2, 3, 3, 3), ModelTransform.pivot(0, -7, 0))
                        .addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create()
                                .uv(36, 26).cuboid(-1, -1, 2, 2, 3, 2), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create()
                .uv(16, 26).cuboid(0, -2, -1.5F, 2, 9, 3), ModelTransform.pivot(-6, -6, -0.5F));
        root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create()
                .uv(26, 26).cuboid(0, -2, -1.5F, 2, 9, 3), ModelTransform.pivot(4, -6, -0.5F));
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create()
                .uv(26, 9).cuboid(-2, 0, -3, 4, 3, 4)
                .uv(23, 0).cuboid(-2, 3, -3, 4, 1, 5), ModelTransform.pivot(-2, 0, 0));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create()
                .uv(0, 25).cuboid(-2, 0, -2, 4, 3, 4)
                .uv(21, 20).cuboid(-2, 3, -2, 4, 1, 5), ModelTransform.pivot(2, 0, 0));
        return TexturedModelData.of(data, 64, 64);
     }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(CopperGolemEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        root.pivotY = 20;
        root.yaw = (float)Math.PI;

        float headSpinTime = (float)entity.getHeadSpinTime() / 10;

        float maxRotation = 2 * (float)Math.PI;

        head.yaw = headYaw * 0.017453292F + MathHelper.lerp(headSpinTime, 0, maxRotation);
        head.pitch = -headPitch * 0.017453292F;

        rightLeg.pitch = -1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
        leftLeg.pitch = 1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;

        rightArm.pitch = -1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
        leftArm.pitch = 1.5F * MathHelper.wrap(limbAngle, 13.0F) * limbDistance;

        body.pitch = MathHelper.lerp(entity.handSwingProgress, 0, -0.25F);
        rightArm.pitch +=  MathHelper.lerp(entity.handSwingProgress, 0, 1.5F);
        leftArm.pitch +=  MathHelper.lerp(entity.handSwingProgress, 0, 1.5F);

        rightArm.pitch = MathHelper.lerp(headSpinTime, rightArm.pitch, root.yaw);
        leftArm.pitch = MathHelper.lerp(headSpinTime, leftArm.pitch, root.yaw);

        if (!entity.isWigglingNose()) {
            nose.pitch = MathHelper.wrap(limbAngle, 13.0F) * limbDistance;
        } else {
            nose.roll = (float)Math.random() - 0.5F;
        }
    }
}
