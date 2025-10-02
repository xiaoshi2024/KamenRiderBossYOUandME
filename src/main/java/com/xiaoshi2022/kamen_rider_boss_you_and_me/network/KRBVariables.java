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
public class KRBVariables {
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
			// 玩家死亡重生时，重置大部分状态变量
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
			clone.isOverlord = false; // 玩家死亡时重置Overlord状态
			// 修改：玩家死亡后恢复为人类
			clone.isGiifu = false;
			clone.baseMaxHealth = 20.0D; // 重置为默认生命值
			clone.lastCustomArmorCount = 0;
			}
		}
	}

	public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {});

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
	// 封印结界（Fuuin Kekkai）技能相关变量
	public boolean dark_kiva_fuuin_kekkai_active = false; // 封印结界激活状态
	public long dark_kiva_fuuin_kekkai_cooldown = 0L; // 封印结界冷却时间

	// 在PlayerVariables类中添加字段
	public double baseMaxHealth = 20.0D; // 默认基础生命值为20点
	public int lastCustomArmorCount = 0;
	public boolean isDarkKivaBeltEquipped = false; // 新增字段：记录是否装备了黑暗Kiva腰带
	public boolean isGenesisDriverEquipped = false; // 新增字段：记录是否装备了Genesis驱动器
	public boolean isSengokuDriverEmptyEquipped = false; // 新增字段：记录是否装备了空的Sengoku驱动器
	public boolean isSengokuDriverFilledEquipped = false; // 新增字段：记录是否装备了有锁种的Sengoku驱动器
	public boolean isOrochiDriverEquipped = false; // 新增字段：记录是否装备了Orochi驱动器
	public boolean isEvilBatsTransformed = false; // 新增字段：记录是否装备了EvilBats盔甲并变身
	public boolean isEvilBatsStealthed = false; // 新增字段：记录是否处于EvilBats隐密模式
	public boolean isDarkOrangelsTransformed = false; // 新增字段：记录是否装备了Dark_orangels盔甲并变身
	public long beltRemovedTime = 0L; // 记录腰带被移除的时间
	public String removedBeltType = ""; // 记录被移除的腰带类型
	// Duke骑士技能相关变量
	public boolean dukeKnightSummoned = false; // Duke骑士是否已召唤
	public long dukeKnightCooldown = 0L; // Duke骑士技能冷却时间（毫秒）
	public int dukeKnightCost = 5; // 召唤Duke骑士消耗的经验值等级
	// Duke玩家战斗数据分析技能相关变量
	public boolean isDukeCombatAnalysisActive = false; // 战斗数据分析是否激活
	public long dukeCombatAnalysisCooldown = 0L; // 战斗数据分析技能冷却时间（毫秒）
	public long dukeCombatAnalysisEndTime = 0L; // 战斗数据分析结束时间（毫秒）
    
	// 自定义蓝条系统 - 骑士能量
	public double riderEnergy = 0.0D; // 当前骑士能量值
	public double maxRiderEnergy = 100.0D; // 最大骑士能量值
	public double riderEnergyGainPerDamage = 1.0D; // 每造成1点伤害获得的能量值
	public double riderEnergyNaturalRegen = 0.02D; // 每秒自然恢复的能量值
	public boolean isRiderEnergyFull = false; // 能量是否已满（用于客户端显示提示）
	public long lastEnergyFullTime = 0L; // 上次能量充满的时间（用于防止提示频繁触发）
    
	// 累积攻击力相关变量 - 用于骑士能量跟随玩家累积攻击力变化
	public double accumulatedAttackDamage = 0.0D; // 玩家累积造成的伤害
	public double accumulatedAttackDamageModifier = 0.1D; // 累积伤害转换为能量的系数
    
	// 基础巴隆技能相关变量
	// 玩家是否为星爵巴隆（Overlord）状态
	public boolean isOverlord = false;
	// 玩家是否为基夫（Giifu）状态
	public boolean isGiifu = false;
	// 巴隆香蕉能量技能冷却时间
	public long baron_banana_energy_cooldown = 0L;

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
		nbt.putLong("dark_kiva_fuuin_kekkai_cooldown", dark_kiva_fuuin_kekkai_cooldown);
		nbt.putBoolean("dark_kiva_fuuin_kekkai_active", dark_kiva_fuuin_kekkai_active);
		nbt.putLong("dark_kiva_blood_suck_cooldown", dark_kiva_blood_suck_cooldown);
		nbt.putBoolean("dark_kiva_sonic_blast_active", dark_kiva_sonic_blast_active);
		nbt.putLong("dark_kiva_sonic_blast_cooldown", dark_kiva_sonic_blast_cooldown);
		nbt.putLong("dark_kiva_blood_steal_cooldown", dark_kiva_blood_steal_cooldown);

		//眼魂
		nbt.putBoolean("isMegaUiorderTransformed", isMegaUiorderTransformed); // 新增：序列化Mega_uiorder变身状态
		nbt.putBoolean("isNecromStandby", isNecromStandby); // 新增：序列化Necrom待机状态

		nbt.putBoolean("isEvilBatsTransformed", isEvilBatsTransformed); // 新增：存储EvilBats变身状态
		nbt.putBoolean("isEvilBatsStealthed", isEvilBatsStealthed); // 新增：存储EvilBats隐密模式状态
		nbt.putBoolean("isDarkKivaBeltEquipped", isDarkKivaBeltEquipped); // 新增字段：记录是否装备了黑暗Kiva腰带
		nbt.putBoolean("isGenesisDriverEquipped", isGenesisDriverEquipped); // 新增字段：记录是否装备了Genesis驱动器
		nbt.putBoolean("isSengokuDriverEmptyEquipped", isSengokuDriverEmptyEquipped); // 新增字段：记录是否装备了空的Sengoku驱动器
		nbt.putBoolean("isSengokuDriverFilledEquipped", isSengokuDriverFilledEquipped); // 新增字段：记录是否装备了有锁种的Sengoku驱动器
		nbt.putBoolean("isOrochiDriverEquipped", isOrochiDriverEquipped); // 新增字段：记录是否装备了Orochi驱动器
		nbt.putBoolean("isDarkOrangelsTransformed", isDarkOrangelsTransformed); // 新增字段：记录是否装备了Dark_orangels盔甲并变身

		nbt.putDouble("baseMaxHealth", baseMaxHealth);   // ← 新增
		// Duke骑士技能相关变量
		nbt.putBoolean("dukeKnightSummoned", dukeKnightSummoned);
		nbt.putLong("dukeKnightCooldown", dukeKnightCooldown);
		nbt.putInt("dukeKnightCost", dukeKnightCost);
		// Duke玩家战斗数据分析技能相关变量
		nbt.putBoolean("isDukeCombatAnalysisActive", isDukeCombatAnalysisActive);
		nbt.putLong("dukeCombatAnalysisCooldown", dukeCombatAnalysisCooldown);
		nbt.putLong("dukeCombatAnalysisEndTime", dukeCombatAnalysisEndTime);
		// 序列化骑士能量相关变量
		nbt.putDouble("riderEnergy", riderEnergy);
		nbt.putDouble("maxRiderEnergy", maxRiderEnergy);
		nbt.putDouble("riderEnergyGainPerDamage", riderEnergyGainPerDamage);
		nbt.putDouble("riderEnergyNaturalRegen", riderEnergyNaturalRegen);
		nbt.putBoolean("isRiderEnergyFull", isRiderEnergyFull);
		nbt.putLong("lastEnergyFullTime", lastEnergyFullTime);
		// 序列化累积攻击力相关变量
		nbt.putDouble("accumulatedAttackDamage", accumulatedAttackDamage);
		nbt.putDouble("accumulatedAttackDamageModifier", accumulatedAttackDamageModifier);
        
		// 序列化基础巴隆技能相关变量
		nbt.putLong("baron_banana_energy_cooldown", baron_banana_energy_cooldown);
		// 序列化Overlord状态
		nbt.putBoolean("isOverlord", isOverlord);
		// 序列化Giifu状态
		nbt.putBoolean("isGiifu", isGiifu);
		// 序列化腰带移除相关变量
		nbt.putLong("beltRemovedTime", beltRemovedTime);
		nbt.putString("removedBeltType", removedBeltType);
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

		// 反序列化黑暗Kiva相关变量
		dark_kiva_bat_mode = nbt.getBoolean("dark_kiva_bat_mode");
		dark_kiva_bat_mode_time = nbt.getLong("dark_kiva_bat_mode_time");
		dark_kiva_blood_suck_active = nbt.getBoolean("dark_kiva_blood_suck_active");
		dark_kiva_blood_suck_cooldown = nbt.getLong("dark_kiva_blood_suck_cooldown");
		dark_kiva_sonic_blast_active = nbt.getBoolean("dark_kiva_sonic_blast_active");
		dark_kiva_sonic_blast_cooldown = nbt.getLong("dark_kiva_sonic_blast_cooldown");
		dark_kiva_blood_steal_cooldown = nbt.getLong("dark_kiva_blood_steal_cooldown");
		dark_kiva_fuuin_kekkai_cooldown = nbt.getLong("dark_kiva_fuuin_kekkai_cooldown");
		dark_kiva_fuuin_kekkai_active = nbt.getBoolean("dark_kiva_fuuin_kekkai_active");
        
		// 眼魂
		isMegaUiorderTransformed = nbt.getBoolean("isMegaUiorderTransformed"); // 新增：读取Mega_uiorder变身状态
		isNecromStandby = nbt.getBoolean("isNecromStandby"); // 新增：读取Necrom待机状态

		isEvilBatsTransformed = nbt.getBoolean("isEvilBatsTransformed"); // 新增：读取EvilBats变身状态
		isEvilBatsStealthed = nbt.getBoolean("isEvilBatsStealthed"); // 新增：读取EvilBats隐密模式状态
		isDarkKivaBeltEquipped = nbt.getBoolean("isDarkKivaBeltEquipped"); // 新增字段：读取是否装备了黑暗Kiva腰带
		isGenesisDriverEquipped = nbt.getBoolean("isGenesisDriverEquipped"); // 新增字段：读取是否装备了Genesis驱动器
		isSengokuDriverEmptyEquipped = nbt.getBoolean("isSengokuDriverEmptyEquipped"); // 新增字段：读取是否装备了空的Sengoku驱动器
		isSengokuDriverFilledEquipped = nbt.getBoolean("isSengokuDriverFilledEquipped"); // 新增字段：读取是否装备了有锁种的Sengoku驱动器
		isOrochiDriverEquipped = nbt.getBoolean("isOrochiDriverEquipped"); // 新增字段：读取是否装备了Orochi驱动器
		isDarkOrangelsTransformed = nbt.getBoolean("isDarkOrangelsTransformed"); // 新增字段：读取是否装备了Dark_orangels盔甲并变身

		baseMaxHealth = nbt.getDouble("baseMaxHealth");  // ← 新增
		// Duke骑士技能相关变量
		dukeKnightSummoned = nbt.getBoolean("dukeKnightSummoned");
		dukeKnightCooldown = nbt.getLong("dukeKnightCooldown");
		dukeKnightCost = nbt.contains("dukeKnightCost") ? nbt.getInt("dukeKnightCost") : 5; // 默认值5
		// Duke玩家战斗数据分析技能相关变量
		isDukeCombatAnalysisActive = nbt.getBoolean("isDukeCombatAnalysisActive");
		dukeCombatAnalysisCooldown = nbt.getLong("dukeCombatAnalysisCooldown");
		dukeCombatAnalysisEndTime = nbt.getLong("dukeCombatAnalysisEndTime");
		// 反序列化骑士能量和累积攻击力相关变量
		riderEnergy = nbt.getDouble("riderEnergy");
		maxRiderEnergy = nbt.getDouble("maxRiderEnergy");
		accumulatedAttackDamage = nbt.getDouble("accumulatedAttackDamage");
		accumulatedAttackDamageModifier = nbt.getDouble("accumulatedAttackDamageModifier");
        
		// 反序列化基础巴隆技能相关变量
		baron_banana_energy_cooldown = nbt.contains("baron_banana_energy_cooldown") ? nbt.getLong("baron_banana_energy_cooldown") : 0L;
		// 反序列化Overlord状态
		isOverlord = nbt.contains("isOverlord") ? nbt.getBoolean("isOverlord") : false;
		// 反序列化Giifu状态
		isGiifu = nbt.contains("isGiifu") ? nbt.getBoolean("isGiifu") : false;
		// 反序列化腰带移除相关变量
		beltRemovedTime = nbt.contains("beltRemovedTime") ? nbt.getLong("beltRemovedTime") : 0L;
		removedBeltType = nbt.contains("removedBeltType") ? nbt.getString("removedBeltType") : "";
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
					// 添加火龙果变量同步
					variables.dragonfruit_ready = message.data.dragonfruit_ready;
					variables.dragonfruit_ready_time = message.data.dragonfruit_ready_time;

					variables.baseMaxHealth = message.data.baseMaxHealth;
					variables.lastCustomArmorCount = message.data.lastCustomArmorCount; // 添加这行，同步lastCustomArmorCount
					variables.isDarkKivaBeltEquipped = message.data.isDarkKivaBeltEquipped; // 添加这行，同步黑暗Kiva腰带状态
					variables.isGenesisDriverEquipped = message.data.isGenesisDriverEquipped; // 添加这行，同步Genesis驱动器状态
					variables.isSengokuDriverEmptyEquipped = message.data.isSengokuDriverEmptyEquipped; // 添加这行，同步空的Sengoku驱动器状态
					variables.isSengokuDriverFilledEquipped = message.data.isSengokuDriverFilledEquipped; // 添加这行，同步有锁种的Sengoku驱动器状态
					variables.isOrochiDriverEquipped = message.data.isOrochiDriverEquipped; // 添加这行，同步Orochi驱动器状态
					variables.isEvilBatsTransformed = message.data.isEvilBatsTransformed; // 添加这行，同步EvilBats变身状态
					variables.isEvilBatsStealthed = message.data.isEvilBatsStealthed; // 添加这行，同步EvilBats隐密模式状态
					variables.isDarkOrangelsTransformed = message.data.isDarkOrangelsTransformed; // 添加这行，同步Dark_orangels变身状态

					// 同步所有骑士能量相关变量
					variables.riderEnergy = message.data.riderEnergy;
					variables.maxRiderEnergy = message.data.maxRiderEnergy;
					variables.riderEnergyGainPerDamage = message.data.riderEnergyGainPerDamage;
					variables.riderEnergyNaturalRegen = message.data.riderEnergyNaturalRegen;
					variables.isRiderEnergyFull = message.data.isRiderEnergyFull;
					variables.lastEnergyFullTime = message.data.lastEnergyFullTime;
					// 同步累积攻击力相关变量
					variables.accumulatedAttackDamage = message.data.accumulatedAttackDamage;
					variables.accumulatedAttackDamageModifier = message.data.accumulatedAttackDamageModifier;
                    
                    // 同步基础巴隆技能相关变量
                    variables.baron_banana_energy_cooldown = message.data.baron_banana_energy_cooldown;
                    // 同步Overlord状态
                    variables.isOverlord = message.data.isOverlord;
                    // 同步Giifu状态
                    variables.isGiifu = message.data.isGiifu;
                    // 同步腰带移除相关变量
                    variables.beltRemovedTime = message.data.beltRemovedTime;
                    variables.removedBeltType = message.data.removedBeltType;
				}
			});
			context.setPacketHandled(true);
		}
	}
}

