package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.renderer.misc;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.misc.DimensionalBarrier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DimensionalBarrierRenderer extends GeoEntityRenderer<DimensionalBarrier> {
    public DimensionalBarrierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DimensionalBarrierModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(DimensionalBarrier animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/dimensional_br.png");
    }
}