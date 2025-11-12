package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.EliteMonster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMonsterRenderer extends HumanoidMobRenderer<EliteMonsterNpc, HumanoidModel<EliteMonsterNpc>> {

    // 百界众成员皮肤类型
    private static final ResourceLocation[] BAKUSOU_MEMBER_TEXTURES = {
        new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/elite_monster/bakusou_member_1.png"),
        new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/elite_monster/bakusou_member_2.png"),
        new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/elite_monster/bakusou_member_3.png"),
        new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/elite_monster/bakusou_member_4.png"),
        new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/elite_monster/bakusou_member_5.png")
    };
    
    // 默认纹理
    private static final ResourceLocation BASE_TEXTURE = 
            new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/elite_monster/bakusou_member_default.png");

    public EliteMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);

        // 添加盔甲层 - 原版盔甲层系统
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));

        // 添加自定义盔甲纹理层
        this.addLayer(new EliteMonsterArmorLayer(this));

        // 添加手部物品渲染层
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(EliteMonsterNpc entity) {
        // 未变身时使用随机百界众成员皮肤
        if (!entity.isTransformed()) {
            // 使用实体ID的哈希值作为种子，确保每个实体有固定的随机皮肤
            long seed = entity.getId();
            int index = (int)(Math.abs(seed % BAKUSOU_MEMBER_TEXTURES.length));
            return BAKUSOU_MEMBER_TEXTURES[index];
        }
        // 变身后的材质由盔甲层处理，这里返回默认材质
        return BASE_TEXTURE;
    }

    @Override
    protected void scale(EliteMonsterNpc entity, PoseStack poseStack, float partialTickTime) {
        // 可以在这里调整实体缩放
        float scale = 1.0F;
        poseStack.scale(scale, scale, scale);
    }
}