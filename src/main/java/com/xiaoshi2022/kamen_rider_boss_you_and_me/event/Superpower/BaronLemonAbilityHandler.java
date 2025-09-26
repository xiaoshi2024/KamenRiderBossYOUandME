package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BaronLemonEnergyEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * 假面骑士巴隆·柠檬形态能力处理器
 * 处理骑士2技能：当玩家手持音速弓柠檬模式时，按下技能按键生成柠檬能量实体定住目标
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BaronLemonAbilityHandler {

    /* ====== 数值常量 ====== */
    private static final int LEMON_ENERGY_COOLDOWN = 20; // 技能冷却时间（tick）
    private static final int LEMON_ENERGY_DURATION = 100; // 柠檬能量实体持续时间（tick）
    private static final float LEMON_ENERGY_RANGE = 2.0f; // 柠檬能量实体范围（2x2）
    private static final double ENERGY_COST = 20.0; // 技能消耗的骑士能量

    /* ====== 主 Tick：冷却管理 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        
        // 技能冷却管理
        if (sp.getPersistentData().contains("baron_lemon_ability_cooldown")) {
            int cooldown = sp.getPersistentData().getInt("baron_lemon_ability_cooldown");
            if (cooldown > 0) {
                sp.getPersistentData().putInt("baron_lemon_ability_cooldown", cooldown - 1);
            } else {
                sp.getPersistentData().remove("baron_lemon_ability_cooldown");
            }
        }
    }

    /* ====== 能力触发方法（可被网络包直接调用） ====== */
    
    /**
     * 骑士2技能：柠檬能量陷阱
     * 当玩家变身巴隆柠檬形态且手持音速弓柠檬模式时，按下技能按键在视线方向生成柠檬能量实体
     */
    public static void tryLemonEnergyTrap(ServerPlayer sp) {
        // 检查是否为巴隆柠檬形态
        if (!isBaronLemonForm(sp)) return;
        
        // 检查是否手持音速弓柠檬模式
        ItemStack mainHandItem = sp.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof sonicarrow)) return;
        
        sonicarrow sonicArrow = (sonicarrow) mainHandItem.getItem();
        if (sonicArrow.getCurrentMode(mainHandItem) != sonicarrow.Mode.LEMON) return;
        
        // 检查冷却
        if (sp.getPersistentData().contains("baron_lemon_ability_cooldown")) return;
        
        // 检查并消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, ENERGY_COST)) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)ENERGY_COST + " 点能量"));
            return;
        }
        
        // 射线检测，获取视线方向的目标位置（类似香蕉能量技能的实现）
        HitResult hitResult = sp.pick(20.0D, 0.0F, false);
        
        // 计算实体生成位置
        double spawnX, spawnY, spawnZ;
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 如果射线击中方块，实体生成在方块位置上方
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            spawnX = blockPos.getX() + 0.5;
            spawnY = blockPos.getY() + 1.0;
            spawnZ = blockPos.getZ() + 0.5;
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            // 如果射线击中实体，在实体位置生成
            Entity targetEntity = ((EntityHitResult) hitResult).getEntity();
            spawnX = targetEntity.getX();
            spawnY = targetEntity.getY() + 0.5;
            spawnZ = targetEntity.getZ();
        } else {
            // 如果没有击中任何东西，实体生成在视线方向的最远端
            Vec3 lookVec = sp.getViewVector(1.0F);
            spawnX = sp.getX() + lookVec.x * 20.0;
            spawnY = sp.getY() + sp.getEyeHeight() + lookVec.y * 20.0;
            spawnZ = sp.getZ() + lookVec.z * 20.0;
        }
        
        // 在目标位置生成柠檬能量实体
        spawnLemonEnergyEntitiesAtPosition(sp, spawnX, spawnY, spawnZ);
        
        // 设置冷却
        sp.getPersistentData().putInt("baron_lemon_ability_cooldown", LEMON_ENERGY_COOLDOWN);
        
        // 播放音效
        play(sp, SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.2F);
    }

    /* ====== 工具方法 ====== */
    
    /** 检查玩家是否穿戴巴隆柠檬形态盔甲 */
    private static boolean isBaronLemonForm(ServerPlayer sp) {
        // 检查是否穿戴巴隆柠檬形态的盔甲（与客户端KeyBinding.java保持一致的判断逻辑）
        return sp.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof baron_lemonItem &&
               sp.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof baron_lemonItem &&
               sp.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof baron_lemonItem;
    }
    
    /** 在目标周围生成2x2范围的柠檬能量实体 */
    private static void spawnLemonEnergyEntities(ServerPlayer sp, LivingEntity target) {
        // 调用新的方法，传入目标实体的位置
        spawnLemonEnergyEntitiesAtPosition(sp, target.getX(), target.getY() + 0.5, target.getZ());
    }
    
    /** 在指定位置生成单个柠檬能量实体 */
    private static void spawnLemonEnergyEntitiesAtPosition(ServerPlayer sp, double centerX, double centerY, double centerZ) {
        Level level = sp.level();
        UUID ownerUUID = sp.getUUID();
        
        // 创建并添加单个柠檬能量实体
        BaronLemonEnergyEntity energyEntity = ModEntityTypes.BARON_LEMON_ENERGY.get().create(level);
        if (energyEntity != null) {
            energyEntity.moveTo(centerX, centerY, centerZ);
            energyEntity.setOwnerId(ownerUUID);
            energyEntity.setDuration(LEMON_ENERGY_DURATION);
            level.addFreshEntity(energyEntity);
        }
    }
    
    /** 播放音效 */
    private static void play(ServerPlayer sp, SoundEvent sound, float volume, float pitch) {
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
    
    /* 禁止实例化 */
    private BaronLemonAbilityHandler() {}
    
}