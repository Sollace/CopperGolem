package com.sollace.coppergolem.client;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

import com.sollace.coppergolem.entity.CopperGolemEntity;

public class CopperGolemEntityRenderer extends MobEntityRenderer<CopperGolemEntity, CopperGolemEntityModel> {
    private static final Identifier TEXTURE = new Identifier("copper_golem", "textures/entity/copper_golem.png");

    public CopperGolemEntityRenderer(Context ctx) {
        super(ctx, new CopperGolemEntityModel(CopperGolemEntityModel.getTexturedModelData().createModel()), 1);
    }

    @Override
    public Identifier getTexture(CopperGolemEntity entity) {
        return TEXTURE;
    }

}
