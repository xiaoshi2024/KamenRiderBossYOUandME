package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.renderer;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NoxSpecialEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NoxSpecialRenderer extends GeoEntityRenderer<NoxSpecialEntity> {

    public NoxSpecialRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager) {
        super(renderManager, new NoxSpecialModel());
        this.shadowRadius = 0.0F; // 无阴影，因为是特效
    }

    private static class NoxSpecialModel extends GeoModel<NoxSpecialEntity> {
        @Override
        public ResourceLocation getModelResource(NoxSpecialEntity animatable) {
            return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/nox_special.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(NoxSpecialEntity animatable) {
            return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/nox_special.png");
        }

        @Override
        public ResourceLocation getAnimationResource(NoxSpecialEntity animatable) {
            return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/nox_special.animation.json");
        }
    }
}