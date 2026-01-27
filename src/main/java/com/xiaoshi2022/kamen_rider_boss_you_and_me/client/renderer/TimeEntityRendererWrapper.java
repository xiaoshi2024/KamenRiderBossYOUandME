package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

/**
 * 包装MCA渲染器，防止在调用isShaking()方法时发生ClassCastException
 * <p>
 * MCA的VillagerLikeEntityMCARenderer.isShaking()方法会将实体强制转换为Infectable接口
 * 但我们的时间实体没有实现这个接口，因此需要包装器来安全处理
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TimeEntityRendererWrapper<T extends LivingEntity> extends EntityRenderer<T> {

    private final EntityRenderer<T> wrappedRenderer;

    public TimeEntityRendererWrapper(EntityRenderer<T> wrappedRenderer, EntityRendererProvider.Context context) {
        super(context);
        this.wrappedRenderer = wrappedRenderer;
    }

    @Override
    public void render(T p_115327_, float p_115328_, float p_115329_, PoseStack p_115330_, MultiBufferSource p_115331_, int p_115332_) {
        // 调用包装的渲染器的渲染方法
        wrappedRenderer.render(p_115327_, p_115328_, p_115329_, p_115330_, p_115331_, p_115332_);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_115326_) {
        // 调用包装的渲染器的getTextureLocation方法
        return wrappedRenderer.getTextureLocation(p_115326_);
    }
}

