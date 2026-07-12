package com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib.entity;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 通用骑士实体模型
 */
public class RiderEffectModel<T extends BaseKamenRiderEffectEntity>
        extends GeoModel<T> {

    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;

    public RiderEffectModel(
            ResourceLocation model,
            ResourceLocation texture,
            ResourceLocation animation
    ) {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
    }

    @Override
    public ResourceLocation getModelResource(T entity) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        return animation;
    }
}
