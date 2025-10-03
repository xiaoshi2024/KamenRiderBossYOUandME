package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.Ridernecrom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RidernecromModel extends GeoModel<RidernecromItem> {
	@Override
	public ResourceLocation getAnimationResource(RidernecromItem object) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/rider_necrom.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(RidernecromItem object) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/rider_necrom.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(RidernecromItem object) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/rider_necrom.png");
	}
}
