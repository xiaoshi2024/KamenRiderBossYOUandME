package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class KcikwudiProcedure {

	@SubscribeEvent
	public static void onEntityAttacked(LivingHurtEvent event) {
		if (event.getEntity() != null && event.getSource() != null) {
			handleKickExplosionDamage(event);
			handleFallDamage(event);
			execute(event, event.getEntity());
		}
	}

	// 处理踢击爆炸伤害排除和坠落伤害保护
	private static void handleKickExplosionDamage(LivingHurtEvent event) {
		Entity entity = event.getEntity();
		DamageSource damageSource = event.getSource();

		// 检查是否是爆炸伤害且来自玩家自己的踢击
		if (damageSource.is(DamageTypes.EXPLOSION) && entity instanceof Player player) {
			// 检查是否是踢击造成的爆炸（通过needExplode标志）
			if (player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
					.orElse(new KRBVariables.PlayerVariables()).needExplode) {
				// 取消对自己的爆炸伤害
				event.setCanceled(true);

				// 立即重置标志
				player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.needExplode = false;
					capability.syncPlayerVariables(player);
				});
			}
		}
	}

	// 处理坠落伤害保护
	private static void handleFallDamage(LivingHurtEvent event) {
		Entity entity = event.getEntity();
		DamageSource damageSource = event.getSource();

		// 检查是否是坠落伤害
		if (damageSource.is(DamageTypes.FALL) && entity instanceof Player player) {
			// 检查是否是踢击状态或处于无敌状态
			KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
					.orElse(new KRBVariables.PlayerVariables());
			
			// 如果玩家正在踢击或处于无敌状态，取消坠落伤害
			if (variables.kick || variables.wudi) {
				event.setCanceled(true);
			}
		}
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;

		// 修改无敌状态处理逻辑：只要wudi为true就无敌
	if (entity instanceof Player player) {
		KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new KRBVariables.PlayerVariables());
		
		// 只要wudi为true就无敌，取消所有类型的伤害
		// 但不会取消kill命令造成的伤害，也不会取消骑士踢造成的伤害
		if (variables.wudi && event instanceof LivingHurtEvent hurtEvent) {
			// 检查伤害来源是否为kill命令
			if (!hurtEvent.getSource().getMsgId().equals("outOfWorld")) {
				// 检查伤害来源是否为玩家攻击（骑士踢）
				// 如果是玩家攻击，则不取消伤害，确保骑士踢可以对其他玩家造成伤害
				// 使用更可靠的方式来判断是否是玩家攻击
				if (!(hurtEvent.getSource().getDirectEntity() instanceof Player)) {
					hurtEvent.setCanceled(true);
				}
			}
		}
	}
	}
}