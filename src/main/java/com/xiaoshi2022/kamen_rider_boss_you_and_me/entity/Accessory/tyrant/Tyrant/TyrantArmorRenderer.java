package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.Tyrant;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.cache.object.GeoBone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

public class TyrantArmorRenderer extends GeoArmorRenderer<TyrantItem> {
	public TyrantArmorRenderer() {
		super(new TyrantModel());
		this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
		this.rightLeg = new GeoBone(null, "armorRightLeg", false, (double) 0, false, false);
		this.leftLeg = new GeoBone(null, "armorLeftLeg", false, (double) 0, false, false);
		this.rightBoot = new GeoBone(null, "armorRightBoot", false, (double) 0, false, false);
		this.leftBoot = new GeoBone(null, "armorLeftBoot", false, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(TyrantItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}