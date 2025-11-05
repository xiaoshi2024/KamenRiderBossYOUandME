package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class KkcikAnXiaAnJianShiProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;

		ItemStack helmet = getHelmet(entity);

		// 检查所有符合条件的头盔、是否正在踢击以及玩家是否被控
		if ((helmet.getItem() == ModItems.BARON_LEMON_HELMET.get() ||
				helmet.getItem() == ModItems.DUKE_HELMET.get() ||
				helmet.getItem() == ModItems.MARIKA_HELMET.get() ||
				helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() ||
				helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()||
				helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()||
				helmet.getItem() == ModItems.TYRANT_HELMET.get()||
				helmet.getItem() == ModItems.SIGURD_HELMET.get()||
				helmet.getItem() == ModItems.DARK_RIDER_HELMET.get()||
				helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()||
				helmet.getItem() == ModItems.RIDERNECROM_HELMET.get() ||
				helmet.getItem() == ModItems.DARK_KIVA_HELMET.get()) && // 新增：黑暗Kiva头盔
				!isKicking(entity) &&
				!isPlayerControlled(entity)) {

			handleKickStart(world, entity);
		}
	}
	
	/**
	 * 检查玩家是否被控（有控制类负面效果）
	 */
	private static boolean isPlayerControlled(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return false;
		}
		 
		// 检查玩家是否有以下控制类负面效果
		return livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) || // 缓慢
				livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) || // 失明
				livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION) || // 反胃
				livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.POISON) || // 中毒
				livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.WITHER) || // 凋零
				livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS); // 虚弱
	}

	// 获取头盔
	private static ItemStack getHelmet(Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		}
		return ItemStack.EMPTY;
	}

	// 检查是否正在踢击
	private static boolean isKicking(Entity entity) {
		return entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables()).kcik;
	}

	private static void handleKickStart(LevelAccessor world, Entity entity) {
		// 给实体一个适中的向上的推力（降低高度）
		for (int i = 0; i < 24; i++) { // 从 30 减少到 24
			entity.setDeltaMovement(new Vec3(0, (entity.getDeltaMovement().y() + 0.07), 0)); // 从 0.1 降低到 0.05
		}

		// 延迟设置踢击状态
		kamen_rider_boss_you_and_me.queueServerWork(20, () -> {
			setPlayerVariable(entity, "kcik", true);
			setPlayerVariable(entity, "wudi", true);

			// 更新最后踢击时间
			entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.lastKickTime = System.currentTimeMillis();
				capability.syncPlayerVariables(entity);
			});
		});
	}

	// 设置玩家变量的通用方法
	private static void setPlayerVariable(Entity entity, String variableName, boolean value) {
		entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			switch (variableName) {
				case "kcik" -> capability.kcik = value;
				case "wudi" -> capability.wudi = value;
			}
			capability.syncPlayerVariables(entity);
		});
	}
}