package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_s;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Gamma_s_Entity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

/**
 * 眼魔实体渲染器
 * 使用Minecraft原版动画系统，不依赖GeckoLib
 */
public class Gamma_sRenderer extends MobRenderer<Gamma_s_Entity, HumanoidModel<Gamma_s_Entity>> {

    // 实体纹理路径
    private static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/entity/gamma_s.png");

    public Gamma_sRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        
        // 添加护甲层（如果实体需要穿护甲）
        this.addLayer(new HumanoidArmorLayer<>(this, 
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        
        // 设置阴影半径
        this.shadowRadius = 0.5F;
        
        // 添加手部物品渲染层
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Gamma_s_Entity entity) {
        return TEXTURE;
    }

    // 如果需要自定义渲染逻辑，可以重写render方法
    // @Override
    // public void render(Gamma_s_Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
    //                   MultiBufferSource bufferSource, int packedLight) {
    //     // 自定义渲染代码
    //     super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    // }
}