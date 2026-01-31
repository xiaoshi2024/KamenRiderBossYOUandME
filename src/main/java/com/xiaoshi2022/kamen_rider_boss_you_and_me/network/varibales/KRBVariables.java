package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
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
				clone.kick = original.kick;
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
			clone.kick = false;
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
		clone.isGhostEye = false; // 玩家死亡时重置眼魔状态
		clone.isFangBloodline = false; // 玩家死亡时重置牙血鬼血脉状态
		clone.isRoidmude = false; // 玩家死亡时重置Roidmude状态
		clone.isDarkGhostTransformed = false; // 玩家死亡时重置黑暗Ghost变身状态
		clone.isDarkGhostActive = false; // 玩家死亡时重置黑暗Ghost激活状态
		clone.isNapoleonGhostTransformed = false; // 玩家死亡时重置拿破仑魂变身状态
		clone.darkGhostMaxHealth = 50.0D; // 玩家死亡时重置黑暗幽灵骑士最大生命值
		clone.isNecromStandby = false; // 玩家死亡时重置眼魂待命状态
		clone.isMegaUiorderTransformed = false; // 玩家死亡时重置手环变身状态
		clone.isNecromTemporaryRemoved = false; // 玩家死亡时重置眼魂临时移除状态
		clone.isBrainDriverEquipped = false; // 玩家死亡时重置BrainDriver装备状态
			clone.isBrainTransformed = false; // 玩家死亡时重置Brain变身状态
			clone.isKnightInvokerEquipped = false; // 玩家死亡时重置KnightInvokerBuckle装备状态
			clone.isWeekEndriverEquipped = false; // 玩家死亡时重置WeekEndriver装备状态
			clone.queenBee_ready = false; // 玩家死亡时重置Queen Bee准备状态
			clone.queenBee_ready_time = 0L; // 玩家死亡时重置Queen Bee准备时间
			clone.isQueenBeeTransformed = false; // 玩家死亡时重置Queen Bee变身状态
			// 修改：玩家死亡后恢复为人类
			clone.isGiifu = false;
			// 保留baseMaxHealth值，不再强制重置为默认值，以保留通过命令设置的生命值
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
	public boolean kick = false;
	public boolean needExplode = false;
	public boolean wudi = false;
	public boolean hasExploded = false;   // ← 新增
	public boolean allowKickBlockDamage = false; // 控制骑士踢是否允许破坏方块，默认为false
	public long lastKickTime = 0L;
	public long kickStartTime = 0L;
	public double kickStartY = 0.0D;
	public double kickInitialVelocity = 0.0D;

	//眼魂相关
	public boolean isMegaUiorderTransformed = false; // 新增字段：记录是否装备了Mega_uiorder并变身
	public boolean isNecromStandby = false;
	public boolean isNecromTemporaryRemoved = false; // 临时取下眼魂标记

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
    public double dark_kiva_bat_mode_takeoff_height = 0.0D; // 蝙蝠形态起飞高度
    public long dark_kiva_bat_mode_cooldown = 0L; // 蝙蝠形态冷却时间
    public boolean dark_kiva_blood_suck_active = false; // 吸血能力激活状态
    public long dark_kiva_blood_suck_cooldown = 0L; // 吸血能力冷却时间
    public boolean dark_kiva_sonic_blast_active = false; // 声波爆破激活状态
    public long dark_kiva_sonic_blast_cooldown = 0L; // 声波爆破冷却时间
    public long dark_kiva_blood_steal_cooldown = 0L; // 生命偷取冷却时间
    // 封印结界（Fuuin Kekkai）技能相关变量
    public boolean dark_kiva_fuuin_kekkai_active = false; // 封印结界激活状态
    public long dark_kiva_fuuin_kekkai_cooldown = 0L; // 封印结界冷却时间
    public String currentFlightController = null; // 当前飞行控制器（用于防止不同盔甲的飞行能力冲突）

	// 在PlayerVariables类中添加字段
	public double baseMaxHealth = 20.0D; // 默认基础生命值为20点
	public int lastCustomArmorCount = 0;
	public boolean isDarkKivaBeltEquipped = false; // 新增字段：记录是否装备了黑暗Kiva腰带
	public boolean isGenesisDriverEquipped = false; // 新增字段：记录是否装备了Genesis驱动器
	public boolean isSengokuDriverEmptyEquipped = false; // 新增字段：记录是否装备了空的Sengoku驱动器
	public boolean isSengokuDriverFilledEquipped = false; // 新增字段：记录是否装备了有锁种的Sengoku驱动器
	public boolean isOrochiDriverEquipped = false; // 新增字段：记录是否装备了Orochi驱动器
	public boolean isGhostDriverEquipped = false; // 新增字段：记录是否装备了Ghost驱动器
	public boolean isGhostEye = false; // 新增字段：记录玩家是否是眼魔
	public boolean isEvilBatsTransformed = false; // 新增字段：记录是否装备了EvilBats盔甲并变身
	public boolean isEvilBatsStealthed = false; // 新增字段：记录是否处于EvilBats隐密模式
	public boolean isDarkOrangelsTransformed = false; // 新增字段：记录是否装备了Dark_orangels盔甲并变身
	public boolean isDarkGhostTransformed = false; // 新增字段：记录是否装备了黑暗Ghost盔甲并变身
	public boolean isDarkGhostActive = false; // 新增字段：记录是否处于Dark Ghost激活状态
	public double darkGhostMaxHealth = 50.0D; // 黑暗幽灵骑士的最大生命值上限
	public boolean isNapoleonGhostTransformed = false; // 新增字段：记录是否装备了拿破仑魂盔甲并变身
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
	// 玩家是否为牙血鬼血脉状态
	public boolean isFangBloodline = false;
	// 玩家是否为机械变异体（Roidmude）状态
	public boolean isRoidmude = false;
	// 机械变异体（Roidmude）类型
	public String roidmudeType = "plain";
	// 机械变异体（Roidmude）编号
	public int roidmudeNumber = 0;
	// 机械变异体（Roidmude）是否已进化
	public boolean isRoidmudeEvolved = false;
	// 玩家是否已获得眼魔世界宝箱
	public boolean hasReceivedGhostEyeLootChest = false;
	// 巴隆香蕉能量技能冷却时间
	public long baron_banana_energy_cooldown = 0L;
	// Queen Bee技能相关变量
	public long quinbee_flight_cooldown = 0L; // 飞行技能冷却时间
	public long quinbee_flight_end_time = 0L; // 飞行结束时间
	public long quinbee_needle_kunai_cooldown = 0L; // 针刺苦无冷却时间
	// 保存的玩家原版盔甲数据
	public net.minecraft.nbt.ListTag originalEvilArmor = null;
	public net.minecraft.nbt.ListTag originalRiderNecromArmor = null;
	public net.minecraft.nbt.ListTag originalDarkKivaArmor = null;
	public net.minecraft.nbt.ListTag originalNapoleonGhostArmor = null; // 新增字段：保存玩家原版拿破仑魂盔甲数据
	public net.minecraft.nbt.ListTag originalBrainArmor = null; // 新增字段：保存玩家原版Brain盔甲数据
	public boolean isBrainDriverEquipped = false; // 新增字段：记录是否装备了BrainDriver腰带
	public boolean isBrainTransformed = false; // 新增字段：记录是否变身为Brain形态
	public boolean isKnightInvokerEquipped = false; // 新增字段：记录是否装备了KnightInvokerBuckle
	public boolean isWeekEndriverEquipped = false; // 新增字段：记录是否装备了WeekEndriver腰带
