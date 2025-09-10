package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ParticleTypesRegistry;
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
import java.util.Iterator;
import java.util.List;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KickDamageHelper;

@Mod.EventBusSubscriber
public class KicktimeProcedure {
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

		// 检查是否正在踢击
		if (!isKicking(entity)) return;

		// 检查头盔类型并处理相应的踢击逻辑
		ItemStack helmet = getHelmet(entity);
		if (helmet.getItem() == ModItems.SIGURD_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.CHERRYSLICE.get());
		} else if (helmet.getItem() == ModItems.BARON_LEMON_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
		} else if (helmet.getItem() == ModItems.DUKE_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
		} else if (helmet.getItem() == ModItems.MARIKA_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.PEACHSLICE.get());
		} else if (helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.MLONSLICE.get());
		} else if (helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
		} else if (helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.LEMONSLICE.get());
		} else if (helmet.getItem() == ModItems.TYRANT_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.DRAGONLICE.get());
		} else if (helmet.getItem() == ModItems.EVIL_BATS_HELMET.get()) {
			handleKick(world, entity, ParticleTypesRegistry.DRAGONLICE.get());
		} else if (helmet.getItem() == ModItems.DARK_KIVA_HELMET.get()) {
			// 新增：处理黑暗Kiva骑士踢，使用dark_bat粒子
			handleKick(world, entity, ParticleTypesRegistry.DARK_BAT.get());
		}
	}

	// 检查是否正在踢击
	private static boolean isKicking(Entity entity) {
		return (entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables())).kcik
				&& !entity.onGround();
	}

	// 获取头盔
	private static ItemStack getHelmet(Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		}
		return ItemStack.EMPTY;
	}

	// 处理踢击逻辑
	// 处理踢击逻辑
	private static void handleKick(LevelAccessor world, Entity entity, ParticleOptions particleOptions) {
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
			newY = 0.4; // 初始向上速度
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
	
	// 新增：重置踢击状态
	private static void resetKickStatus(Player player) {
		KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());
		
		variables.kcik = false;
		variables.kickStartTime = 0L;
		variables.lastKickTime = System.currentTimeMillis();
		variables.syncPlayerVariables(player);
		
		// 停止踢击动画
		if (player.level().isClientSide() && player instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer) {
			var animation = (dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation>) 
					dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess.getPlayerAssociatedData(clientPlayer)
					.get(new net.minecraft.resources.ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
			if (animation != null) {
				animation.setAnimation(null);
			}
		}
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
				helmet.getItem() == ModItems.DARK_KIVA_HELMET.get(); // 新增：黑暗Kiva头盔
	}
}