package com.xiaoshi2022.kamen_rider_boss_you_and_me.init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;

@Mod.EventBusSubscriber
public final class ArmorAnimationFactory {

	/* 嵌套接口：所有盔甲类只需实现这个接口即可统一设置动画 */
	public interface AnimatableAccessor {
		void setAnimationProcedure(String procedure);
	}

	/* ---------------------------------------------------------- */

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
		if (e.phase != TickEvent.Phase.END) return;

		handleSlot(e, EquipmentSlot.HEAD);
		handleSlot(e, EquipmentSlot.CHEST);
		handleSlot(e, EquipmentSlot.LEGS);
		handleSlot(e, EquipmentSlot.FEET);
	}

	private static void handleSlot(TickEvent.PlayerTickEvent e, EquipmentSlot slot) {
		ItemStack stack = e.player.getItemBySlot(slot);
		if (stack.isEmpty() || !(stack.getItem() instanceof GeoItem)) return;

		var tag = stack.getOrCreateTag();
		String anim = tag.getString("geckoAnim");
		if (anim.isEmpty()) return;

		tag.remove("geckoAnim"); // 只触发一次

		if (stack.getItem() instanceof AnimatableAccessor accessor) {
			accessor.setAnimationProcedure(anim);
		}
	}
}