public boolean queenBee_ready = false; // 新增字段：记录是否准备好变身为Queen Bee形态
public long queenBee_ready_time = 0L; // 新增字段：记录Queen Bee准备时间
public boolean isQueenBeeTransformed = false; // 新增字段：记录是否变身为Queen Bee形态

	public void syncPlayerVariables(Entity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			// 检查是否混合了超过一个种族
			int activeRaces = 0;
			if (isOverlord) activeRaces++;
			if (isGiifu) activeRaces++;
			if (isFangBloodline) activeRaces++;
			if (isGhostEye) activeRaces++;
			if (isRoidmude) activeRaces++;
			
			// 如果混合了超过一个种族，触发成就
			if (activeRaces > 1) {
				com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.MixedRaceTrigger.getInstance().trigger(serverPlayer);
			}
			
			// 检查玩家是否已断开连接
			kamen_rider_boss_you_and_me.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
		}
	}

	public Tag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("kick", kick);
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
		nbt.putDouble("dark_kiva_bat_mode_takeoff_height", dark_kiva_bat_mode_takeoff_height);
		nbt.putLong("dark_kiva_bat_mode_cooldown", dark_kiva_bat_mode_cooldown);
		nbt.putBoolean("dark_kiva_blood_suck_active", dark_kiva_blood_suck_active);
		nbt.putLong("dark_kiva_fuuin_kekkai_cooldown", dark_kiva_fuuin_kekkai_cooldown);
		nbt.putBoolean("dark_kiva_fuuin_kekkai_active", dark_kiva_fuuin_kekkai_active);
		nbt.putLong("dark_kiva_blood_suck_cooldown", dark_kiva_blood_suck_cooldown);
		nbt.putBoolean("dark_kiva_sonic_blast_active", dark_kiva_sonic_blast_active);
		nbt.putLong("dark_kiva_sonic_blast_cooldown", dark_kiva_sonic_blast_cooldown);
		nbt.putLong("dark_kiva_blood_steal_cooldown", dark_kiva_blood_steal_cooldown);

		// 眼魂
		nbt.putBoolean("isMegaUiorderTransformed", isMegaUiorderTransformed); // 新增：序列化Mega_uiorder变身状态
		nbt.putBoolean("isNecromStandby", isNecromStandby); // 新增：序列化Necrom待机状态
		nbt.putBoolean("isNecromTemporaryRemoved", isNecromTemporaryRemoved); // 序列化临时取下眼魂标记
		nbt.putBoolean("isGhostEye", isGhostEye); // 新增：序列化眼魔状态

		nbt.putBoolean("isEvilBatsTransformed", isEvilBatsTransformed); // 新增：存储EvilBats变身状态
		nbt.putBoolean("isEvilBatsStealthed", isEvilBatsStealthed); // 新增：存储EvilBats隐密模式状态
		nbt.putBoolean("isDarkKivaBeltEquipped", isDarkKivaBeltEquipped); // 新增字段：记录是否装备了黑暗Kiva腰带
		nbt.putBoolean("isGenesisDriverEquipped", isGenesisDriverEquipped); // 新增字段：记录是否装备了Genesis驱动器
		nbt.putString("currentFlightController", currentFlightController == null ? "" : currentFlightController); // 新增：存储当前飞行控制器
		nbt.putBoolean("isSengokuDriverEmptyEquipped", isSengokuDriverEmptyEquipped); // 新增字段：记录是否装备了空的Sengoku驱动器
		nbt.putBoolean("isSengokuDriverFilledEquipped", isSengokuDriverFilledEquipped); // 新增字段：记录是否装备了有锁种的Sengoku驱动器
		nbt.putBoolean("isOrochiDriverEquipped", isOrochiDriverEquipped); // 新增字段：记录是否装备了Orochi驱动器
		nbt.putBoolean("isDarkOrangelsTransformed", isDarkOrangelsTransformed); // 新增字段：记录是否装备了Dark_orangels盔甲并变身
		nbt.putBoolean("isDarkGhostTransformed", isDarkGhostTransformed); // 新增字段：记录是否装备了黑暗Ghost盔甲并变身
		nbt.putBoolean("isDarkGhostActive", isDarkGhostActive); // 新增字段：记录是否处于Dark Ghost激活状态
		nbt.putBoolean("isNapoleonGhostTransformed", isNapoleonGhostTransformed); // 新增字段：记录是否装备了拿破仑魂盔甲并变身
		nbt.putDouble("darkGhostMaxHealth", darkGhostMaxHealth); // 黑暗幽灵骑士的最大生命值上限

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
		// 序列化Queen Bee技能相关变量
		nbt.putLong("quinbee_flight_cooldown", quinbee_flight_cooldown);
		nbt.putLong("quinbee_flight_end_time", quinbee_flight_end_time);
		nbt.putLong("quinbee_needle_kunai_cooldown", quinbee_needle_kunai_cooldown);
		// 序列化Overlord状态
		nbt.putBoolean("isOverlord", isOverlord);
		// 序列化Giifu状态
		nbt.putBoolean("isGiifu", isGiifu);
		// 序列化牙血鬼血脉状态
		nbt.putBoolean("isFangBloodline", isFangBloodline);
		// 序列化Roidmude状态
		nbt.putBoolean("isRoidmude", isRoidmude);
		nbt.putString("roidmudeType", roidmudeType);
		nbt.putInt("roidmudeNumber", roidmudeNumber);
		nbt.putBoolean("isRoidmudeEvolved", isRoidmudeEvolved);
		// 序列化宝箱领取状态
		nbt.putBoolean("hasReceivedGhostEyeLootChest", hasReceivedGhostEyeLootChest);
		// 序列化基础最大生命值，确保通过命令设置的生命值能够持久化保存
		nbt.putDouble("baseMaxHealth", baseMaxHealth);
		// 序列化腰带移除相关变量
		nbt.putLong("beltRemovedTime", beltRemovedTime);
		nbt.putString("removedBeltType", removedBeltType);
		// 序列化保存的玩家原版盔甲数据
		if (originalEvilArmor != null) {
			nbt.put("originalEvilArmor", originalEvilArmor);
		}
		if (originalRiderNecromArmor != null) {
			nbt.put("originalRiderNecromArmor", originalRiderNecromArmor);
		}
		if (originalDarkKivaArmor != null) {
			nbt.put("originalDarkKivaArmor", originalDarkKivaArmor);
		}
		if (originalNapoleonGhostArmor != null) {
			nbt.put("originalNapoleonGhostArmor", originalNapoleonGhostArmor);
		}
		if (originalBrainArmor != null) {
			nbt.put("originalBrainArmor", originalBrainArmor);
		}
		nbt.putBoolean("isBrainDriverEquipped", isBrainDriverEquipped);
	nbt.putBoolean("isBrainTransformed", isBrainTransformed);
