package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.Tyrant;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.TyrantAbilityHandler;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.cache.object.GeoBone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

public class TyrantArmorRenderer extends GeoArmorRenderer<TyrantItem> {

	private static final ResourceLocation TRANSLUCENT_TEXTURE = new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/tyrant_armor_translucent.png");
	// 保存当前被渲染的实体
	private LivingEntity currentEntity;


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
	public void prepForRender(@Nullable Entity entity, ItemStack stack, @Nullable EquipmentSlot slot, @Nullable HumanoidModel<?> baseModel) {
		super.prepForRender(entity, stack, slot, baseModel);
		this.currentEntity = (LivingEntity) entity;
	}

	@Override
	public RenderType getRenderType(TyrantItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		// 使用保存的实体
		if (currentEntity instanceof Player player && TyrantAbilityHandler.isPlayerIntangible(player)) {
			// 使用透明贴图和半透明渲染类型
			return RenderType.entityTranslucent(TRANSLUCENT_TEXTURE);
		}
		// 否则使用普通贴图和半透明渲染类型
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}

}
