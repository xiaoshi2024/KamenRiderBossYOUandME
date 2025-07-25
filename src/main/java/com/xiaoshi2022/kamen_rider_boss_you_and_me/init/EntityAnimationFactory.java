//package com.xiaoshi2022.kamen_rider_boss_you_and_me.init;
//
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.event.entity.living.LivingEvent;
//
//@Mod.EventBusSubscriber
//public class EntityAnimationFactory {
//	@SubscribeEvent
//	public static void onEntityTick(LivingEvent.LivingTickEvent event) {
//		if (event != null && event.getEntity() != null) {
//			if (event.getEntity() instanceof KnecromghostEntity syncable) {
//				String animation = syncable.getSyncedAnimation();
//				if (!animation.equals("undefined")) {
//					syncable.setAnimation("undefined");
//					syncable.animationprocedure = animation;
//				}
//			}
//		}
//	}
//}