nbt.putBoolean("isKnightInvokerEquipped", isKnightInvokerEquipped);
nbt.putBoolean("isWeekEndriverEquipped", isWeekEndriverEquipped);
nbt.putBoolean("queenBee_ready", queenBee_ready);
nbt.putLong("queenBee_ready_time", queenBee_ready_time);
nbt.putBoolean("isQueenBeeTransformed", isQueenBeeTransformed);
		return nbt;
	}

	public void readNBT(Tag tag) {
		if (tag == null) {
			return;
		}
		CompoundTag nbt = (CompoundTag) tag;
		kick = nbt.getBoolean("kick");
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
		dark_kiva_bat_mode_takeoff_height = nbt.getDouble("dark_kiva_bat_mode_takeoff_height");
		dark_kiva_bat_mode_cooldown = nbt.getLong("dark_kiva_bat_mode_cooldown");
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
		isNecromTemporaryRemoved = nbt.getBoolean("isNecromTemporaryRemoved"); // 读取临时取下眼魂标记
		isGhostEye = nbt.getBoolean("isGhostEye"); // 新增：读取眼魔状态

		isEvilBatsTransformed = nbt.getBoolean("isEvilBatsTransformed"); // 新增：读取EvilBats变身状态
		isEvilBatsStealthed = nbt.getBoolean("isEvilBatsStealthed"); // 新增：读取EvilBats隐密模式状态
		isDarkKivaBeltEquipped = nbt.getBoolean("isDarkKivaBeltEquipped"); // 新增字段：读取是否装备了黑暗Kiva腰带
		isGenesisDriverEquipped = nbt.getBoolean("isGenesisDriverEquipped"); // 新增字段：读取是否装备了Genesis驱动器
		currentFlightController = nbt.contains("currentFlightController") && !nbt.getString("currentFlightController").isEmpty() ? nbt.getString("currentFlightController") : null; // 新增：读取当前飞行控制器
		isSengokuDriverEmptyEquipped = nbt.getBoolean("isSengokuDriverEmptyEquipped"); // 新增字段：读取是否装备了空的Sengoku驱动器
		isSengokuDriverFilledEquipped = nbt.getBoolean("isSengokuDriverFilledEquipped"); // 新增字段：读取是否装备了有锁种的Sengoku驱动器
		isOrochiDriverEquipped = nbt.getBoolean("isOrochiDriverEquipped"); // 新增字段：读取是否装备了Orochi驱动器
		isDarkOrangelsTransformed = nbt.getBoolean("isDarkOrangelsTransformed"); // 新增字段：读取是否装备了Dark_orangels盔甲并变身
		isDarkGhostTransformed = nbt.getBoolean("isDarkGhostTransformed"); // 新增字段：记录是否装备了黑暗Ghost盔甲并变身
		isDarkGhostActive = nbt.getBoolean("isDarkGhostActive"); // 新增字段：记录是否处于Dark Ghost激活状态
		isNapoleonGhostTransformed = nbt.contains("isNapoleonGhostTransformed") ? nbt.getBoolean("isNapoleonGhostTransformed") : false; // 新增字段：读取是否装备了拿破仑魂盔甲并变身
		darkGhostMaxHealth = nbt.contains("darkGhostMaxHealth") ? nbt.getDouble("darkGhostMaxHealth") : 50.0D; // 黑暗幽灵骑士的最大生命值上限

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
		// 反序列化Queen Bee技能相关变量
		quinbee_flight_cooldown = nbt.contains("quinbee_flight_cooldown") ? nbt.getLong("quinbee_flight_cooldown") : 0L;
		quinbee_flight_end_time = nbt.contains("quinbee_flight_end_time") ? nbt.getLong("quinbee_flight_end_time") : 0L;
		quinbee_needle_kunai_cooldown = nbt.contains("quinbee_needle_kunai_cooldown") ? nbt.getLong("quinbee_needle_kunai_cooldown") : 0L;
		// 反序列化Overlord状态
		isOverlord = nbt.contains("isOverlord") ? nbt.getBoolean("isOverlord") : false;
		// 反序列化Giifu状态
		isGiifu = nbt.contains("isGiifu") ? nbt.getBoolean("isGiifu") : false;
		// 反序列化牙血鬼血脉状态
		isFangBloodline = nbt.contains("isFangBloodline") ? nbt.getBoolean("isFangBloodline") : false;
		// 反序列化Roidmude状态
		isRoidmude = nbt.contains("isRoidmude") ? nbt.getBoolean("isRoidmude") : false;
		roidmudeType = nbt.contains("roidmudeType") ? nbt.getString("roidmudeType") : "plain";
		roidmudeNumber = nbt.contains("roidmudeNumber") ? nbt.getInt("roidmudeNumber") : 0;
		isRoidmudeEvolved = nbt.contains("isRoidmudeEvolved") ? nbt.getBoolean("isRoidmudeEvolved") : false;
		// 反序列化宝箱领取状态
		hasReceivedGhostEyeLootChest = nbt.contains("hasReceivedGhostEyeLootChest") ? nbt.getBoolean("hasReceivedGhostEyeLootChest") : false;
		// 反序列化腰带移除相关变量
		beltRemovedTime = nbt.contains("beltRemovedTime") ? nbt.getLong("beltRemovedTime") : 0L;
		removedBeltType = nbt.contains("removedBeltType") ? nbt.getString("removedBeltType") : "";
		// 反序列化保存的玩家原版盔甲数据
		if (nbt.contains("originalEvilArmor")) {
			originalEvilArmor = nbt.getList("originalEvilArmor", net.minecraft.nbt.Tag.TAG_COMPOUND);
		} else {
			originalEvilArmor = null;
		}
		if (nbt.contains("originalRiderNecromArmor")) {
			originalRiderNecromArmor = nbt.getList("originalRiderNecromArmor", net.minecraft.nbt.Tag.TAG_COMPOUND);
		} else {
			originalRiderNecromArmor = null;
		}
		if (nbt.contains("originalDarkKivaArmor")) {
			originalDarkKivaArmor = nbt.getList("originalDarkKivaArmor", net.minecraft.nbt.Tag.TAG_COMPOUND);
		} else {
			originalDarkKivaArmor = null;
		}
		if (nbt.contains("originalNapoleonGhostArmor")) {
			originalNapoleonGhostArmor = nbt.getList("originalNapoleonGhostArmor", net.minecraft.nbt.Tag.TAG_COMPOUND);
		} else {
			originalNapoleonGhostArmor = null;
		}
		if (nbt.contains("originalBrainArmor")) {
			originalBrainArmor = nbt.getList("originalBrainArmor", net.minecraft.nbt.Tag.TAG_COMPOUND);
		} else {
			originalBrainArmor = null;
		}
		isBrainDriverEquipped = nbt.contains("isBrainDriverEquipped") ? nbt.getBoolean("isBrainDriverEquipped") : false;
	isBrainTransformed = nbt.contains("isBrainTransformed") ? nbt.getBoolean("isBrainTransformed") : false;
isKnightInvokerEquipped = nbt.contains("isKnightInvokerEquipped") ? nbt.getBoolean("isKnightInvokerEquipped") : false;
isWeekEndriverEquipped = nbt.contains("isWeekEndriverEquipped") ? nbt.getBoolean("isWeekEndriverEquipped") : false;
queenBee_ready = nbt.contains("queenBee_ready") ? nbt.getBoolean("queenBee_ready") : false;
queenBee_ready_time = nbt.contains("queenBee_ready_time") ? nbt.getLong("queenBee_ready_time") : 0L;
isQueenBeeTransformed = nbt.contains("isQueenBeeTransformed") ? nbt.getBoolean("isQueenBeeTransformed") : false;
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
					// 使用DistExecutor确保只在客户端执行客户端代码，避免服务器加载客户端类
					net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
						// 客户端代码，只会在客户端执行，且只在运行时加载
						ClientHandler.handlePlayerVariablesSync(message);
					});
				}
			});
			context.setPacketHandled(true);
		}
		
		// 客户端专用处理类，服务器不会加载这个内部类
		private static class ClientHandler {
			private static void handlePlayerVariablesSync(PlayerVariablesSyncMessage message) {
				// 这里可以安全使用客户端专属类，因为整个类只在客户端加载
				net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
				if (player != null) {
					PlayerVariables variables = ((PlayerVariables) player.getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
					variables.kick = message.data.kick;
					variables.wudi = message.data.wudi;
					variables.lastKickTime = message.data.lastKickTime;
					variables.kickStartTime = message.data.kickStartTime;
					variables.kickStartY = message.data.kickStartY;
					variables.needExplode = message.data.needExplode;

					//眼魂
					variables.isMegaUiorderTransformed = message.data.isMegaUiorderTransformed;
					variables.isNecromStandby = message.data.isNecromStandby;
					variables.isGhostDriverEquipped = message.data.isGhostDriverEquipped;

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
					variables.lastCustomArmorCount = message.data.lastCustomArmorCount;
					variables.isDarkKivaBeltEquipped = message.data.isDarkKivaBeltEquipped;
					variables.isGenesisDriverEquipped = message.data.isGenesisDriverEquipped;
					variables.isSengokuDriverEmptyEquipped = message.data.isSengokuDriverEmptyEquipped;
					variables.isSengokuDriverFilledEquipped = message.data.isSengokuDriverFilledEquipped;
					variables.isOrochiDriverEquipped = message.data.isOrochiDriverEquipped;
					variables.isEvilBatsTransformed = message.data.isEvilBatsTransformed;
					variables.isEvilBatsStealthed = message.data.isEvilBatsStealthed;
					variables.isDarkOrangelsTransformed = message.data.isDarkOrangelsTransformed;
					variables.isDarkGhostActive = message.data.isDarkGhostActive;
					variables.isNapoleonGhostTransformed = message.data.isNapoleonGhostTransformed;

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
									// 同步Queen Bee技能相关变量
									variables.quinbee_flight_cooldown = message.data.quinbee_flight_cooldown;
									variables.quinbee_flight_end_time = message.data.quinbee_flight_end_time;
									variables.quinbee_needle_kunai_cooldown = message.data.quinbee_needle_kunai_cooldown;
									// 同步Overlord状态
									variables.isOverlord = message.data.isOverlord;
									// 同步Giifu状态
									variables.isGiifu = message.data.isGiifu;
									// 同步牙血鬼血脉状态
									variables.isFangBloodline = message.data.isFangBloodline;
									// 同步Roidmude状态
									variables.isRoidmude = message.data.isRoidmude;
									variables.roidmudeType = message.data.roidmudeType;
									variables.roidmudeNumber = message.data.roidmudeNumber;
									variables.isRoidmudeEvolved = message.data.isRoidmudeEvolved;
									// 同步宝箱领取状态
									variables.hasReceivedGhostEyeLootChest = message.data.hasReceivedGhostEyeLootChest;
									// 同步腰带移除相关变量
									variables.beltRemovedTime = message.data.beltRemovedTime;
									variables.removedBeltType = message.data.removedBeltType;
									// 同步保存的玩家原版盔甲数据
									variables.originalEvilArmor = message.data.originalEvilArmor;
									variables.originalRiderNecromArmor = message.data.originalRiderNecromArmor;
									variables.originalDarkKivaArmor = message.data.originalDarkKivaArmor;
									variables.originalNapoleonGhostArmor = message.data.originalNapoleonGhostArmor;
						variables.originalBrainArmor = message.data.originalBrainArmor;
						variables.isBrainDriverEquipped = message.data.isBrainDriverEquipped;
						variables.isBrainTransformed = message.data.isBrainTransformed;
variables.isKnightInvokerEquipped = message.data.isKnightInvokerEquipped;
variables.isWeekEndriverEquipped = message.data.isWeekEndriverEquipped;
variables.queenBee_ready = message.data.queenBee_ready;
variables.queenBee_ready_time = message.data.queenBee_ready_time;
variables.isQueenBeeTransformed = message.data.isQueenBeeTransformed;
				}
			}
		}
	}
}