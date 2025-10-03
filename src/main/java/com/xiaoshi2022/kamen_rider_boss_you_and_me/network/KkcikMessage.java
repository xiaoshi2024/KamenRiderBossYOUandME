
package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.riderkick.KkcikAnXiaAnJianShiProcedure;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class KkcikMessage {
	int type, pressedms;

	public KkcikMessage(int type, int pressedms) {
		this.type = type;
		this.pressedms = pressedms;
	}

	public KkcikMessage(FriendlyByteBuf buffer) {
		this.type = buffer.readInt();
		this.pressedms = buffer.readInt();
	}

	public static void buffer(KkcikMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}

	public static void handler(KkcikMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			pressAction(context.getSender(), message.type, message.pressedms);
		});
		context.setPacketHandled(true);
	}

	public static void pressAction(Player entity, int type, int pressedms) {

		// 只在服务端执行
		if (!entity.level().isClientSide()) {
			CuriosApi.getCuriosInventory(entity)
					.resolve()
					.flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver))
					.ifPresent(slot -> {
						Genesis_driver driver = (Genesis_driver) slot.stack().getItem();
						if (driver.getMode(slot.stack()) == Genesis_driver.BeltMode.LEMON) {
							// 1. 发包给所有跟踪该实体的玩家（含自己）
							PacketHandler.INSTANCE.send(
									PacketDistributor.TRACKING_ENTITY.with(() -> entity),
									new BeltAnimationPacket(entity.getId(), "lemon_tick", driver.getMode(slot.stack()))
							);
							// 2. 服务端本地播动画（GeckoLib 会在服务端记录，客户端随后同步）
							driver.triggerAnim(entity, "controller", "lemon_tick");

							// 3. 服务端播放声音
							entity.level().playSound(
									null,
									entity.getX(), entity.getY(), entity.getZ(),
									ModBossSounds.LEMON_TICK.get(),
									SoundSource.PLAYERS,
									2.0F, 1.0F
							);
						}
					});
		}

		Level world = entity.level();
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		// security measure to prevent arbitrary chunk generation
		if (!world.hasChunkAt(entity.blockPosition()))
			return;
		if (type == 0) {

			KkcikAnXiaAnJianShiProcedure.execute(world, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		kamen_rider_boss_you_and_me.addNetworkMessage(KkcikMessage.class, KkcikMessage::buffer, KkcikMessage::new, KkcikMessage::handler);
	}
}