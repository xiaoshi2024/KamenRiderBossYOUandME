package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
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
			execute(event, event.getEntity());
		}
	}

	// 处理踢击爆炸伤害排除
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

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;

		// 修改无敌状态处理逻辑：只有在踢击动作期间才真正无敌
		if (entity instanceof Player player) {
			KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
					.orElse(new KRBVariables.PlayerVariables());
			
			// 只有同时满足wudi为true且kcik为true时才无敌
			// 这样确保无敌状态只在踢击动作期间有效
			if (variables.wudi && variables.kick && event != null && event.isCancelable()) {
				event.setCanceled(true);
			}
		}
	}
}