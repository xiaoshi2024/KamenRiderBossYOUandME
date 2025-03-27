
package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.ghost_by_necrom.necrom_ghost;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.ghost_by_necrom.KnecromghostEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class KnecromghostRenderer extends GeoEntityRenderer<KnecromghostEntity> {
	public KnecromghostRenderer(EntityRendererProvider.Context renderManager) {
		super(renderManager, new KnecromghostModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	public RenderType getRenderType(KnecromghostEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}

	@Override
	public void preRender(PoseStack poseStack, KnecromghostEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green,
			float blue, float alpha) {
		float scale = 1f;
		this.scaleHeight = scale;
		this.scaleWidth = scale;
		super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
