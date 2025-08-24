package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RriderbronTouKuiShiJianMeiYouXiKeProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;

		ItemStack helmet = getHelmet(entity);

		// 检查所有符合条件的头盔
		if (helmet.getItem() == ModItems.BARON_LEMON_HELMET.get() ||
				helmet.getItem() == ModItems.DUKE_HELMET.get() ||
				helmet.getItem() == ModItems.MARIKA_HELMET.get() ||
				helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() ||
				helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()||
				helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()||
				helmet.getItem() == ModItems.TYRANT_HELMET.get()||
				helmet.getItem() == ModItems.SIGURD_HELMET.get()) {

			addInvisibilityEffect(entity);
		}
	}

	// 获取头盔
	private static ItemStack getHelmet(Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		}
		return ItemStack.EMPTY;
	}

	// 添加隐身效果
	private static void addInvisibilityEffect(Entity entity) {
		if (entity instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
			livingEntity.addEffect(new MobEffectInstance(
					MobEffects.INVISIBILITY,
					60,
					1,
					false,
					false
			));
		}
	}
}