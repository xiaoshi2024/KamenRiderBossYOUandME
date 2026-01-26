package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.entity.npc.Villager;

/**
 * 包装MCA渲染器，防止在调用isShaking()方法时发生ClassCastException
 * <p>
 * MCA的VillagerLikeEntityMCARenderer.isShaking()方法会将实体强制转换为Infectable接口
 * 但我们的时间实体没有实现这个接口，因此需要包装器来安全处理
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TimeEntityRendererWrapper extends VillagerRenderer {

    private final VillagerRenderer wrappedRenderer;

    public TimeEntityRendererWrapper(VillagerRenderer wrappedRenderer, EntityRendererProvider.Context context) {
        super(context);
        this.wrappedRenderer = wrappedRenderer;
    }

    @Override
    public void render(Villager p_115327_, float p_115328_, float p_115329_, PoseStack p_115330_, MultiBufferSource p_115331_, int p_115332_) {
        // 调用包装的渲染器的渲染方法
        wrappedRenderer.render(p_115327_, p_115328_, p_115329_, p_115330_, p_115331_, p_115332_);
    }

    @Override
    protected boolean isShaking(Villager p_115334_) {
        try {
            // 尝试加载Infectable接口类
            Class<?> infectableClass = Class.forName("forge.net.mca.entity.Infectable");
            // 检查实体是否实现了Infectable接口
            if (infectableClass.isInstance(p_115334_)) {
                // 如果实现了，调用原始的isShaking方法
                return super.isShaking(p_115334_);
            }
        } catch (ClassNotFoundException e) {
            // 如果Infectable接口不存在，说明MCA可能版本不同或未正确加载，直接返回false
        }
        // 如果实体不是Infectable的实例，返回false，避免ClassCastException
        return false;
    }
}