package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.BrainRoidmude;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Roidmude.BrainRoidmudeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BrainRoidmudeRenderer extends GeoEntityRenderer<BrainRoidmudeEntity> {
    public BrainRoidmudeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BrainRoidmudeModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(BrainRoidmudeEntity instance) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/brain_monster.png");
    }
}
