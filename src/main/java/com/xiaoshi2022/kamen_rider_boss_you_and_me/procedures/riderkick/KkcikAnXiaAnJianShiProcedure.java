package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
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

		// 首先检查是否可以执行踢击（冷却时间、是否在地面等）
		if (!KicktimeProcedure.canPerformKick(entity)) {
			return;
		}

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
				helmet.getItem() == ModItems.DARK_KIVA_HELMET.get() ||
				helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get() ||
				helmet.getItem() == ModItems.BRAIN_HELMET.get()) && // 新增：Brain头盔
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
				.orElse(new KRBVariables.PlayerVariables()).kick;
	}

	private static void handleKickStart(LevelAccessor world, Entity entity) {
		// 给实体一个适中的向上的推力
		// 计算累积的向上速度
		double currentY = entity.getDeltaMovement().y();
		double newY = currentY + (0.07 * 24); // 直接计算总推力
		
		// 根据玩家视角方向设置水平速度，参考Brain头槌技能的实现
		Vec3 look = entity.getLookAngle();
		double horizontalSpeed = 2.0; // 增加向前推力，参考Brain头槌技能
		double horizontalX = look.x * horizontalSpeed;
		double horizontalZ = look.z * horizontalSpeed;
		
		// 设置完整的速度向量
		entity.setDeltaMovement(new Vec3(horizontalX, newY, horizontalZ));
		// 这样可以确保一次性设置足够大的向上速度和向前速度
		
		// 标记玩家为受伤状态，确保速度变化立即生效
		if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
			livingEntity.hurtMarked = true;
		}

		// 立即设置踢击状态，而不是延迟20tick
		// 这样KicktimeProcedure就能立即开始处理抛物线移动
		setPlayerVariable(entity, "kick", true);
		setPlayerVariable(entity, "wudi", true);

		// 更新最后踢击时间
		entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			capability.lastKickTime = System.currentTimeMillis();
			capability.syncPlayerVariables(entity);
		});
	}

	// 设置玩家变量的通用方法
	private static void setPlayerVariable(Entity entity, String variableName, boolean value) {
		entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			switch (variableName) {
				case "kick" -> capability.kick = value;
				case "wudi" -> capability.wudi = value;
			}
			capability.syncPlayerVariables(entity);
		});
	}
}