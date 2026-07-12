package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xiaoshi2022.kamenriderbossyouandme.client.skin.CombinedSkinBuilder;
import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib.entity.RiderEffectModel;
import com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib.entity.RiderEffectRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class FusionEffectRenderer extends RiderEffectRenderer<FusionEffectEntity> {

    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/entity/bloodtx.png");

    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    public FusionEffectRenderer(EntityRendererProvider.Context context) {
        super(context, new RiderEffectModel<>(
                ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/entity/bloodtx.geo.json"),
                DEFAULT_TEXTURE,
                ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/entity/bloodtx.animation.json")
        ));

        // 只加一个层：组合纹理层
        this.addRenderLayer(new CombinedSkinLayer(this));
    }

    /**
     * 组合纹理层 - 同时包含特效和3个玩家皮肤
     */
    private static class CombinedSkinLayer extends GeoRenderLayer<FusionEffectEntity> {

        public CombinedSkinLayer(FusionEffectRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, FusionEffectEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {

            // 获取三个玩家的皮肤
            ResourceLocation[] skins = new ResourceLocation[3];
            for (int i = 0; i < 3; i++) {
                ResourceLocation skin = animatable.getPlayerSkin(i);
                skins[i] = (skin != null) ? skin : DEFAULT_SKIN;
            }

            // 构建缓存键
            String[] names = animatable.getPlayerNames();
            String key = names[0] + "_" + names[1] + "_" + names[2];

            // 获取组合纹理
            ResourceLocation combinedSkin = CombinedSkinBuilder.getOrCreate(key, skins);

            // 渲染整个模型
            RenderType combinedRenderType = RenderType.entityTranslucent(combinedSkin);
            VertexConsumer combinedConsumer = bufferSource.getBuffer(combinedRenderType);

            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                    combinedRenderType, combinedConsumer, partialTick, packedLight, packedOverlay, 0xFFFFFFFF);
        }
    }
}