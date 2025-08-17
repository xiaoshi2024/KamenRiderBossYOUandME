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
		if ((entity.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables())).kcik == true
				&& (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem() == ModItems.BARON_LEMON_HELMET.get()) {
			if (!entity.onGround()) {
				// ▼▼▼▼▼ 核心修改部分 ▼▼▼▼▼
				Vec3 look = entity.getLookAngle();
				// ▼▼▼▼▼ 关键数值调整 ▼▼▼▼▼
				double currentY = entity.getDeltaMovement().y;
				double newY = Math.min(currentY, 0) - 0.35; // 确保不会弹跳，且匀速下落

				entity.setDeltaMovement(new Vec3(
						look.x * 1.8,  // 水平冲刺速度（调低更易控制）
						newY,          // 垂直运动（强制向下）
						look.z * 1.8
				));
					if (world.isClientSide()) {
						if (entity instanceof AbstractClientPlayer player) {
							var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));
							if (animation != null && !animation.isActive()) {
								animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("kamen_rider_boss_you_and_me", "kick"))));
							}
						}
					}
					// 服务器端处理粒子生成（放在客户端检查之外）
					else if (world instanceof ServerLevel srvLvl) {
						Vec3 looks = entity.getLookAngle();
						// 发送粒子给所有客户端
						srvLvl.sendParticles(
								ParticleTypesRegistry.LEMONSLICE.get(),
								entity.getX(),
								entity.getY() + 0.9,
								entity.getZ(),
								4, // 粒子数量
								looks.x * 0.1,
								looks.y * 0.1,
								looks.z * 0.1,
								0 // 额外速度
						);
					}
					if (!world.isClientSide()) {
						if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
							List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
							synchronized (connections) {
								Iterator<Connection> iterator = connections.iterator();
								while (iterator.hasNext()) {
									Connection connection = iterator.next();
									if (!connection.isConnecting() && connection.isConnected())
										kamen_rider_boss_you_and_me.PACKET_HANDLER.sendTo(new SetupAnimationsProcedure.kamen_rider_boss_you_and_meModAnimationMessage(Component.literal("kick"), entity.getId(), false), connection, NetworkDirection.PLAY_TO_CLIENT);
								}
							}
						}
					}
				}
			}
	}
}
