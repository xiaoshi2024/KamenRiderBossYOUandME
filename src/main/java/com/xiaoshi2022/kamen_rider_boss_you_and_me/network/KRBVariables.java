package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class  KRBVariables {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		 kamen_rider_boss_you_and_me.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new, PlayerVariablesSyncMessage::handler);
	}

	@SubscribeEvent
	public static void init(RegisterCapabilitiesEvent event) {
		event.register(PlayerVariables.class);
	}

	@Mod.EventBusSubscriber
	public static class EventBusVariableHandlers {
		@SubscribeEvent
		public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void clonePlayer(PlayerEvent.Clone event) {
			PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
			PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
			if (!event.isWasDeath()) {
				clone.kcik = original.kcik;
			clone.wudi = original.wudi;
			clone.lastKickTime = original.lastKickTime;
			clone.kickStartTime = original.kickStartTime;
			clone.kickStartY = original.kickStartY;
			clone.needExplode = original.needExplode;
			// 复制锁种相关变量
			clone.cherry_ready = original.cherry_ready;
			clone.cherry_ready_time = original.cherry_ready_time;
			clone.lemon_ready = original.lemon_ready;
			clone.lemon_ready_time = original.lemon_ready_time;
			clone.peach_ready = original.peach_ready;
			clone.peach_ready_time = original.peach_ready_time;
			clone.melon_ready = original.melon_ready;
			clone.melon_ready_time = original.melon_ready_time;
			clone.banana_ready = original.banana_ready;
			clone.banana_ready_time = original.banana_ready_time;

				// 眼魂
				clone.isMegaUiorderTransformed = original.isMegaUiorderTransformed;
				clone.isNecromStandby = original.isNecromStandby;

			} else {
			// 玩家死亡重生时，重置所有状态变量
			clone.kcik = false;
			clone.wudi = false;
			clone.lastKickTime = 0L;
			clone.kickStartTime = 0L;
			clone.kickStartY = 0.0D;
			clone.needExplode = false;
			clone.hasExploded = false;
			// 重置锁种相关变量
			clone.cherry_ready = false;
			clone.cherry_ready_time = 0L;
			clone.lemon_ready = false;
			clone.lemon_ready_time = 0L;
			clone.peach_ready = false;
			clone.peach_ready_time = 0L;
			clone.melon_ready = false;
			clone.melon_ready_time = 0L;
			clone.banana_ready = false;
			clone.banana_ready_time = 0L;
			// 重置黑暗Kiva相关变量
			clone.dark_kiva_bat_mode = false;
			clone.dark_kiva_bat_mode_time = 0L;
			clone.dark_kiva_blood_suck_active = false;
			clone.dark_kiva_blood_suck_cooldown = 0L;
			clone.dark_kiva_sonic_blast_active = false;
			clone.dark_kiva_sonic_blast_cooldown = 0L;
			clone.dark_kiva_blood_steal_cooldown = 0L;

			// 眼魂
				clone.isMegaUiorderTransformed = false;
				clone.isNecromStandby = false;


			// 新增：重置其他状态变量
			clone.isDarkKivaBeltEquipped = false;
			clone.baseMaxHealth = 20.0D;
			clone.lastCustomArmorCount = 0;
			}
		}
	}

	public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {
	});

	@Mod.EventBusSubscriber
	private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
		@SubscribeEvent
		public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
				event.addCapability(new ResourceLocation("kamen_rider_boss_you_and_me", "player_variables"), new PlayerVariablesProvider());
		}

		private final PlayerVariables playerVariables = new PlayerVariables();
		private final LazyOptional<PlayerVariables> instance = LazyOptional.of(() -> playerVariables);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
		}

		@Override
		public Tag serializeNBT() {
			return playerVariables.writeNBT();
		}

		@Override
		public void deserializeNBT(Tag nbt) {
			playerVariables.readNBT(nbt);
		}
	}

	public static class PlayerVariables {
		public boolean kcik = false;
		public boolean needExplode = false;
		public boolean wudi = false;
		public boolean hasExploded = false;   // ← 新增
		public long lastKickTime = 0L;
		public long kickStartTime = 0L;
		public double kickStartY = 0.0D;
		public double kickInitialVelocity = 0.0D;

		//眼魂相关
		public boolean isMegaUiorderTransformed = false; // 新增字段：记录是否装备了Mega_uiorder并变身
		public boolean isNecromStandby = false;

		// 锁种相关变量
		public boolean cherry_ready = false;
		public long cherry_ready_time = 0L;
		public boolean lemon_ready = false;
		public long lemon_ready_time = 0L;
		public boolean peach_ready = false;
		public long peach_ready_time = 0L;
		public boolean melon_ready = false;
		public long melon_ready_time = 0L;
		public boolean banana_ready = false;
		public long banana_ready_time = 0L;
		public boolean orange_ready = false;
		public long orange_ready_time = 0L;
		public boolean dragonfruit_ready = false;
		public long dragonfruit_ready_time = 0L;
		public long dragonfruit_time = 0L;
		// 黑暗Kiva相关变量
		public boolean dark_kiva_bat_mode = false;    // 蝙蝠形态
		public long dark_kiva_bat_mode_time = 0L;     // 蝙蝠形态开始时间
		public boolean dark_kiva_blood_suck_active = false; // 吸血能力激活状态
		public long dark_kiva_blood_suck_cooldown = 0L; // 吸血能力冷却时间
		public boolean dark_kiva_sonic_blast_active = false; // 声波爆破激活状态
		public long dark_kiva_sonic_blast_cooldown = 0L; // 声波爆破冷却时间
		public long dark_kiva_blood_steal_cooldown = 0L; // 生命偷取冷却时间

		// 在PlayerVariables类中添加字段
		public double baseMaxHealth = 20.0D; // 默认基础生命值为20点
		public int lastCustomArmorCount = 0;
    public boolean isDarkKivaBeltEquipped = false; // 新增字段：记录是否装备了黑暗Kiva腰带
    public boolean isEvilBatsTransformed = false; // 新增字段：记录是否装备了EvilBats盔甲并变身

        public void syncPlayerVariables(Entity entity) {
			if (entity instanceof ServerPlayer serverPlayer)
				 kamen_rider_boss_you_and_me.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
		}

		public Tag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("kcik", kcik);
		nbt.putBoolean("wudi", wudi);
		nbt.putLong("lastKickTime", lastKickTime);
		nbt.putLong("kickStartTime", kickStartTime);
		nbt.putDouble("kickStartY", kickStartY);
		nbt.putBoolean("hasExploded", hasExploded);
			nbt.putBoolean("needExplode", needExplode);
		// 序列化锁种相关变量
		nbt.putBoolean("cherry_ready", cherry_ready);
		nbt.putLong("cherry_ready_time", cherry_ready_time);
		nbt.putBoolean("lemon_ready", lemon_ready);
		nbt.putLong("lemon_ready_time", lemon_ready_time);
		nbt.putBoolean("peach_ready", peach_ready);
		nbt.putLong("peach_ready_time", peach_ready_time);
		nbt.putBoolean("melon_ready", melon_ready);
		nbt.putLong("melon_ready_time", melon_ready_time);
		nbt.putBoolean("banana_ready", banana_ready);
		nbt.putLong("banana_ready_time", banana_ready_time);
		nbt.putBoolean("orange_ready", orange_ready);
		nbt.putLong("orange_ready_time", orange_ready_time);
		nbt.putBoolean("dragonfruit_ready", dragonfruit_ready);
		nbt.putLong("dragonfruit_ready_time", dragonfruit_ready_time);
		nbt.putLong("dragonfruit_time", dragonfruit_time);
		// 序列化黑暗Kiva相关变量
		nbt.putBoolean("dark_kiva_bat_mode", dark_kiva_bat_mode);
		nbt.putLong("dark_kiva_bat_mode_time", dark_kiva_bat_mode_time);
		nbt.putBoolean("dark_kiva_blood_suck_active", dark_kiva_blood_suck_active);
		nbt.putLong("dark_kiva_blood_suck_cooldown", dark_kiva_blood_suck_cooldown);
		nbt.putBoolean("dark_kiva_sonic_blast_active", dark_kiva_sonic_blast_active);
		nbt.putLong("dark_kiva_sonic_blast_cooldown", dark_kiva_sonic_blast_cooldown);
		nbt.putLong("dark_kiva_blood_steal_cooldown", dark_kiva_blood_steal_cooldown);

		//眼魂
			nbt.putBoolean("isMegaUiorderTransformed", isMegaUiorderTransformed); // 新增：序列化Mega_uiorder变身状态
			nbt.putBoolean("isNecromStandby", isNecromStandby); // 新增：序列化Necrom待机状态

		nbt.putBoolean("isEvilBatsTransformed", isEvilBatsTransformed); // 新增：存储EvilBats变身状态

			nbt.putDouble("baseMaxHealth", baseMaxHealth);   // ← 新增
		return nbt;
	}

		public void readNBT(Tag tag) {
		CompoundTag nbt = (CompoundTag) tag;
		kcik = nbt.getBoolean("kcik");
		wudi = nbt.getBoolean("wudi");
		lastKickTime = nbt.getLong("lastKickTime");
		kickStartTime = nbt.getLong("kickStartTime");
		kickStartY = nbt.getDouble("kickStartY");
			hasExploded = nbt.getBoolean("hasExploded");
			needExplode = nbt.getBoolean("needExplode");
		// 反序列化锁种相关变量
		cherry_ready = nbt.getBoolean("cherry_ready");
		cherry_ready_time = nbt.getLong("cherry_ready_time");
		lemon_ready = nbt.getBoolean("lemon_ready");
		lemon_ready_time = nbt.getLong("lemon_ready_time");
		peach_ready = nbt.getBoolean("peach_ready");
		peach_ready_time = nbt.getLong("peach_ready_time");
		melon_ready = nbt.getBoolean("melon_ready");
		melon_ready_time = nbt.getLong("melon_ready_time");
		banana_ready = nbt.getBoolean("banana_ready");
		banana_ready_time = nbt.getLong("banana_ready_time");
		orange_ready = nbt.getBoolean("orange_ready");
		orange_ready_time = nbt.getLong("orange_ready_time");
		dragonfruit_ready = nbt.getBoolean("dragonfruit_ready");
		dragonfruit_ready_time = nbt.getLong("dragonfruit_ready_time");
		dragonfruit_time = nbt.getLong("dragonfruit_time");

		//眼魂
			isMegaUiorderTransformed = nbt.getBoolean("isMegaUiorderTransformed"); // 新增：读取Mega_uiorder变身状态
			isNecromStandby = nbt.getBoolean("isNecromStandby"); // 新增：读取Necrom待机状态

		isEvilBatsTransformed = nbt.getBoolean("isEvilBatsTransformed"); // 新增：读取EvilBats变身状态

			baseMaxHealth = nbt.getDouble("baseMaxHealth");  // ← 新增
		}
	}

	public static class PlayerVariablesSyncMessage {
		private final PlayerVariables data;

		public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
			this.data = new PlayerVariables();
			this.data.readNBT(buffer.readNbt());
		}

		public PlayerVariablesSyncMessage(PlayerVariables data) {
			this.data = data;
		}

		public static void buffer(PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
			buffer.writeNbt((CompoundTag) message.data.writeNBT());
		}

		public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				if (!context.getDirection().getReceptionSide().isServer()) {
					PlayerVariables variables = ((PlayerVariables) Minecraft.getInstance().player.getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
					variables.kcik = message.data.kcik;
				variables.wudi = message.data.wudi;
				variables.lastKickTime = message.data.lastKickTime;
				variables.kickStartTime = message.data.kickStartTime;
				variables.kickStartY = message.data.kickStartY;
				variables.needExplode = message.data.needExplode;

				//眼魂
					variables.isMegaUiorderTransformed = message.data.isMegaUiorderTransformed; // 新增：同步Mega_uiorder变身状态
					variables.isNecromStandby = message.data.isNecromStandby;

				// 同步锁种相关变量
				variables.cherry_ready = message.data.cherry_ready;
				variables.cherry_ready_time = message.data.cherry_ready_time;
				variables.lemon_ready = message.data.lemon_ready;
				variables.lemon_ready_time = message.data.lemon_ready_time;
				variables.peach_ready = message.data.peach_ready;
				variables.peach_ready_time = message.data.peach_ready_time;
				variables.melon_ready = message.data.melon_ready;
				variables.melon_ready_time = message.data.melon_ready_time;
				variables.banana_ready = message.data.banana_ready;
			variables.banana_ready_time = message.data.banana_ready_time;
			variables.orange_ready = message.data.orange_ready;
			variables.orange_ready_time = message.data.orange_ready_time;

					variables.baseMaxHealth = message.data.baseMaxHealth;
				variables.lastCustomArmorCount = message.data.lastCustomArmorCount; // 添加这行，同步lastCustomArmorCount
				variables.isDarkKivaBeltEquipped = message.data.isDarkKivaBeltEquipped; // 添加这行，同步黑暗Kiva腰带状态
				variables.isEvilBatsTransformed = message.data.isEvilBatsTransformed; // 添加这行，同步EvilBats变身状态
				}
			});
			context.setPacketHandled(true);
		}
	}
}

