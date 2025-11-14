
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package com.xiaoshi2022.kamen_rider_boss_you_and_me.init;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.KkcikMessage;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick.KicktimeProcedure;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class KamcsModKeyMappings {
	public static final KeyMapping KKCIK = new KeyMapping("key.kamen_rider_boss_you_and_me.kick", GLFW.GLFW_KEY_R, "key.categories.misc") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				// 新增：检查是否可以踢击
				if (KicktimeProcedure.canPerformKick(Minecraft.getInstance().player)) {
					kamen_rider_boss_you_and_me.PACKET_HANDLER.sendToServer(new KkcikMessage(0, 0));
					KkcikMessage.pressAction(Minecraft.getInstance().player, 0, 0);
				}
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(KKCIK);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				KKCIK.consumeClick();
			}
		}
	}
}