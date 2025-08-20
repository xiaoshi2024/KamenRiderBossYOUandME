package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick.SetupAnimationsProcedure;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber
public class KickendProcedure {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
		}
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;

		// 检查所有符合条件的头盔
		if (isValidHelmet(entity) &&
				(entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables())).kcik == true &&
				entity.onGround()) {

			handleKickEnd(world, x, y, z, entity);
		}
	}

	// 提取头盔检测逻辑到单独的方法
	private static boolean isValidHelmet(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) return false;

		ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		return helmet.getItem() == ModItems.BARON_LEMON_HELMET.get() ||
				helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() ||
				helmet.getItem() == ModItems.MARIKA_HELMET.get() ||
				helmet.getItem() == ModItems.DUKE_HELMET.get() ||
				helmet.getItem() == ModItems.SIGURD_HELMET.get();
	}

	private static void handleKickEnd(LevelAccessor world, double x, double y, double z, Entity entity) {
		// 设置需要爆炸的标志
		setPlayerVariable(entity, "needExplode", true);

		// 重置踢击状态
		setPlayerVariable(entity, "kcik", false);

		// 创建爆炸（现在可以安全地创建，因为伤害会被拦截）
		if (world instanceof Level _level && !_level.isClientSide()) {
			_level.explode(null, x, y, z, 4, Level.ExplosionInteraction.NONE);
		}

		// 处理动画等其他逻辑
		if (world.isClientSide()) {
			if (entity instanceof AbstractClientPlayer player) {
				var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
				if (animation != null) {
					animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", "steadily"))));
				}
			}
		}

		// 延迟重置无敌状态和爆炸标志
		kamen_rider_boss_you_and_me.queueServerWork(5, () -> {
			setPlayerVariable(entity, "wudi", false);
			setPlayerVariable(entity, "needExplode", false);

			// 重置踢击时间变量
			entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.kickStartTime = 0L;
				capability.kickStartY = 0.0D;
				capability.syncPlayerVariables(entity);
			});
		});
	}

	// 处理动画的通用方法
	private static void handleAnimation(LevelAccessor world, Entity entity, String animationName) {
		// 客户端动画处理
		if (world.isClientSide()) {
			if (entity instanceof AbstractClientPlayer player) {
				var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(
						new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
				if (animation != null) {
					animation.setAnimation(new KeyframeAnimationPlayer(
							PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", animationName))));
				}
			}
		}

		// 服务端动画同步
		if (!world.isClientSide()) {
			if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
				List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
				synchronized (connections) {
					Iterator<Connection> iterator = connections.iterator();
					while (iterator.hasNext()) {
						Connection connection = iterator.next();
						if (!connection.isConnecting() && connection.isConnected())
							kamen_rider_boss_you_and_me.PACKET_HANDLER.sendTo(
									new SetupAnimationsProcedure.kamen_rider_boss_you_and_meModAnimationMessage(
											Component.literal(animationName), entity.getId(), true),
									connection, NetworkDirection.PLAY_TO_CLIENT);
					}
				}
			}
		}
	}

	// 设置玩家变量的通用方法
	// 设置玩家变量的通用方法
	private static void setPlayerVariable(Entity entity, String variableName, boolean value) {
		entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			switch (variableName) {
				case "kcik" -> capability.kcik = value;
				case "wudi" -> capability.wudi = value;
				case "needExplode" -> capability.needExplode = value;
			}
			capability.syncPlayerVariables(entity);
		});
	}
}