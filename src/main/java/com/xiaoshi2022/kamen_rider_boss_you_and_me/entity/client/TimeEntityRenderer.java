package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;

/**
 * 时间一族实体渲染器
 * 这个渲染器扩展了VillagerRenderer，用于处理时间一族实体的渲染
 * 它覆盖了isShaking方法，以避免MCA模组的ClassCastException
 */
public class TimeEntityRenderer extends VillagerRenderer {
    
    public TimeEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(Villager p_115327_, float p_115328_, float p_115329_, PoseStack p_115330_, MultiBufferSource p_115331_, int p_115332_) {
        // 调用父类的渲染方法
        super.render(p_115327_, p_115328_, p_115329_, p_115330_, p_115331_, p_115332_);
    }
    
    @Override
    protected boolean isShaking(Villager p_115334_) {
        // 覆盖isShaking方法，避免MCA模组的ClassCastException
        // 检查实体是否实现了Infectable接口，如果是则调用默认实现，否则返回false
        try {
            // 使用反射检查实体是否实现了Infectable接口
            Class<?> infectableClass = Class.forName("forge.net.mca.entity.Infectable");
            if (infectableClass.isInstance(p_115334_)) {
                // 如果实现了Infectable接口，调用父类的isShaking方法
                return super.isShaking(p_115334_);
            }
        } catch (ClassNotFoundException e) {
            // 如果Infectable接口不存在（MCA未安装），返回false
        }
        
        // 对于没有实现Infectable接口的实体，返回false
        return false;
    }
}
