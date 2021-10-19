package com.sollace.coppergolem.client;

import net.minecraft.block.Oxidizable.OxidizationLevel;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.HashMap;
import java.util.Map;

public class CopperGolemEntityRenderer extends MobEntityRenderer<CopperGolemEntity, CopperGolemEntityModel> {
    private final Map<OxidizationLevel, Identifier> textures = new HashMap<>();

    public CopperGolemEntityRenderer(Context ctx) {
        super(ctx, new CopperGolemEntityModel(CopperGolemEntityModel.getTexturedModelData().createModel()), 0.3F);
    }

    @Override
    public Identifier getTexture(CopperGolemEntity entity) {
        return textures.computeIfAbsent(entity.getDegradationLevel(), l -> new Identifier("copper_golem", "textures/entity/copper_golem/copper_golem_" + l.name().toLowerCase() + ".png"));
    }
}
