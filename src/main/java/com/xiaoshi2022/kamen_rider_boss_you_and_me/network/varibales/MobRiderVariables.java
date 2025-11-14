package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MobRiderVariables {
    // 创建能力实例
    public static final Capability<MobRiderData> MOB_RIDER_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<MobRiderData>() {});
    
    // 为非玩家生物附加能力
    public static class MobRiderVariablesProvider implements ICapabilitySerializable<Tag> {
        private final MobRiderData mobRiderData = new MobRiderData();
        private final LazyOptional<MobRiderData> instance = LazyOptional.of(() -> mobRiderData);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == MOB_RIDER_DATA_CAPABILITY ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public Tag serializeNBT() {
            return mobRiderData.writeNBT();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            mobRiderData.readNBT(nbt);
        }
    }
    
    @Mod.EventBusSubscriber
    public static class CapabilityAttachHandler {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Mob && !(event.getObject() instanceof net.minecraft.world.entity.player.Player)) {
                event.addCapability(new ResourceLocation("kamen_rider_boss_you_and_me", "mob_rider_data"), 
                        new MobRiderVariablesProvider());
            }
        }
    }
    
    // 非玩家生物的骑士数据类
    public static class MobRiderData {
        // 骑士能量相关变量
        public double riderEnergy = 0.0D; // 当前骑士能量
        public double maxRiderEnergy = 100.0D; // 最大骑士能量
        public double riderEnergyGainPerDamage = 0.1D; // 每造成1点伤害获得的能量
        public double riderEnergyNaturalRegen = 1.0D; // 自然恢复速率
        public boolean isRiderEnergyFull = false; // 能量是否已满
        public long lastEnergyFullTime = 0L; // 上次能量充满的时间
        
        // 技能冷却相关变量
        public long cherry_sonic_arrow_cooldown = 0L;
        public long baron_lemon_ability_cooldown = 0L;
        public long baron_banana_energy_cooldown = 0L;
        public long lemon_clone_cooldown = 0L; // 柠檬分身技能冷却
        
        // 技能状态相关变量
        public boolean isDarkGaimKickEnhance = false;
        public long darkGaimKickEnhanceExpiry = 0L;
        public boolean dark_kiva_bat_mode = false;
        public long dark_kiva_bat_mode_time = 0L;
        
        // 保存数据到NBT
        public CompoundTag writeNBT() {
            CompoundTag tag = new CompoundTag();
            
            // 保存骑士能量相关数据
            tag.putDouble("riderEnergy", riderEnergy);
            tag.putDouble("maxRiderEnergy", maxRiderEnergy);
            tag.putDouble("riderEnergyGainPerDamage", riderEnergyGainPerDamage);
            tag.putDouble("riderEnergyNaturalRegen", riderEnergyNaturalRegen);
            tag.putBoolean("isRiderEnergyFull", isRiderEnergyFull);
            tag.putLong("lastEnergyFullTime", lastEnergyFullTime);
            
            // 保存冷却时间数据
            tag.putLong("cherry_sonic_arrow_cooldown", cherry_sonic_arrow_cooldown);
            tag.putLong("baron_lemon_ability_cooldown", baron_lemon_ability_cooldown);
            tag.putLong("baron_banana_energy_cooldown", baron_banana_energy_cooldown);
            tag.putLong("lemon_clone_cooldown", lemon_clone_cooldown);
            
            // 保存技能状态数据
            tag.putBoolean("isDarkGaimKickEnhance", isDarkGaimKickEnhance);
            tag.putLong("darkGaimKickEnhanceExpiry", darkGaimKickEnhanceExpiry);
            tag.putBoolean("dark_kiva_bat_mode", dark_kiva_bat_mode);
            tag.putLong("dark_kiva_bat_mode_time", dark_kiva_bat_mode_time);
            
            return tag;
        }
        
        // 从NBT读取数据
        public void readNBT(Tag nbt) {
            if (nbt instanceof CompoundTag tag) {
                // 读取骑士能量相关数据
                riderEnergy = tag.getDouble("riderEnergy");
                maxRiderEnergy = tag.contains("maxRiderEnergy") ? tag.getDouble("maxRiderEnergy") : 100.0D;
                riderEnergyGainPerDamage = tag.contains("riderEnergyGainPerDamage") ? tag.getDouble("riderEnergyGainPerDamage") : 0.1D;
                riderEnergyNaturalRegen = tag.contains("riderEnergyNaturalRegen") ? tag.getDouble("riderEnergyNaturalRegen") : 1.0D;
                isRiderEnergyFull = tag.getBoolean("isRiderEnergyFull");
                lastEnergyFullTime = tag.getLong("lastEnergyFullTime");
                
                // 读取冷却时间数据
                cherry_sonic_arrow_cooldown = tag.getLong("cherry_sonic_arrow_cooldown");
                baron_lemon_ability_cooldown = tag.getLong("baron_lemon_ability_cooldown");
                baron_banana_energy_cooldown = tag.getLong("baron_banana_energy_cooldown");
                lemon_clone_cooldown = tag.contains("lemon_clone_cooldown") ? tag.getLong("lemon_clone_cooldown") : 0L;
                
                // 读取技能状态数据
                isDarkGaimKickEnhance = tag.getBoolean("isDarkGaimKickEnhance");
                darkGaimKickEnhanceExpiry = tag.getLong("darkGaimKickEnhanceExpiry");
                dark_kiva_bat_mode = tag.getBoolean("dark_kiva_bat_mode");
                dark_kiva_bat_mode_time = tag.getLong("dark_kiva_bat_mode_time");
            }
        }
    }
}