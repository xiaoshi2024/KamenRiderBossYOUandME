package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatStampFinishEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    private static final boolean DEBUG = true;

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
		if (variables.kcik && isHelmetValid(helmet) && entity.onGround()) {
			// 玩家落地，处理踢击结束逻辑
			handleKickEnd(event, world, entity);
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
		variables.kcik = false;

		// 服务器端处理爆炸效果和伤害
		if (!world.isClientSide()) {
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

			// 专门处理基夫石像的伤害
			handleGiifuDamage(world, entity);

			// 处理普通敌人的伤害
			handleEnemyDamage(world, entity);

			// 清理特效实体
			cleanupEffectEntity(entity, serverLevel);
		}

		// 客户端处理动画
		if (world.isClientSide()) {
			handleAnimation(entity);
		}

		// 设置无敌状态
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
			helmet.getItem() == ModItems.DARK_KIVA_HELMET.get();
	}

	// 处理无敌状态的重置
	private static void handleInvulnerabilityReset(LevelAccessor world, Player entity, KRBVariables.PlayerVariables variables) {
		// 如果处于无敌状态且需要爆炸
		if (variables.wudi && variables.needExplode) {
			// 只在服务器端执行延迟任务
			if (!world.isClientSide() && entity.getServer() != null) {
				// 创建一个延迟任务来重置无敌状态
				entity.getServer().execute(() -> {
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
		// 根据头盔计算伤害
		float damage = calculateDamage(entity);

		// 找到范围内的敌人
		List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
				LivingEntity.class,
				new AABB(
						entity.getX() - 3.0, entity.getY() - 3.0, entity.getZ() - 3.0,
						entity.getX() + 3.0, entity.getY() + 3.0, entity.getZ() + 3.0
				),
				e -> e != entity && !e.isAlliedTo(entity)
		);

		// 对每个敌人造成伤害
		for (LivingEntity enemy : nearbyEntities) {
			damageEnemy(enemy, entity, damage);
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
		}

		return damage;
	}

	// 对敌人造成伤害
	private static void damageEnemy(LivingEntity enemy, Player attacker, float damage) {
		if (!enemy.isInvulnerableTo(attacker.damageSources().playerAttack(attacker))) {
			enemy.hurt(attacker.damageSources().playerAttack(attacker), damage);
			if (DEBUG) {
				kamen_rider_boss_you_and_me.LOGGER.info("KickendProcedure: Damaged entity {} with {} damage",
						enemy.getName().getString(), damage);
			}
		}
	}

	// 处理客户端动画
	private static void handleAnimation(Player entity) {
		// 设置steadily动画
		setPlayerVariable(entity, "steadily", 1);
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
		List<BatStampFinishEntity> entities = serverLevel.getEntitiesOfClass(
				BatStampFinishEntity.class,
				entity.getBoundingBox().inflate(10.0)
		);

		for (BatStampFinishEntity batEntity : entities) {
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
	}
}