package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.ghost_by_necrom.necrom_ghost;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.ghost_by_necrom.KnecromghostEntity;
import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;



public class KnecromghostModel extends GeoModel<KnecromghostEntity> {
	@Override
	public ResourceLocation getAnimationResource(KnecromghostEntity entity) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/entity/krnecrom_ghost.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(KnecromghostEntity entity) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/entity/krnecrom_ghost.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(KnecromghostEntity entity) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/" + entity.getTexture() + ".png");
	}

}
