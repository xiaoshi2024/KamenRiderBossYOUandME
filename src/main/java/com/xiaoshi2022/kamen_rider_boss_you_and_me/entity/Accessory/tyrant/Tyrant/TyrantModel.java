package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.Tyrant;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class TyrantModel extends GeoModel<TyrantItem> {
	@Override
	public ResourceLocation getAnimationResource(TyrantItem object) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/tyrant.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(TyrantItem object) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/tyrant.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(TyrantItem object) {
		return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/tyrant.png");
	}
}