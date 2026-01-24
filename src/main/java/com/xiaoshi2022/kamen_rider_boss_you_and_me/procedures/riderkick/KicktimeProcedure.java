package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatStampFinishEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkGhostRiderKickEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NecromEyexEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ParticleTypesRegistry;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KickDamageHelper;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public class KicktimeProcedure {

	// 添加一个静态变量来跟踪每个玩家的特效实体
	private static final Map<UUID, Integer> playerToEffectEntity = new HashMap<>();
	private static final boolean DEBUG = false;

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player.level(), event.player);
		}
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null) 
			return;

		// 检查玩家是否在进行骑士踢
		if (entity instanceof Player player && isRiderKicking(player)) {
			// 记录当前使用的头盔类型
			ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
			if (DEBUG && !world.isClientSide()) {
				kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Player {} is kicking with helmet: {}", 
					player.getScoreboardName(), helmet.getHoverName().getString());
			}
			
			// 新增：检查是否已经存在特效实体，如果有，并且当前头盔类型与实体不匹配，则先清理旧实体
			UUID playerId = player.getUUID();
			if (playerToEffectEntity.containsKey(playerId) && world instanceof ServerLevel serverLevel) {
				int entityId = playerToEffectEntity.get(playerId);
				Entity existingEntity = serverLevel.getEntity(entityId);
				boolean entityTypeMatches = false;
				
				if (existingEntity != null) {
					// 根据头盔类型检查实体类型是否匹配
					if (helmet.getItem() == ModItems.EVIL_BATS_HELMET.get() && existingEntity instanceof BatStampFinishEntity) {
						entityTypeMatches = true;
					} else if (helmet.getItem() == ModItems.RIDERNECROM_HELMET.get() && existingEntity instanceof NecromEyexEntity) {
						entityTypeMatches = true;
					} else if (helmet.getItem() == ModItems.DARK_RIDER_HELMET.get() && existingEntity instanceof DarkGhostRiderKickEntity) {
						entityTypeMatches = true;
					} else if (helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get() && existingEntity instanceof DarkGhostRiderKickEntity) {
						entityTypeMatches = true;
					}
				}
				
				if (!entityTypeMatches) {
					// 类型不匹配，清理旧实体
					if (DEBUG && existingEntity != null) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Helmet type changed, cleaning up mismatched entity for player {}", 
							player.getScoreboardName());
					}
					cleanupEffectEntity(entity);
				}
			}

			// 根据头盔类型执行不同的特效
			if (helmet.getItem() == ModItems.BARON_LEMON_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
			} else if (helmet.getItem() == ModItems.MARIKA_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.PEACHSLICE.get());
			}else if (helmet.getItem() == ModItems.DARK_RIDER_HELMET.get()) {
				// 处理 DARK_RIDER_HELMET 的特效和移动
				if (DEBUG && !world.isClientSide()) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: DARK_RIDER_HELMET detected on player {}", player.getScoreboardName());
				}
				handleDarkGhostKick(world, entity);

			} else if (helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.MLONSLICE.get());
			} else if (helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
			} else if (helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
			} else if (helmet.getItem() == ModItems.TYRANT_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.DRAGONLICE.get());
			} else if (helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()) {
				// 处理 EVIL_BATS_HELMET 的特效和移动
				if (DEBUG && !world.isClientSide()) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: EVIL_BATS_HELMET detected on player {}", player.getScoreboardName());
				}
				handleEvilBatsKick(world, entity);
			} else if (helmet.getItem() == ModItems.DARK_KIVA_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.DARK_BAT.get());
			} else if (helmet.getItem() == ModItems.RIDERNECROM_HELMET.get()) {
				// 处理 RIDERNECROM_HELMET 的特效和移动
				if (DEBUG && !world.isClientSide()) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: RIDERNECROM_HELMET detected on player {}", player.getScoreboardName());
				}
				handleRidernecromKick(world, entity);
			} else if (helmet.getItem() == ModItems.DUKE_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
			} else if (helmet.getItem() == ModItems.SIGURD_HELMET.get()) {
				handleKick(world, entity, ParticleTypesRegistry.CHERRYSLICE.get());
			} else if (helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get()) {
				// 处理 NAPOLEON_GHOST_HELMET 的特效和移动
				if (DEBUG && !world.isClientSide()) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: NAPOLEON_GHOST_HELMET detected on player {}", player.getScoreboardName());
				}
				handleNapoleonGhostKick(world, entity);
			} else if (helmet.getItem() == ModItems.BRAIN_HELMET.get()) {
					// Brain骑士踢：无特效，只有抛物线移动
					if (DEBUG && !world.isClientSide()) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: BRAIN_HELMET detected on player {}", player.getScoreboardName());
					}
					
					// 检查玩家是否落地，如果落地则重置踢击状态
					// 但只有当玩家已经在空中过（有垂直速度）时才重置
					if (player.onGround() && Math.abs(entity.getDeltaMovement().y) < 0.1) {
						resetKickStatus(player);
						return;
					}
					
					// 只处理动画和抛物线移动，不添加粒子特效
					handleAnimation(world, entity);
					syncAnimation(world, entity);
					// 使用EVIL_BATS的抛物线移动逻辑，因为它提供了平滑的抛物线效果
					handleEvilBatsParabolicMovement(world, entity);
				}
		} else {
			// 如果不在踢击状态，清理特效实体
			cleanupEffectEntity(entity);
		}
	}

	// 处理 RIDERNECROM_HELMET 的骑士踢特效
	private static void handleRidernecromKick(LevelAccessor world, Entity entity) {
		if (world instanceof ServerLevel serverLevel && entity instanceof Player player) {
			// 检查玩家是否落地，如果落地则重置踢击状态
			// 但只有当玩家已经在空中过（有垂直速度）时才重置
			if (player.onGround() && Math.abs(entity.getDeltaMovement().y) < 0.1) {
				resetKickStatus(player);
				return;
			}

			UUID playerId = player.getUUID();

			// 检查是否已经为该玩家创建了特效实体
			if (!playerToEffectEntity.containsKey(playerId)) {
				// 调试日志：开始创建实体
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Creating NecromEyexEntity for player {}", player.getScoreboardName());
				}
				
				try {
					// 创建 NecromEyexEntity 实体
					NecromEyexEntity necromEyexEntity = new NecromEyexEntity(ModEntityTypes.NECROM_EYEX.get(), serverLevel);
					
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: NecromEyexEntity created with ID: {}", necromEyexEntity.getId());
					}
					
					necromEyexEntity.setTargetPlayer(player);
					necromEyexEntity.startFinish(); // 开始播放动画
					serverLevel.addFreshEntity(necromEyexEntity);

					// 存储实体ID
					playerToEffectEntity.put(playerId, necromEyexEntity.getId());
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: NecromEyexEntity added to world and tracked for player {}", player.getScoreboardName());
					}
				} catch (Exception e) {
					// 捕获并记录任何创建实体时的错误
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.error("KicktimeProcedure: Error creating NecromEyexEntity for player {}", player.getScoreboardName(), e);
					}
				}
			} else {
				// 如果实体已存在，检查是否还存活
				int entityId = playerToEffectEntity.get(playerId);
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Checking existing NecromEyexEntity with ID: {} for player {}", entityId, player.getScoreboardName());
				}
				Entity existingEntity = serverLevel.getEntity(entityId);
				if (existingEntity == null || !existingEntity.isAlive()) {
					// 实体已消失，重新创建
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing NecromEyexEntity not found or not alive, recreating for player {}", player.getScoreboardName());
					}
					playerToEffectEntity.remove(playerId);
					handleRidernecromKick(world, entity); // 递归调用重新创建
					return;
				} else if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing NecromEyexEntity with ID: {} is alive for player {}", entityId, player.getScoreboardName());
				}
			}

			// 确保玩家踢击动画正常播放
			handleAnimation(world, entity);
			syncAnimation(world, entity);
		}

		// 单独处理 RIDERNECROM_HELMET 的抛物线移动
		if (DEBUG && !world.isClientSide() && entity instanceof Player) {
			kamen_rider_boss_you_and_me.LOGGER.debug("KicktimeProcedure: Handling RIDERNECROM_HELMET parabolic movement for player {}", ((Player)entity).getScoreboardName());
		}
		handleEvilBatsParabolicMovement(world, entity);
	}

	// 处理 EVIL_BATS_HELMET 的骑士踢特效
	private static void handleEvilBatsKick(LevelAccessor world, Entity entity) {
		if (world instanceof ServerLevel serverLevel && entity instanceof Player player) {
			// 检查玩家是否落地，如果落地则重置踢击状态
			// 但只有当玩家已经在空中过（有垂直速度）时才重置
			if (player.onGround() && Math.abs(entity.getDeltaMovement().y) < 0.1) {
				resetKickStatus(player);
				return;
			}

			UUID playerId = player.getUUID();

			// 检查是否已经为该玩家创建了特效实体
			if (!playerToEffectEntity.containsKey(playerId)) {
				// 调试日志：开始创建实体
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Creating BatStampFinishEntity for player {}", player.getScoreboardName());
				}
				
				try {
					// 创建 BatStampFinishEntity 实体
					BatStampFinishEntity batStampFinishEntity = new BatStampFinishEntity(ModEntityTypes.BAT_STAMP_FINISH.get(), serverLevel);
					
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: BatStampFinishEntity created with ID: {}", batStampFinishEntity.getId());
					}
					
					batStampFinishEntity.setTargetPlayer(player);
					batStampFinishEntity.startFinish(); // 开始播放 skick 动画
					serverLevel.addFreshEntity(batStampFinishEntity);

					// 存储实体ID
					playerToEffectEntity.put(playerId, batStampFinishEntity.getId());
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: BatStampFinishEntity added to world and tracked for player {}", player.getScoreboardName());
					}
				} catch (Exception e) {
					// 捕获并记录任何创建实体时的错误
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.error("KicktimeProcedure: Error creating BatStampFinishEntity for player {}", player.getScoreboardName(), e);
					}
				}
			} else {
				// 如果实体已存在，检查是否还存活
				int entityId = playerToEffectEntity.get(playerId);
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Checking existing BatStampFinishEntity with ID: {} for player {}", entityId, player.getScoreboardName());
				}
				Entity existingEntity = serverLevel.getEntity(entityId);
				if (existingEntity == null || !existingEntity.isAlive()) {
					// 实体已消失，重新创建
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing BatStampFinishEntity not found or not alive, recreating for player {}", player.getScoreboardName());
					}
					playerToEffectEntity.remove(playerId);
					handleEvilBatsKick(world, entity); // 递归调用重新创建
					return;
				} else if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing BatStampFinishEntity with ID: {} is alive for player {}", entityId, player.getScoreboardName());
				}
			}

			// 确保玩家踢击动画正常播放
			handleAnimation(world, entity);
			syncAnimation(world, entity);
		}

		// 单独处理 EVIL_BATS_HELMET 的抛物线移动
		if (DEBUG && !world.isClientSide() && entity instanceof Player) {
			kamen_rider_boss_you_and_me.LOGGER.debug("KicktimeProcedure: Handling EVIL_BATS_HELMET parabolic movement for player {}", ((Player)entity).getScoreboardName());
		}
		handleEvilBatsParabolicMovement(world, entity);
	}

	// 处理 DARK_RIDER_HELMET 的骑士踢特效
	private static void handleDarkGhostKick(LevelAccessor world, Entity entity) {
		if (world instanceof ServerLevel serverLevel && entity instanceof Player player) {
			// 检查玩家是否落地，如果落地则重置踢击状态
			// 但只有当玩家已经在空中过（有垂直速度）时才重置
			if (player.onGround() && Math.abs(entity.getDeltaMovement().y) < 0.1) {
				resetKickStatus(player);
				return;
			}

			UUID playerId = player.getUUID();

			// 检查是否已经为该玩家创建了特效实体
			if (!playerToEffectEntity.containsKey(playerId)) {
				// 调试日志：开始创建实体
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Creating DarkGhostRiderKickEntity for player {}", player.getScoreboardName());
				}
				
				try {
					// 创建 DarkGhostRiderKickEntity 实体
						DarkGhostRiderKickEntity darkGhostRiderKickEntity = new DarkGhostRiderKickEntity(ModEntityTypes.DARKGHOST_RIDER_KICK.get(), serverLevel);
					
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: DarkGhostRiderKickEntity created with ID: {}", darkGhostRiderKickEntity.getId());
					}
					
					darkGhostRiderKickEntity.setTargetPlayer(player);
					darkGhostRiderKickEntity.startFinish(); // 开始播放动画
					serverLevel.addFreshEntity(darkGhostRiderKickEntity);

					// 存储实体ID
					playerToEffectEntity.put(playerId, darkGhostRiderKickEntity.getId());
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: DarkGhostRiderKickEntity added to world and tracked for player {}", player.getScoreboardName());
					}
				} catch (Exception e) {
					// 捕获并记录任何创建实体时的错误
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.error("KicktimeProcedure: Error creating DarkGhostRiderKickEntity for player {}", player.getScoreboardName(), e);
					}
				}
			} else {
				// 如果实体已存在，检查是否还存活
				int entityId = playerToEffectEntity.get(playerId);
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Checking existing DarkGhostRiderKickEntity with ID: {} for player {}", entityId, player.getScoreboardName());
				}
				Entity existingEntity = serverLevel.getEntity(entityId);
				if (existingEntity == null || !existingEntity.isAlive()) {
					// 实体已消失，重新创建
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing DarkGhostRiderKickEntity not found or not alive, recreating for player {}", player.getScoreboardName());
					}
					playerToEffectEntity.remove(playerId);
					handleDarkGhostKick(world, entity); // 递归调用重新创建
					return;
				} else if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing DarkGhostRiderKickEntity with ID: {} is alive for player {}", entityId, player.getScoreboardName());
				}
			}

			// 确保玩家踢击动画正常播放
			handleAnimation(world, entity);
			syncAnimation(world, entity);
		}

		// 单独处理 DARK_RIDER_HELMET 的抛物线移动
		if (DEBUG && !world.isClientSide() && entity instanceof Player) {
			kamen_rider_boss_you_and_me.LOGGER.debug("KicktimeProcedure: Handling DARK_RIDER_HELMET parabolic movement for player {}", ((Player)entity).getScoreboardName());
		}
		handleEvilBatsParabolicMovement(world, entity);
	}

	// 新增：专门处理 EVIL_BATS_HELMET 的抛物线移动
	private static void handleEvilBatsParabolicMovement(LevelAccessor world, Entity entity) {
		// 设置移动速度 - 抛物线实现
		Vec3 look = entity.getLookAngle();

		// 水平速度
		double horizontalSpeed = 1.5;
		double horizontalX = look.x * horizontalSpeed;
		double horizontalZ = look.z * horizontalSpeed;

		// 获取当前垂直速度并应用重力
		double currentY = entity.getDeltaMovement().y;
		double newY;

		// 获取踢击状态
		KRBVariables.PlayerVariables variables = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		if (variables.kickStartTime == 0L) {
				// 第一次执行踢击，初始化
				variables.kickStartTime = System.currentTimeMillis();
				// 保持当前的向上速度，不要重置为固定值
				// 这样就能保留KkcikAnXiaAnJianShiProcedure中设置的较大速度
				newY = currentY;
			} else {
				// 简单的重力模拟：每tick减少速度
				newY = currentY - 0.08;
			}

		// 限制最大下落速度
		if (newY < -1.0) {
			newY = -1.0;
		}

		// 同步变量
		variables.syncPlayerVariables(entity);

		// 应用抛物线移动
		entity.setDeltaMovement(new Vec3(
				horizontalX,
				newY,
				horizontalZ
		));
	}

	// 清理特效实体
	private static void cleanupEffectEntity(Entity entity) {
		if (entity instanceof Player player) {
			UUID playerId = player.getUUID();
			if (playerToEffectEntity.containsKey(playerId)) {
				// 从世界中移除实体
				if (player.level() instanceof ServerLevel serverLevel) {
					int entityId = playerToEffectEntity.get(playerId);
					Entity effectEntity = serverLevel.getEntity(entityId);
					if (effectEntity != null) {
						if (DEBUG) {
							// 改进：根据实体类型输出正确的日志信息
							String entityType = effectEntity instanceof BatStampFinishEntity ? "BatStampFinishEntity" : 
							                   effectEntity instanceof NecromEyexEntity ? "NecromEyexEntity" : 
							                   effectEntity instanceof DarkGhostRiderKickEntity ? "DarkGhostRiderKickEntity" : "UnknownEntity";
							kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Discarding {} with ID: {} for player {}", 
							        entityType, entityId, player.getScoreboardName());
						}
						effectEntity.discard();
					}
				}
				playerToEffectEntity.remove(playerId);
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Removed special effect entity tracking for player {}", player.getScoreboardName());
				}
			}
		}
	}

	// 新增：重置踢击状态（修改这个方法）
	private static void resetKickStatus(Player player) {
		KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		variables.kick = false;
		variables.kickStartTime = 0L;
		variables.lastKickTime = System.currentTimeMillis();
		variables.syncPlayerVariables(player);

		// 清理特效实体
		cleanupEffectEntity(player);
		
		if (DEBUG) {
			kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Reset kick status for player {}", player.getScoreboardName());
		}
	}

	// 判断玩家是否正在进行骑士踢
	public static boolean isRiderKicking(Player player) {
		if (player == null) return false;
		// 获取玩家的头盔
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		// 检查是否佩戴了特定的骑士头盔
		boolean hasSpecialHelmet = helmet.getItem() == ModItems.BARON_LEMON_HELMET.get()
				|| helmet.getItem() == ModItems.DUKE_HELMET.get()
				|| helmet.getItem() == ModItems.SIGURD_HELMET.get()
				|| helmet.getItem() == ModItems.MARIKA_HELMET.get()
				|| helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()
				|| helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()
				|| helmet.getItem() == ModItems.TYRANT_HELMET.get()
				|| helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()
				|| helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()
				|| helmet.getItem() == ModItems.DARK_KIVA_HELMET.get()
				|| helmet.getItem() == ModItems.DARK_RIDER_HELMET.get()
				|| helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get()
				|| helmet.getItem() == ModItems.RIDERNECROM_HELMET.get()
				|| helmet.getItem() == ModItems.BRAIN_HELMET.get();

		// 获取玩家的kick状态
		boolean isKicking = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables()).kick;

		return hasSpecialHelmet && isKicking;
	}

	// 检查是否正在踢击
	private static boolean isKicking(Entity entity) {
		return (entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables())).kick;
	}

	// 获取头盔
	private static ItemStack getHelmet(Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		}
		return ItemStack.EMPTY;
	}

	// 处理踢击逻辑
	private static void handleKick(LevelAccessor world, Entity entity, ParticleOptions particleOptions) {
		// 检查玩家是否落地，如果落地则重置踢击状态
		// 但只有当玩家已经在空中过（有垂直速度）时才重置
		if (entity instanceof Player player && player.onGround() && Math.abs(entity.getDeltaMovement().y) < 0.1) {
			resetKickStatus(player);
			return;
		}

		// 设置移动速度 - 更简单的抛物线实现
		Vec3 look = entity.getLookAngle();

		// 水平速度
		double horizontalSpeed = 1.5;
		double horizontalX = look.x * horizontalSpeed;
		double horizontalZ = look.z * horizontalSpeed;

		// 获取当前垂直速度并应用重力
		double currentY = entity.getDeltaMovement().y;
		double newY;

		// 获取踢击状态
		KRBVariables.PlayerVariables variables = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		if (variables.kickStartTime == 0L) {
				// 第一次执行踢击，初始化
				variables.kickStartTime = System.currentTimeMillis();
				// 保持当前的向上速度，不要重置为固定值
				// 这样就能保留KkcikAnXiaAnJianShiProcedure中设置的较大速度
				newY = currentY;
			} else {
				// 简单的重力模拟：每tick减少速度
				newY = currentY - 0.08;
			}

		// 限制最大下落速度
		if (newY < -1.0) {
			newY = -1.0;
		}

		// 同步变量
		variables.syncPlayerVariables(entity);

		entity.setDeltaMovement(new Vec3(
				horizontalX,
				newY,
				horizontalZ
		));

		// 处理动画和粒子效果
		handleAnimation(world, entity);

		if (world instanceof ServerLevel srvLvl) {
			Vec3 looks = entity.getLookAngle();
			srvLvl.sendParticles(
					particleOptions,
					entity.getX(),
					entity.getY() + 0.9,
					entity.getZ(),
					4,
					looks.x * 0.1,
					looks.y * 0.1,
					looks.z * 0.1,
					0
			);
		}

		syncAnimation(world, entity);

		// 新增：检测附近是否有其他正在踢击的玩家，实现对踢功能
		checkAndHandleDualKick(world, entity);
	}

	// 新增：检测并处理对踢
	private static void checkAndHandleDualKick(LevelAccessor world, Entity kicker) {
		if (!(world instanceof ServerLevel serverLevel) || !(kicker instanceof Player)) {
			return;
		}

		// 获取踢者的变量
		KRBVariables.PlayerVariables kickerVars = kicker.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		// 检查踢者是否在空中
		if (kicker.onGround()) {
			return;
		}

		// 扫描附近的实体
		double scanRadius = 3.0; // 检测半径
		List<Entity> nearbyEntities = serverLevel.getEntities(
				kicker,
				kicker.getBoundingBox().inflate(scanRadius),
				e -> e instanceof Player && e != kicker && !isKickingEntityOnCooldown((Player)e)
		);

		for (Entity nearbyEntity : nearbyEntities) {
			if (nearbyEntity instanceof Player targetPlayer && isKicking(nearbyEntity)) {
				// 两个玩家都在空中进行骑士踢，可以触发对踢
				handleDualKickDamage(serverLevel, (Player)kicker, targetPlayer);
				break; // 一次只处理一个对踢目标
			}
		}
	}

	// 新增：检查玩家是否在对踢冷却中
	private static boolean isKickingEntityOnCooldown(Player player) {
		KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());

		long currentTime = System.currentTimeMillis();
		return currentTime - variables.lastKickTime < 2000; // 2秒冷却时间
	}

	// 新增：处理对踢伤害
	private static void handleDualKickDamage(ServerLevel world, Player kicker, Player target) {
		// 获取双方的踢击伤害
		float kickerDamage = KickDamageHelper.getKickDamage(kicker);
		float targetDamage = KickDamageHelper.getKickDamage(target);

		// 对踢时，双方受到对方的踢击伤害
		kicker.hurt(kicker.damageSources().playerAttack(target), targetDamage);
		target.hurt(target.damageSources().playerAttack(kicker), kickerDamage);

		// 播放对踢特效和声音
		world.playSound(null, kicker.getX(), kicker.getY(), kicker.getZ(),
				net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

		// 创建爆炸粒子效果
		// 去掉 instanceof，直接强制转换
		ServerLevel serverLevel = (ServerLevel) world;
		serverLevel.sendParticles(
				ParticleTypes.EXPLOSION,
				(kicker.getX() + target.getX()) / 2,
				(kicker.getY() + target.getY()) / 2,
				(kicker.getZ() + target.getZ()) / 2,
				10, 0.5, 0.5, 0.5, 0.1
		);

		// 重置双方的踢击状态
		resetKickStatus(kicker);
		resetKickStatus(target);
	}


	// 处理动画
	private static void handleAnimation(LevelAccessor world, Entity entity) {
		if (world.isClientSide()) {
			if (entity instanceof AbstractClientPlayer player) {
				var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player)
						.get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
				if (animation != null && !animation.isActive()) {
					animation.setAnimation(new KeyframeAnimationPlayer(
							PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", "kick"))));
				}
			}
		}
	}

	// 同步动画到客户端
	private static void syncAnimation(LevelAccessor world, Entity entity) {
		if (!world.isClientSide()) {
			if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
				List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
				synchronized (connections) {
					Iterator<Connection> iterator = connections.iterator();
					while (iterator.hasNext()) {
						Connection connection = iterator.next();
						if (!connection.isConnecting() && connection.isConnected()) {
							kamen_rider_boss_you_and_me.PACKET_HANDLER.sendTo(
									new SetupAnimationsProcedure.kamen_rider_boss_you_and_meModAnimationMessage(
											Component.literal("kick"), entity.getId(), false),
									connection, NetworkDirection.PLAY_TO_CLIENT);
						}
					}
				}
			}
		}
	}

	// 新增：检查玩家是否可以执行踢击（防止重复触发）
	public static boolean canPerformKick(Entity entity) {
		if (entity == null) return false;

		// 检查是否已经在踢击状态
		if (isKicking(entity)) {
			return false;
		}

		// 检查冷却时间（在玩家变量中存储）
		long currentTime = System.currentTimeMillis();
		long lastKickTime = entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables()).lastKickTime;

		// 40秒冷却时间【骑士踢cd】
        if (currentTime - lastKickTime < 40000) {
			return false;
		}

		// 检查是否在地面上（只能在陆地上起跳）
		if (!entity.onGround()) {
			return false;
		}

		// 检查玩家是否被Baron的技能控制（高等级减速效果）
		if (entity instanceof LivingEntity livingEntity) {
			if (livingEntity.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) && 
					livingEntity.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() >= 250) {
				// 向玩家发送提示信息
				if (entity instanceof Player player) {
					player.displayClientMessage(net.minecraft.network.chat.Component.literal("目前没有办法破开这个被控"), true);
				}
				return false;
			}
		}

		// 检查是否有合适的头盔
		ItemStack helmet = getHelmet(entity);
		return helmet.getItem() == ModItems.SIGURD_HELMET.get() ||
				helmet.getItem() == ModItems.BARON_LEMON_HELMET.get() ||
				helmet.getItem() == ModItems.DUKE_HELMET.get() ||
				helmet.getItem() == ModItems.MARIKA_HELMET.get() ||
				helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()||
				helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()||
				helmet.getItem() == ModItems.TYRANT_HELMET.get() ||
				helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() ||
				helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()||
				helmet.getItem() == ModItems.DARK_RIDER_HELMET.get()||
				helmet.getItem() == ModItems.DARK_KIVA_HELMET.get() || // 新增：黑暗Kiva头盔
				helmet.getItem() == ModItems.RIDERNECROM_HELMET.get() || // 新增：RiderNecrom头盔
				helmet.getItem() == ModItems.NAPOLEON_GHOST_HELMET.get() || // 新增：拿破仑幽灵头盔
				helmet.getItem() == ModItems.BRAIN_HELMET.get(); // 新增：Brain头盔
	}

	// 处理 NAPOLEON_GHOST_HELMET 的特效和移动
	private static void handleNapoleonGhostKick(LevelAccessor world, Entity entity) {
		if (world instanceof ServerLevel serverLevel && entity instanceof Player player) {
			// 检查玩家是否落地，如果落地则重置踢击状态
			// 但只有当玩家已经在空中过（有垂直速度）时才重置
			if (player.onGround() && Math.abs(entity.getDeltaMovement().y) < 0.1) {
				resetKickStatus(player);
				return;
			}

			UUID playerId = player.getUUID();

			// 检查是否已经为该玩家创建了特效实体
			if (!playerToEffectEntity.containsKey(playerId)) {
				// 调试日志：开始创建实体
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Creating DarkGhostRiderKickEntity for player {}", player.getScoreboardName());
				}
				
				try {
					// 创建 DarkGhostRiderKickEntity 实体
					DarkGhostRiderKickEntity darkGhostRiderKickEntity = new DarkGhostRiderKickEntity(ModEntityTypes.DARKGHOST_RIDER_KICK.get(), serverLevel);
					
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: DarkGhostRiderKickEntity created with ID: {}", darkGhostRiderKickEntity.getId());
					}
					
					darkGhostRiderKickEntity.setTargetPlayer(player);
					darkGhostRiderKickEntity.startFinish(); // 开始播放动画
					serverLevel.addFreshEntity(darkGhostRiderKickEntity);

					// 存储实体ID
					playerToEffectEntity.put(playerId, darkGhostRiderKickEntity.getId());
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: DarkGhostRiderKickEntity added to world and tracked for player {}", player.getScoreboardName());
					}
				} catch (Exception e) {
					// 捕获并记录任何创建实体时的错误
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.error("KicktimeProcedure: Error creating DarkGhostRiderKickEntity for player {}", player.getScoreboardName(), e);
					}
				}
			} else {
				// 如果实体已存在，检查是否还存活
				int entityId = playerToEffectEntity.get(playerId);
				if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Checking existing DarkGhostRiderKickEntity with ID: {} for player {}", entityId, player.getScoreboardName());
				}
				Entity existingEntity = serverLevel.getEntity(entityId);
				if (existingEntity == null || !existingEntity.isAlive()) {
					// 实体已消失，重新创建
					if (DEBUG) {
						kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing DarkGhostRiderKickEntity not found or not alive, recreating for player {}", player.getScoreboardName());
					}
					playerToEffectEntity.remove(playerId);
					handleNapoleonGhostKick(world, entity); // 递归调用重新创建
					return;
				} else if (DEBUG) {
					kamen_rider_boss_you_and_me.LOGGER.info("KicktimeProcedure: Existing DarkGhostRiderKickEntity with ID: {} is alive for player {}", entityId, player.getScoreboardName());
				}
			}

			// 确保玩家踢击动画正常播放
			handleAnimation(world, entity);
			syncAnimation(world, entity);
		}

		// 单独处理 NAPOLEON_GHOST_HELMET 的抛物线移动
		if (DEBUG && !world.isClientSide() && entity instanceof Player) {
			kamen_rider_boss_you_and_me.LOGGER.debug("KicktimeProcedure: Handling NAPOLEON_GHOST_HELMET parabolic movement for player {}", ((Player)entity).getScoreboardName());
		}
		handleEvilBatsParabolicMovement(world, entity);
	}
}