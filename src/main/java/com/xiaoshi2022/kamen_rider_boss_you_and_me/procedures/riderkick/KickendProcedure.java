package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
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
		if ((entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem() == ModItems.BARON_LEMON_HELMET.get()
				&& (entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables())).kcik == true && entity.onGround()) {
			{
				boolean _setval = false;
				entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.kcik = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (world instanceof Level _level && !_level.isClientSide())
				_level.explode(null, x, y, z, 4, Level.ExplosionInteraction.NONE);
			if (world.isClientSide()) {
				if (entity instanceof AbstractClientPlayer player) {
					var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
					if (animation != null) {
						animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", "steadily"))));
					}
				}
			}
			if (!world.isClientSide()) {
				if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
					List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
					synchronized (connections) {
						Iterator<Connection> iterator = connections.iterator();
						while (iterator.hasNext()) {
							Connection connection = iterator.next();
							if (!connection.isConnecting() && connection.isConnected())
								kamen_rider_boss_you_and_me.PACKET_HANDLER.sendTo(new SetupAnimationsProcedure.kamen_rider_boss_you_and_meModAnimationMessage(Component.literal("steadily"), entity.getId(), true), connection, NetworkDirection.PLAY_TO_CLIENT);
						}
					}
				}
			}
			entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
			// 仅在服务端执行
			if (!world.isClientSide() && entity instanceof Player player) {
				// 获取当前饱食度
				int newFoodLevel = player.getFoodData().getFoodLevel() - 3;
				// 保证不小于 0
				newFoodLevel = Math.max(newFoodLevel, 0);
				// 直接设置
				player.getFoodData().setFoodLevel(newFoodLevel);
			}
			{
				boolean _setval = false;
				entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wudi = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
		if ((entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()
				&& (entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables())).kcik == true && entity.onGround()) {
			{
				boolean _setval = false;
				entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.kcik = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (world instanceof Level _level && !_level.isClientSide())
				_level.explode(null, x, y, z, 4, Level.ExplosionInteraction.NONE);
			if (world.isClientSide()) {
				if (entity instanceof AbstractClientPlayer player) {
					var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
					if (animation != null) {
						animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", "steadily"))));
					}
				}
			}
			if (!world.isClientSide()) {
				if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
					List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
					synchronized (connections) {
						Iterator<Connection> iterator = connections.iterator();
						while (iterator.hasNext()) {
							Connection connection = iterator.next();
							if (!connection.isConnecting() && connection.isConnected())
								kamen_rider_boss_you_and_me.PACKET_HANDLER.sendTo(new SetupAnimationsProcedure.kamen_rider_boss_you_and_meModAnimationMessage(Component.literal("steadily"), entity.getId(), true), connection, NetworkDirection.PLAY_TO_CLIENT);
						}
					}
				}
			}
			entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
			// 仅在服务端执行
			if (!world.isClientSide() && entity instanceof Player player) {
				// 获取当前饱食度
				int newFoodLevel = player.getFoodData().getFoodLevel() - 3;
				// 保证不小于 0
				newFoodLevel = Math.max(newFoodLevel, 0);
				// 直接设置
				player.getFoodData().setFoodLevel(newFoodLevel);
			}
			{
				boolean _setval = false;
				entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wudi = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
		if ((entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem() == ModItems.SIGURD_HELMET.get()
				&& (entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables())).kcik == true && entity.onGround()) {
			{
				boolean _setval = false;
				entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.kcik = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (world instanceof Level _level && !_level.isClientSide())
				_level.explode(null, x, y, z, 4, Level.ExplosionInteraction.NONE);
			if (world.isClientSide()) {
				if (entity instanceof AbstractClientPlayer player) {
					var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
					if (animation != null) {
						animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", "steadily"))));
					}
				}
			}
			if (!world.isClientSide()) {
				if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
					List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
					synchronized (connections) {
						Iterator<Connection> iterator = connections.iterator();
						while (iterator.hasNext()) {
							Connection connection = iterator.next();
							if (!connection.isConnecting() && connection.isConnected())
								kamen_rider_boss_you_and_me.PACKET_HANDLER.sendTo(new SetupAnimationsProcedure.kamen_rider_boss_you_and_meModAnimationMessage(Component.literal("steadily"), entity.getId(), true), connection, NetworkDirection.PLAY_TO_CLIENT);
						}
					}
				}
			}
			entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
			// 仅在服务端执行
			if (!world.isClientSide() && entity instanceof Player player) {
				// 获取当前饱食度
				int newFoodLevel = player.getFoodData().getFoodLevel() - 3;
				// 保证不小于 0
				newFoodLevel = Math.max(newFoodLevel, 0);
				// 直接设置
				player.getFoodData().setFoodLevel(newFoodLevel);
			}
			{
				boolean _setval = false;
				entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wudi = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
	}
}
