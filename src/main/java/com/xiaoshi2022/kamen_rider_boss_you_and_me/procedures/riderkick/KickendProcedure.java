package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatStampFinishEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkGhostRiderKickEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NecromEyexEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KickDamageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class KickendProcedure {

	private static final Map<UUID, Integer> playerToEffectEntity = new WeakHashMap<>();
	// 添加静态变量来跟踪每个玩家的落地状态和跳跃状态
	private static final Map<UUID, Boolean> playerLandingState = new WeakHashMap<>();
	private static final Map<UUID, Boolean> playerJumpingState = new WeakHashMap<>();
    private static final boolean DEBUG = false;

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Player entity = event.player;
			LevelAccessor world = event.player.level();
			execute(event, world, entity);
		}
	}

	public static void execute(@Nullable Event event, LevelAccessor world, Player entity) {
		if (entity == null)
			return;

		KRBVariables.PlayerVariables variables = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		// 检查玩家是否处于踢击状态且装备了正确的头盔
		ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
		UUID playerId = entity.getUUID();
		
		// 记录上一状态
		boolean wasOnGround = playerLandingState.getOrDefault(playerId, false);
		boolean wasJumping = playerJumpingState.getOrDefault(playerId, false);
		
		// 更新当前状态
		boolean isOnGround = entity.onGround();
		// 只在kick标志为true时才视为骑士踢跳跃，避免正常跳跃触发
		boolean isJumping = variables.kick;
		
		playerLandingState.put(playerId, isOnGround);
		playerJumpingState.put(playerId, isJumping);

		if (DEBUG && !world.isClientSide()) {
			kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Player {} - kick: {}, isHelmetValid: {}, onGround: {}, wasOnGround: {}, isJumping: {}, wasJumping: {}", 
					entity.getScoreboardName(), variables.kick, isHelmetValid(helmet), isOnGround, wasOnGround, isJumping, wasJumping);
			if (!isHelmetValid(helmet)) {
				kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Helmet invalid - item: {}", helmet.getItem());
			}
		}
		
		// 检查传统踢击结束条件
		if (variables.kick && isHelmetValid(helmet) && isOnGround) {
			// 玩家落地，处理踢击结束逻辑
			handleKickEnd(event, world, entity);
		}
		
		// 新增：检测玩家从空中落地的情况，即使kick标志已经被重置
		if (!wasOnGround && isOnGround && wasJumping && isHelmetValid(helmet)) {
			// 玩家从空中落地，处理落地伤害逻辑
			handleLandingDamage(world, entity);
		}

		// 处理无敌状态的计时和重置
		handleInvulnerabilityReset(world, entity, variables);
	}

	// 处理踢击结束的逻辑
	private static void handleKickEnd(@Nullable Event event, LevelAccessor world, Player entity) {
		if (DEBUG && !world.isClientSide()) {
			kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Handling kick end for player {}", entity.getScoreboardName());
		}

		// 设置爆炸标志并重置踢击标志
		KRBVariables.PlayerVariables variables = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		variables.needExplode = true;
		variables.kick = false;
// 立即同步变量，确保网络延迟不会影响伤害逻辑
		variables.syncPlayerVariables(entity);
		// 获取头盔
		ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
		boolean isBrainHelmet = helmet.getItem() == ModItems.BRAIN_HELMET.get();

		// 服务器端处理爆炸效果和伤害
		if (!world.isClientSide()) {
			// Brain骑士踢没有特效和音效，只处理伤害
			if (!isBrainHelmet) {
				// 播放爆炸音效
				world.playSound(null, new BlockPos((int) entity.getX(), (int) entity.getY(), (int) entity.getZ()),
						SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

				// 创建爆炸粒子效果
				ServerLevel serverLevel = (ServerLevel) world;
				serverLevel.sendParticles(
						ParticleTypes.EXPLOSION,
						entity.getX(),
						entity.getY(),
						entity.getZ(),
						10, 0.5, 0.5, 0.5, 0.1
				);

				// 处理方块破坏（如果允许的话）
				if (variables.allowKickBlockDamage) {
					// 获取爆炸半径和破坏强度
					float radius = KickDamageHelper.getKickExplosionRadius(entity);
					float destructionStrength = KickDamageHelper.getKickBlockDestructionStrength(entity);

					// 创建一个不会破坏方块的爆炸，但会对实体造成伤害
					serverLevel.explode(
						entity, 
						entity.getX(), 
						entity.getY(), 
						entity.getZ(), 
						radius, 
						false, // 是否会引起火灾
						Level.ExplosionInteraction.NONE // 不破坏方块
					);

					// 自定义方块破坏处理（如果允许的话）
					if (destructionStrength > 0) {
							performCustomBlockDestruction(serverLevel, entity, radius, destructionStrength);
					}
				}

				// 清理特效实体
				cleanupEffectEntity(entity, (ServerLevel) world);
			}

			// 专门处理基夫石像的伤害
			handleGiifuDamage(world, entity);

			// 处理普通敌人的伤害
			handleEnemyDamage(world, entity);
		}

		// 客户端处理动画
		if (world.isClientSide()) {
			handleAnimation(entity);
		}

		// 设置无敌状态
		variables.wudi = true;
		variables.syncPlayerVariables(entity);
	}

	// 新增：直接处理落地伤害逻辑，不依赖kick标志
	private static void handleLandingDamage(LevelAccessor world, Player entity) {
		if (DEBUG && !world.isClientSide()) {
			kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Handling landing damage for player {}", entity.getScoreboardName());
		}

		// 获取头盔
		ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
		if (!isHelmetValid(helmet)) {
			if (DEBUG && !world.isClientSide()) {
				kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Helmet invalid for landing damage - item: {}", helmet.getItem());
			}
			return;
		}

		// 获取玩家变量
		KRBVariables.PlayerVariables variables = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		// 服务器端处理爆炸效果和伤害
		if (!world.isClientSide()) {
			boolean isBrainHelmet = helmet.getItem() == ModItems.BRAIN_HELMET.get();
			
			// Brain骑士踢没有特效和音效，只处理伤害
			if (!isBrainHelmet) {
				// 播放爆炸音效
				world.playSound(null, new BlockPos((int) entity.getX(), (int) entity.getY(), (int) entity.getZ()),
						SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

				// 创建爆炸粒子效果
				ServerLevel serverLevel = (ServerLevel) world;
				serverLevel.sendParticles(
						ParticleTypes.EXPLOSION,
						entity.getX(),
						entity.getY(),
						entity.getZ(),
						10, 0.5, 0.5, 0.5, 0.1
				);

				// 清理特效实体
				cleanupEffectEntity(entity, serverLevel);
			}

			// 专门处理基夫石像的伤害
			handleGiifuDamage(world, entity);

			// 处理普通敌人的伤害
			handleEnemyDamage(world, entity);
		}

		// 重置踢击标志并设置无敌状态
		variables.kick = false;
		variables.needExplode = true;
		variables.wudi = true;
		variables.syncPlayerVariables(entity);
	}

	// 检查头盔是否有效
	private static boolean isHelmetValid(ItemStack helmet) {
		return helmet.getItem() == ModItems.SIGURD_HELMET.get() ||
			helmet.getItem() == ModItems.BARON_LEMON_HELMET.get() ||
			helmet.getItem() == ModItems.DUKE_HELMET.get() ||
			helmet.getItem() == ModItems.MARIKA_HELMET.get() ||
			helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() ||
			helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get() ||
			helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get() ||
			helmet.getItem() == ModItems.TYRANT_HELMET.get() ||
			helmet.getItem() == ModItems.EVIL_BATS_HELMET.get() ||
			helmet.getItem() == ModItems.DARK_KIVA_HELMET.get() ||
			helmet.getItem() == ModItems.DARK_RIDER_HELMET.get() ||
			helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get() ||
			helmet.getItem() == ModItems.RIDERNECROM_HELMET.get() ||
			helmet.getItem() == ModItems.BRAIN_HELMET.get() ||
			isDarkGhostHelmet(helmet);
	}
	
	// 检查是否是黑暗ghost头盔
	private static boolean isDarkGhostHelmet(ItemStack helmet) {
		return helmet != null && !helmet.isEmpty() && 
				helmet.getItem() == ModItems.DARK_RIDER_HELMET.get();
	}

	// 处理无敌状态的重置
	private static void handleInvulnerabilityReset(LevelAccessor world, Player entity, KRBVariables.PlayerVariables variables) {
		// 如果处于无敌状态
		if (variables.wudi) {
			// 只在服务器端执行延迟任务
			if (!world.isClientSide() && entity.getServer() != null) {
				// 创建一个延迟任务来重置无敌状态 - 延迟20tick（1秒）
				kamen_rider_boss_you_and_me.queueServerWork(20, () -> {
					KRBVariables.PlayerVariables delayedVariables = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
							.orElse(new KRBVariables.PlayerVariables());

					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Resetting invulnerability for player {}", entity.getScoreboardName());
					}

					delayedVariables.wudi = false;
					delayedVariables.needExplode = false;
					delayedVariables.syncPlayerVariables(entity);
				});
			} else if (world.isClientSide()) {
				// 客户端直接重置变量（如果需要的话）
				variables.wudi = false;
				variables.needExplode = false;
				variables.syncPlayerVariables(entity);
			}
		}
	}

	// 处理基夫石像的伤害
	private static void handleGiifuDamage(LevelAccessor world, Player entity) {
		// 获取基夫石像
		List<Entity> giifuList = world.getEntities(
				entity,
				entity.getBoundingBox().inflate(5.0),
				e -> e.getType().equals(EntityType.ENDER_DRAGON) && e.hasCustomName() && e.getCustomName().getString().equals("基夫石像")
		);

		if (!giifuList.isEmpty()) {
			Entity giifu = giifuList.get(0);
			// 在基夫石像的NBT中存储伤害值
			CompoundTag giifuTag = new CompoundTag();
			if (giifu.getPersistentData().contains("Damage")) {
				int damage = giifu.getPersistentData().getInt("Damage") + 1;
				giifu.getPersistentData().putInt("Damage", damage);
			} else {
				giifu.getPersistentData().putInt("Damage", 1);
			}
			if (DEBUG) {
				kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Dealt damage to Giifu statue, current damage count: {}", 
						giifu.getPersistentData().getInt("Damage"));
			}
		}
	}

	// 处理普通敌人的伤害
	private static void handleEnemyDamage(LevelAccessor world, Player entity) {
		// 确保只在服务器端执行伤害逻辑
		if (world.isClientSide()) {
			return;
		}
		
		// 根据头盔计算伤害
		float damage = calculateDamage(entity);

		// 增加爆炸范围到5x5x5，确保有明显的范围伤害效果
		double explosionRadius = 5.0;

		// 找到范围内的敌人（包括其他玩家）
		List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
				LivingEntity.class,
				new AABB(
						entity.getX() - explosionRadius, entity.getY() - explosionRadius, entity.getZ() - explosionRadius,
						entity.getX() + explosionRadius, entity.getY() + explosionRadius, entity.getZ() + explosionRadius
				),
				e -> e != null && e != entity // 只排除自己，不排除其他玩家
			);

		// 调试信息：打印找到的实体数量和类型
		if (DEBUG) {
			kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Found {} nearby entities for player {}", 
					nearbyEntities.size(), entity.getScoreboardName());
			for (LivingEntity enemy : nearbyEntities) {
				kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Nearby entity: {} (type: {}, isPlayer: {})", 
						enemy.getName().getString(), enemy.getType().getDescriptionId(), enemy instanceof Player);
			}
		}

		// 对每个敌人造成伤害
		for (LivingEntity enemy : nearbyEntities) {
			// 计算敌人到爆炸中心的距离
			double distance = enemy.distanceTo(entity);
			// 根据距离计算伤害衰减，距离越远伤害越小
			float distanceFactor = 1.0F - (float) (distance / explosionRadius);
			distanceFactor = Math.max(0.3F, distanceFactor); // 最小保留30%伤害
			float finalDamage = damage * distanceFactor;
			
			damageEnemy(enemy, entity, finalDamage);
		}
	}

	// 根据头盔计算伤害
	private static float calculateDamage(Player entity) {
		ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
		float damage = 40.0F; // 默认伤害值（SIGURD_HELMET）

		// 根据不同的头盔类型设置不同的伤害值
		if (helmet.getItem() == ModItems.BARON_LEMON_HELMET.get()) {
			damage = 50.0F;
		} else if (helmet.getItem() == ModItems.DUKE_HELMET.get()) {
			damage = 70.0F;
		} else if (helmet.getItem() == ModItems.MARIKA_HELMET.get()) {
			damage = 45.0F;
		} else if (helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()) {
			damage = 60.0F;
		} else if (helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()) {
			damage = 55.0F;
		} else if (helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()) {
			damage = 65.0F;
			// 检查是否激活了踢力增强
			if (entity.getPersistentData().getBoolean("DarkGaimKickEnhance")) {
				// 将伤害提升1.3倍
				damage *= 1.3F;
			}
		} else if (helmet.getItem() == ModItems.TYRANT_HELMET.get()) {
			damage = 80.0F;
		} else if (helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()) {
			damage = 55.0F;
		} else if (helmet.getItem() == ModItems.DARK_KIVA_HELMET.get()) {
			damage = 75.0F;
		} else if (helmet.getItem() == ModItems.RIDERNECROM_HELMET.get()) {
			damage = 68.0F;
		} else if (helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get()) {
			damage = 75.0F;
		} else if (helmet.getItem() == ModItems.BRAIN_HELMET.get()) {
			damage = KickDamageHelper.getKickDamage(entity); // 使用Brain头盔的伤害值
		} else if (isDarkGhostHelmet(helmet)) {
			damage = 72.0F; // 黑暗ghost头盔的伤害值
		}

		return damage;
	}

	// 对敌人造成伤害
	private static void damageEnemy(LivingEntity enemy, Player attacker, float damage) {
		// 直接对敌人造成伤害，不检查免疫状态
		// 这样可以确保在多人游戏中骑士踢也能对其他玩家造成伤害
		enemy.hurt(attacker.damageSources().playerAttack(attacker), damage);
		if (DEBUG) {
			kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Damaged entity {} with {} damage",
				enemy.getName().getString(), damage);
		}
	}

	// 处理客户端动画
	private static void handleAnimation(Player entity) {
		// 设置steadily动画
		setPlayerVariable(entity, "steadily", 1);
	}

	/**
	 * 执行自定义的方块破坏逻辑
	 */	
	private static void performCustomBlockDestruction(ServerLevel world, Player player, float radius, float destructionStrength) {
		// 获取爆炸中心点
		BlockPos centerPos = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
		int radiusInt = (int) Math.ceil(radius);

		// 遍历爆炸范围内的所有方块
		for (int x = -radiusInt; x <= radiusInt; x++) {
			for (int y = -radiusInt; y <= radiusInt; y++) {
				for (int z = -radiusInt; z <= radiusInt; z++) {
					BlockPos pos = centerPos.offset(x, y, z);
					BlockState state = world.getBlockState(pos);

					// 计算方块到爆炸中心的距离
					double distance = Math.sqrt(x*x + y*y + z*z);

					// 如果方块在爆炸范围内
					if (distance <= radius) {
						// 检查是否为基夫石像，如果是则跳过
						List<Entity> entitiesAtPos = world.getEntities(
								player, 
								new AABB(pos), 
								e -> KickDamageHelper.isGiifuStatue(e)
						);

						if (entitiesAtPos.isEmpty()) {
							// 计算爆炸对该方块的破坏力量（距离越远力量越小）
							float power = 1.0F - (float) (distance / radius);
							power *= destructionStrength;

							// 获取方块的爆炸抗性
							float resistance = state.getExplosionResistance(world, pos, null);

							// 如果破坏力量大于方块的爆炸抗性，则破坏方块
							if (power > resistance / 5.0F && state.getDestroySpeed(world, pos) >= 0) {
								world.destroyBlock(pos, true);
							}
						}
					}
				}
			}
		}
	}

	// 设置玩家变量
	private static void setPlayerVariable(Player player, String variableName, int value) {
		CompoundTag playerData = player.getPersistentData();
		playerData.putInt(variableName, value);
	}

	// 清理特效实体
	private static void cleanupEffectEntity(Player entity, ServerLevel serverLevel) {
		UUID playerId = entity.getUUID();
		ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);

		// 特别是为EVIL_BATS_HELMET清理特效实体
		if (helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()) {
			// 方式1：通过KicktimeProcedure中的映射表清理
			cleanupEffectEntityFromKicktime(entity);

			// 方式2：直接在世界中查找并清理
			cleanupEffectEntityFromWorld(entity, serverLevel);
		}

		if (DEBUG) {
			kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Cleaned up effect entities for player {}", entity.getScoreboardName());
		}
	}

	// 从KicktimeProcedure的映射表清理实体
	private static void cleanupEffectEntityFromKicktime(Player entity) {
		try {
			// 使用反射访问KicktimeProcedure中的playerToEffectEntity映射
			java.lang.reflect.Field field = KicktimeProcedure.class.getDeclaredField("playerToEffectEntity");
			field.setAccessible(true);
			Map<UUID, Integer> map = (Map<UUID, Integer>) field.get(null);

			if (map.containsKey(entity.getUUID())) {
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Removing effect entity from KicktimeProcedure map for player {}", 
						entity.getScoreboardName());
				}
				map.remove(entity.getUUID());
			}
		} catch (Exception e) {
			// 如果反射失败，记录错误但继续执行
			if (DEBUG) {
				kamen_rider_boss_you_and_me.LOGGER.error("KickendProcedure: Error accessing KicktimeProcedure's playerToEffectEntity", e);
			}
		}
	}

	// 从世界中直接清理实体
	private static void cleanupEffectEntityFromWorld(Player entity, ServerLevel serverLevel) {
		// 查找并移除所有与该玩家关联的BatStampFinishEntity
		List<BatStampFinishEntity> batEntities = serverLevel.getEntitiesOfClass(
				BatStampFinishEntity.class,
				entity.getBoundingBox().inflate(10.0)
		);

		for (BatStampFinishEntity batEntity : batEntities) {
			// 直接调用getTargetPlayerId方法，不再使用反射
			UUID targetId = batEntity.getTargetPlayerId();

			if (targetId != null && targetId.equals(entity.getUUID())) {
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Discarding BatStampFinishEntity with ID: {}", 
							batEntity.getId());
				}
				batEntity.discard();
			}
		}

		// 查找并移除所有与该玩家关联的NecromEyexEntity
		List<NecromEyexEntity> necromEntities = serverLevel.getEntitiesOfClass(
				NecromEyexEntity.class,
				entity.getBoundingBox().inflate(10.0)
		);

		for (NecromEyexEntity necromEntity : necromEntities) {
			// 直接调用getTargetPlayerId方法
			UUID targetId = necromEntity.getTargetPlayerId();

			if (targetId != null && targetId.equals(entity.getUUID())) {
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Discarding NecromEyexEntity with ID: {}", 
							necromEntity.getId());
				}
				necromEntity.discard();
			}
		}
		
		// 查找并移除所有与该玩家关联的DarkGhostRiderKickEntity
		List<DarkGhostRiderKickEntity> darkGhostEntities = serverLevel.getEntitiesOfClass(
				DarkGhostRiderKickEntity.class,
				entity.getBoundingBox().inflate(10.0)
		);

		for (DarkGhostRiderKickEntity darkGhostEntity : darkGhostEntities) {
			// 直接调用getTargetPlayerId方法
			UUID targetId = darkGhostEntity.getTargetPlayerId();

			if (targetId != null && targetId.equals(entity.getUUID())) {
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Discarding DarkGhostRiderKickEntity with ID: {}", 
							darkGhostEntity.getId());
				}
				darkGhostEntity.discard();
			}
		}
	}
}