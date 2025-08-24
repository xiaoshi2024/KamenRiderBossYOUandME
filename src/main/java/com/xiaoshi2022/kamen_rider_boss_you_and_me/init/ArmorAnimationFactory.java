package com.xiaoshi2022.kamen_rider_boss_you_and_me.init;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import software.bernie.geckolib.animatable.GeoItem;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

@Mod.EventBusSubscriber
public class ArmorAnimationFactory {
	@SubscribeEvent
	public static void animatedArmors(TickEvent.PlayerTickEvent event) {
		String animation = "";
		if (event.phase == TickEvent.Phase.END) {
			if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() != (ItemStack.EMPTY).getItem() && event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof GeoItem) {
				if (!event.player.getItemBySlot(EquipmentSlot.HEAD).getOrCreateTag().getString("geckoAnim").equals("")) {
					animation = event.player.getItemBySlot(EquipmentSlot.HEAD).getOrCreateTag().getString("geckoAnim");
					event.player.getItemBySlot(EquipmentSlot.HEAD).getOrCreateTag().putString("geckoAnim", "");
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof RidernecromItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof rider_baronsItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof baron_lemonItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ZangetsuShinItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Sigurd animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Marika animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Duke animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Dark_orangels animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TyrantItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;

				}
			}
			if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != (ItemStack.EMPTY).getItem() && event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof GeoItem) {
				if (!event.player.getItemBySlot(EquipmentSlot.CHEST).getOrCreateTag().getString("geckoAnim").equals("")) {
					animation = event.player.getItemBySlot(EquipmentSlot.CHEST).getOrCreateTag().getString("geckoAnim");
					event.player.getItemBySlot(EquipmentSlot.CHEST).getOrCreateTag().putString("geckoAnim", "");
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RidernecromItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof rider_baronsItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof baron_lemonItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ZangetsuShinItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Sigurd animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Marika animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Duke animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Dark_orangels animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TyrantItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;

				}
			}
			if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() != (ItemStack.EMPTY).getItem() && event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof GeoItem) {
				if (!event.player.getItemBySlot(EquipmentSlot.LEGS).getOrCreateTag().getString("geckoAnim").equals("")) {
					animation = event.player.getItemBySlot(EquipmentSlot.LEGS).getOrCreateTag().getString("geckoAnim");
					event.player.getItemBySlot(EquipmentSlot.LEGS).getOrCreateTag().putString("geckoAnim", "");
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof RidernecromItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof rider_baronsItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof baron_lemonItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ZangetsuShinItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Sigurd animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Marika animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Duke animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Dark_orangels animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof TyrantItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;

				}
			}
			if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() != (ItemStack.EMPTY).getItem() && event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof GeoItem) {
				if (!event.player.getItemBySlot(EquipmentSlot.FEET).getOrCreateTag().getString("geckoAnim").equals("")) {
					animation = event.player.getItemBySlot(EquipmentSlot.FEET).getOrCreateTag().getString("geckoAnim");
					event.player.getItemBySlot(EquipmentSlot.FEET).getOrCreateTag().putString("geckoAnim", "");
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof RidernecromItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof rider_baronsItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof baron_lemonItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ZangetsuShinItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Sigurd animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Marika animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Duke animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Dark_orangels animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;
					if (event.player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TyrantItem animatable && event.player.level().isClientSide())
						animatable.animationprocedure = animation;

				}
			}
		}
	}
}
