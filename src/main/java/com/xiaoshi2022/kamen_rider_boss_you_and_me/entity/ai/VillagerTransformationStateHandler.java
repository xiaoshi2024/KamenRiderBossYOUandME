package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.VillagerEquipDriverHandler.handleVillagerWithDriver;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class VillagerTransformationStateHandler {
    private static final Random random = new Random();
    private static final int TRANSFORMATION_COOLDOWN_TICKS = 20 * 10; // 10秒冷却时间
    private static final int DANGER_CHECK_RADIUS = 10; // 危险检测半径
    private static final int IDLE_CHECK_INTERVAL = 20 * 30; // 30秒检查一次是否可以解除变身
    
    // 闲置检测相关常量和数据结构
    private static final Map<Integer, IdleState> VILLAGER_IDLE_STATES = new HashMap<>();
    private static final int IDLE_THRESHOLD = 1200; // 60秒没有移动或战斗
    private static final int CHECK_INTERVAL = 20; // 每秒检查一次

    // 当村民受到伤害时触发（危险状态）
    @SubscribeEvent
    public static void onVillagerHurt(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (MCAUtil.isVillagerEntityMCA(entity)) {
            // 从Curio槽位或主手获取腰带
            BeltCurioIntegration.getBeltFromEntity(entity).ifPresent(beltStack -> {
                // 检查是否已经变身
                boolean isTransformed = !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                boolean isAlreadyMarked = entity.getPersistentData().getBoolean("is_transformed_ridder");
                
                // 只有在未变身后才触发变身逻辑
                if (!isTransformed || !isAlreadyMarked) {
                    // 危险时自动变身
                    handleVillagerWithDriver(entity, beltStack);
                }
            });
        }
    }

    // 当村民tick时检查状态
    @SubscribeEvent
    public static void onVillagerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (MCAUtil.isVillagerEntityMCA(entity) && !entity.level().isClientSide) {
            // 检查村民是否手持腰带，如果是则尝试装备
            ItemStack mainHandStack = entity.getMainHandItem();
            if (mainHandStack.getItem() instanceof Genesis_driver && !BeltCurioIntegration.hasBeltInCurioSlot(entity)) {
                BeltCurioIntegration.tryEquipBeltToCurioSlot(entity, mainHandStack);
            }
            
            // 处理腰带逻辑
            if (BeltCurioIntegration.hasBeltInCurioSlot(entity)) {
                BeltCurioIntegration.getBeltFromEntity(entity).ifPresent(beltStack -> {
                    // 检查是否处于危险状态
                    boolean isInDanger = isInDanger(entity);
                    // 检查是否已经变身
                    boolean isTransformed = !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                    boolean isAlreadyMarked = entity.getPersistentData().getBoolean("is_transformed_ridder");
                    
                    // 只有在危险状态且未变身后才触发变身逻辑
                    if (isInDanger && (!isTransformed || !isAlreadyMarked)) {
                        handleVillagerWithDriver(entity, beltStack);
                    }
                    
                    // 检查并处理闲置状态
                    checkAndHandleIdleState(entity, beltStack);
                });
            }
        }
    }
    
    // 使用Curio槽位的腰带检查并处理变身状态
    private static void checkAndHandleTransformationStateWithBelt(LivingEntity villager, ItemStack beltStack) {
        if (!(beltStack.getItem() instanceof Genesis_driver)) {
            return;
        }
        
        Genesis_driver driver = (Genesis_driver) beltStack.getItem();
        boolean isTransformed = !villager.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        boolean isInDanger = isInDanger(villager);

        if (isTransformed && !isInDanger) {
            // 安全状态且已变身，有几率解除变身
            if (random.nextFloat() < 0.3f) { // 30%几率解除变身
               revertTransformation(villager);
            }
        } else if (!isTransformed && isInDanger) {
            // 危险状态且未变身，自动变身
            handleVillagerWithDriver(villager, beltStack);
        }
    }

    // 检查并处理村民的变身状态
    private static void checkAndHandleTransformationState(LivingEntity villager) {
        // 检查村民是否手持腰带
        ItemStack mainHandStack = villager.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof Genesis_driver)) {
            return;
        }

        Genesis_driver driver = (Genesis_driver) mainHandStack.getItem();
        boolean isTransformed = !villager.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        boolean isInDanger = isInDanger(villager);

        if (isTransformed && !isInDanger) {
            // 安全状态且已变身，有几率解除变身
            if (random.nextFloat() < 0.3f) { // 30%几率解除变身
               revertTransformation(villager);
            }
        } else if (!isTransformed && isInDanger) {
            // 危险状态且未变身，自动变身
            handleVillagerWithDriver(villager, mainHandStack);
        }
    }

    // 检查村民是否处于危险状态
    private static boolean isInDanger(LivingEntity villager) {
        // 检查附近是否有敌对生物
        AABB searchBox = new AABB(
            villager.getX() - DANGER_CHECK_RADIUS,
            villager.getY() - DANGER_CHECK_RADIUS,
            villager.getZ() - DANGER_CHECK_RADIUS,
            villager.getX() + DANGER_CHECK_RADIUS,
            villager.getY() + DANGER_CHECK_RADIUS,
            villager.getZ() + DANGER_CHECK_RADIUS
        );

        List<LivingEntity> nearbyEnemies = villager.level().getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            entity -> entity instanceof Enemy && entity.isAlive()
        );

        // 如果附近有敌对生物，视为危险状态
        if (!nearbyEnemies.isEmpty()) {
            return true;
        }

        // 检查村民是否正在被追击或处于战斗状态
        // 检查村民是否有活跃的攻击目标或正在逃跑
        if (villager.getLastHurtByMob() != null || villager.getLastHurtMob() != null) {
            return true;
        }

        return false;
    }

    // 检查并处理村民的闲置状态，当闲置时间过长时自动解除变身
    private static void checkAndHandleIdleState(LivingEntity villager, ItemStack beltStack) {
        // 只在每CHECK_INTERVAL个tick检查一次
        if (villager.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        
        int villagerId = villager.getId();
        IdleState state = VILLAGER_IDLE_STATES.computeIfAbsent(villagerId, id -> new IdleState());
        
        boolean isActive = isVillagerActive(villager);
        boolean isTransformed = !villager.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        
        if (isActive) {
            // 村民处于活动状态，重置闲置计时器
            state.resetIdleTime();
        } else if (isTransformed) {
            // 村民处于闲置状态且已变身，增加闲置时间
            state.incrementIdleTime();
            
            // 如果闲置时间超过阈值，并且概率触发
            if (state.getIdleTime() >= IDLE_THRESHOLD && random.nextFloat() < 0.1F) { // 10%的概率解除变身
                revertTransformation(villager);
                state.resetIdleTime();
            }
        }
        
        // 清理已死亡的村民状态
        if (!villager.isAlive()) {
            VILLAGER_IDLE_STATES.remove(villagerId);
        }
    }
    
    /**
     * 判断村民是否处于活动状态
     */
    private static boolean isVillagerActive(LivingEntity villager) {
        // 检查是否正在移动
        if (villager.getDeltaMovement().x() != 0 || villager.getDeltaMovement().z() != 0) {
            return true;
        }
        
        // 检查是否正在攻击或被攻击
        if (villager.getLastHurtByMob() != null || villager.getLastHurtMob() != null) {
            return true;
        }
        
        // 检查是否处于危险状态
        if (isInDanger(villager)) {
            return true;
        }
        
        // 检查是否正在使用物品
        if (villager.isUsingItem()) {
            return true;
        }
        
        // 默认为闲置
        return false;
    }
    
    // 解除变身方法，同时恢复村民的本体模型
    private static void revertTransformation(LivingEntity villager) {
        // 播放解除变身音效
        villager.level().playSound(
            null,
            villager.getX(),
            villager.getY(),
            villager.getZ(),
            ModBossSounds.LOCKOFF.get(), // 使用锁种解除音效
            SoundSource.PLAYERS,
            0.8F,
            1.0F
        );

        // 清除变身状态标记
        villager.getPersistentData().remove("is_transformed_ridder");
        
        // 清除隐藏模型的NBT标签
        villager.getPersistentData().remove("hide_original_model");

        // 移除所有盔甲槽位的装备，这将触发模型恢复
        villager.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        villager.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        villager.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        
        // 确保清除脚部装备
        if (!villager.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
            villager.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        }
        
        // 保留Curio槽位中的腰带，不再移除它，这样村民可以在需要时再次变身
        // BeltCurioIntegration.removeCurio(villager, BeltCurioIntegration.BELT_SLOT_TYPE);
        
        // 刷新实体尺寸和属性，确保模型正确恢复
        villager.refreshDimensions();
        
        // 确保状态同步到客户端
        villager.canChangeDimensions();
    }
    
    /**
     * 村民闲置状态类
     */
    private static class IdleState {
        private int idleTime = 0;
        
        public void incrementIdleTime() {
            idleTime += CHECK_INTERVAL;
        }
        
        public void resetIdleTime() {
            idleTime = 0;
        }
        
        public int getIdleTime() {
            return idleTime;
        }
    }
}