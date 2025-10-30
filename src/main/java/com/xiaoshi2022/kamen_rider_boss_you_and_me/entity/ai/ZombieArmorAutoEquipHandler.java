package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class ZombieArmorAutoEquipHandler {
    
    // 闲置检测相关常量和数据结构
    private static final Map<Integer, IdleState> ENTITY_IDLE_STATES = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final int IDLE_THRESHOLD = 1200; // 60秒没有移动或战斗
    private static final int CHECK_INTERVAL = 20; // 每秒检查一次

    // 当生物装备发生变化时触发
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        // 检查是否是僵尸
        if (entity instanceof Zombie) {
            // 检查主手是否持有Genesis_driver
            ItemStack mainHandStack = entity.getMainHandItem();
            if (mainHandStack.getItem() instanceof Genesis_driver) {
                // 添加NBT标签标记这是一个变身僵尸
                addTransformationNBT(entity);
                handleZombieWithDriver((Zombie) entity, mainHandStack);
            }
        }
    }

    // 当生物tick时检查，确保即使没有装备变化也能正确装备盔甲和音速弓
    @SubscribeEvent
      public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 每20tick（1秒）检查一次，避免过于频繁
        if (entity.tickCount % 20 == 0 && !entity.level().isClientSide) {
            // 检查是否是僵尸实体
            if (entity instanceof Zombie zombie) {
                handleZombieTransformation(zombie);
            } 
            // 检查是否是村民实体
            else if (isVillager(entity)) {
                handleVillagerTransformation(entity);
            }
        }
    }
    
    // 判断是否是村民
    private static boolean isVillager(LivingEntity entity) {
        return entity.getType() == EntityType.VILLAGER || entity.getType() == EntityType.WANDERING_TRADER;
    }
    
    // 处理僵尸变身逻辑
    private static void handleZombieTransformation(Zombie zombie) {
        // 检查僵尸是否手持腰带或在Curio槽装备了腰带
        boolean hasDriver = false;
        
        // 检查主手
        ItemStack mainHandStack = zombie.getMainHandItem();
        if (mainHandStack.getItem() instanceof Genesis_driver) {
            hasDriver = true;
        }
        
        // 检查Curio槽
        if (!hasDriver && BeltCurioIntegration.hasBeltInCurioSlot(zombie)) {
            hasDriver = true;
        }
        
        // 根据是否持有腰带更新NBT标签
        if (hasDriver) {
            addTransformationNBT(zombie);
        } else {
            removeTransformationNBT(zombie);
        }
        
        // 检查僵尸是否手持腰带，如果是则尝试装备到Curio槽
        if (mainHandStack.getItem() instanceof Genesis_driver && !BeltCurioIntegration.hasBeltInCurioSlot(zombie)) {
            BeltCurioIntegration.tryEquipBeltToCurioSlot(zombie, mainHandStack);
        }
        
        // 如果已装备腰带，处理腰带逻辑
        if (BeltCurioIntegration.hasBeltInCurioSlot(zombie)) {
            BeltCurioIntegration.getBeltFromEntity(zombie).ifPresent(
                beltStack -> {
                    handleZombieWithDriver(zombie, beltStack);
                    checkAndHandleIdleState(zombie);
                }
            );
        }
    }
    
    // 处理村民变身逻辑
    private static void handleVillagerTransformation(LivingEntity villager) {
        // 检查村民是否手持腰带或在Curio槽装备了腰带
        boolean hasDriver = false;
        
        // 检查主手
        ItemStack mainHandStack = villager.getMainHandItem();
        if (mainHandStack.getItem() instanceof Genesis_driver) {
            hasDriver = true;
        }
        
        // 检查Curio槽
        if (!hasDriver && BeltCurioIntegration.hasBeltInCurioSlot(villager)) {
            hasDriver = true;
        }
        
        // 根据是否持有腰带更新NBT标签
        if (hasDriver) {
            addTransformationNBT(villager);
            
            // 如果已装备腰带，为村民装备音速弓
            if (BeltCurioIntegration.hasBeltInCurioSlot(villager)) {
                BeltCurioIntegration.getBeltFromEntity(villager).ifPresent(
                    beltStack -> {
                        // 为村民装备音速弓
                        giveSonicBowToTransformedEntity(villager);
                    }
                );
            }
        } else {
            removeTransformationNBT(villager);
        }
        
        // 检查村民是否手持腰带，如果是则尝试装备到Curio槽
        if (mainHandStack.getItem() instanceof Genesis_driver && !BeltCurioIntegration.hasBeltInCurioSlot(villager)) {
            BeltCurioIntegration.tryEquipBeltToCurioSlot(villager, mainHandStack);
        }
    }
    
    /**
     * 为实体添加变身NBT标签
     * 使用共享标签确保NBT能正确同步到客户端
     */
    private static void addTransformationNBT(LivingEntity entity) {
        // 首先在共享标签中设置（客户端可见）
        CompoundTag shareTag = entity.getPersistentData();
        shareTag.putBoolean("is_transformed_ridder", true);
        
        // 注意：对getPersistentData()返回的CompoundTag的修改会自动同步到客户端
        // 不需要额外的同步调用
    }
    
    /**
     * 为实体移除变身NBT标签
     * 使用共享标签确保NBT能正确同步到客户端
     */
    private static void removeTransformationNBT(LivingEntity entity) {
        // 从共享标签中移除
        CompoundTag shareTag = entity.getPersistentData();
        shareTag.remove("is_transformed_ridder");
        
        // 注意：对getPersistentData()返回的CompoundTag的修改会自动同步到客户端
        // 不需要额外的同步调用
    }
    
    /**
     * 检查并处理僵尸的闲置状态，当闲置时间过长时自动移除盔甲
     */
    private static void checkAndHandleIdleState(Zombie zombie) {
        int entityId = zombie.getId();
        IdleState state = ENTITY_IDLE_STATES.computeIfAbsent(entityId, id -> new IdleState());
        
        boolean isActive = isEntityActive(zombie);
        
        if (isActive) {
            // 实体处于活动状态，重置闲置计时器
            state.resetIdleTime();
        } else {
            // 实体处于闲置状态，增加闲置时间
            state.incrementIdleTime();
            
            // 如果闲置时间超过阈值，并且概率触发
            if (state.getIdleTime() >= IDLE_THRESHOLD && RANDOM.nextFloat() < 0.1F) { // 10%的概率脱下盔甲
                removeAllArmor(zombie);
                // 移除变身NBT标签
                removeTransformationNBT(zombie);
                state.resetIdleTime();
            }
        }
        
        // 清理已死亡的实体状态
        if (!zombie.isAlive()) {
            ENTITY_IDLE_STATES.remove(entityId);
        }
    }
    
    /**
     * 判断实体是否处于活动状态
     */
    private static boolean isEntityActive(Zombie zombie) {
        // 检查是否正在移动
        if (zombie.getDeltaMovement().x() != 0 || zombie.getDeltaMovement().z() != 0) {
            return true;
        }
        
        // 检查是否正在攻击或被攻击
        if (zombie.getLastHurtByMob() != null && zombie.tickCount - zombie.getLastHurtByMobTimestamp() < 200) {
            return true;
        }
        
        // 检查是否有目标（例如僵尸正在跟踪玩家）
        if (zombie.getTarget() instanceof Player) {
            return true;
        }
        
        // 检查是否正在使用物品
        if (zombie.isUsingItem()) {
            return true;
        }
        
        // 默认为闲置
        return false;
    }
    
    /**
     * 实体闲置状态类
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
    
    // 移除所有盔甲的辅助方法，同时恢复僵尸的本体模型
    private static void removeAllArmor(Zombie zombie) {
        // 清除所有盔甲槽位的物品，这将触发模型恢复
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            if (!zombie.getItemBySlot(slot).isEmpty()) {
                zombie.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        
        // 确保清除脚部装备
        if (!zombie.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        }
        
        // 刷新实体尺寸和属性，确保模型正确恢复
        zombie.refreshDimensions();
        
        // 对于服务器端，确保状态同步到客户端
        if (!zombie.level().isClientSide()) {
            zombie.canChangeDimensions();
        }
    }

    // 处理手持腰带的僵尸装备盔甲和音速弓的逻辑
    private static void handleZombieWithDriver(Zombie zombie, ItemStack driverStack) {
        Genesis_driver driver = (Genesis_driver) driverStack.getItem();
        Genesis_driver.BeltMode mode = driver.getMode(driverStack);
        
        // 检查僵尸是否已经标记为变身状态
        boolean isAlreadyTransformed = zombie.getPersistentData().getBoolean("is_transformed_ridder");
        // 检查是否已经装备了盔甲
        boolean isArmorEquipped = !zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        
        // 只有在第一次变身时才设置血量，避免每次调用都重置血量导致无法被击败
        if (!isAlreadyTransformed || !isArmorEquipped) {
            // 提升血量到40点
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(40.0D);
            zombie.setHealth(40.0F);
        }

        // 标记是否需要播放音效（只有当盔甲从无到有时才播放）
        boolean shouldPlaySound = zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty();

        // 根据腰带模式为僵尸装备对应的盔甲（只装备头盔、胸甲和护腿，不装备鞋子）
        switch (mode) {
            case LEMON -> {
                // DUKE 盔甲
                equipArmor(zombie, EquipmentSlot.HEAD, new ItemStack(ModItems.DUKE_HELMET.get()));
                equipArmor(zombie, EquipmentSlot.CHEST, new ItemStack(ModItems.DUKE_CHESTPLATE.get()));
                equipArmor(zombie, EquipmentSlot.LEGS, new ItemStack(ModItems.DUKE_LEGGINGS.get()));
                // 播放对应音效
                if (shouldPlaySound) {
                    playTransformationSound(zombie, ModBossSounds.LEMON_BARON.get());
                }
            }
            case MELON -> {
                // Zangetsu Shin 盔甲
                equipArmor(zombie, EquipmentSlot.HEAD, new ItemStack(ModItems.ZANGETSU_SHIN_HELMET.get()));
                equipArmor(zombie, EquipmentSlot.CHEST, new ItemStack(ModItems.ZANGETSU_SHIN_CHESTPLATE.get()));
                equipArmor(zombie, EquipmentSlot.LEGS, new ItemStack(ModItems.ZANGETSU_SHIN_LEGGINGS.get()));
                // 播放对应音效
                if (shouldPlaySound) {
                    playTransformationSound(zombie, ModBossSounds.MELONX_ARMS.get());
                }
            }
            case CHERRY -> {
                // Sigurd 盔甲
                equipArmor(zombie, EquipmentSlot.HEAD, new ItemStack(ModItems.SIGURD_HELMET.get()));
                equipArmor(zombie, EquipmentSlot.CHEST, new ItemStack(ModItems.SIGURD_CHESTPLATE.get()));
                equipArmor(zombie, EquipmentSlot.LEGS, new ItemStack(ModItems.SIGURD_LEGGINGS.get()));
                // 播放对应音效
                if (shouldPlaySound) {
                    playTransformationSound(zombie, ModBossSounds.CHERRY_ARMS.get()); // 注意这里保持了原有的拼写
                }
            }
            case PEACH -> {
                // Marika 盔甲
                equipArmor(zombie, EquipmentSlot.HEAD, new ItemStack(ModItems.MARIKA_HELMET.get()));
                equipArmor(zombie, EquipmentSlot.CHEST, new ItemStack(ModItems.MARIKA_CHESTPLATE.get()));
                equipArmor(zombie, EquipmentSlot.LEGS, new ItemStack(ModItems.MARIKA_LEGGINGS.get()));
                // 播放对应音效
                if (shouldPlaySound) {
                    playTransformationSound(zombie, ModBossSounds.PEACH_ARMS.get());
                }
            }
            case DRAGONFRUIT -> {
                // Tyrant 盔甲
                equipArmor(zombie, EquipmentSlot.HEAD, new ItemStack(ModItems.TYRANT_HELMET.get()));
                equipArmor(zombie, EquipmentSlot.CHEST, new ItemStack(ModItems.TYRANT_CHESTPLATE.get()));
                equipArmor(zombie, EquipmentSlot.LEGS, new ItemStack(ModItems.TYRANT_LEGGINGS.get()));
                // 播放对应音效
                if (shouldPlaySound) {
                    playTransformationSound(zombie, ModBossSounds.DRAGONFRUIT_ARMS.get());
                }
            }
            case DEFAULT -> {
                // 默认模式不装备盔甲
            }
        }
        
        // 为僵尸装备音速弓
        giveSonicBowToTransformedEntity(zombie);
    }
    
    // 播放变身音效的辅助方法
    private static void playTransformationSound(Zombie zombie, net.minecraft.sounds.SoundEvent soundEvent) {
        if (!zombie.level().isClientSide) {
            zombie.level().playSound(
                null,
                zombie.getX(),
                zombie.getY(),
                zombie.getZ(),
                soundEvent,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
        }
    }

    // 辅助方法：装备盔甲，只在当前槽位为空时装备
    private static void equipArmor(Zombie zombie, EquipmentSlot slot, ItemStack armorStack) {
        if (zombie.getItemBySlot(slot).isEmpty()) {
            zombie.setItemSlot(slot, armorStack);
        }
    }
    
    // 为僵尸和村民装备音速弓
    private static void giveSonicBowToTransformedEntity(LivingEntity entity) {
        // 检查是否是僵尸或村民
        boolean isZombie = entity instanceof Zombie;
        boolean isVillager = entity.getType() == EntityType.VILLAGER || entity.getType() == EntityType.WANDERING_TRADER;
        
        // 只有僵尸或村民才给予音速弓
        if (!(isZombie || isVillager)) {
            return;
        }
        
        // 检查是否已经装备了音速弓，如果没有则装备
        ItemStack mainHand = entity.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow)) {
            // 创建音速弓实例
            ItemStack sonicArrow = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get());
            
            // 设置到主手
            entity.setItemSlot(EquipmentSlot.MAINHAND, sonicArrow);
            
            // 确保实体能够使用这把弓进行射击
            setupRangedAttackAI(entity);
        }
    }
    
    // 设置实体的远程攻击AI，使僵尸和村民能够使用音速弓射击
    private static void setupRangedAttackAI(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            // 清除现有的AI目标和行为，为新的行为腾出空间
            mob.setTarget(null);
            
            // 使实体具有攻击性，这样它会主动寻找并攻击玩家
            mob.setAggressive(true);
            
            // 确保实体能够自动寻找玩家作为目标
            if (mob instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob) {
                pathfinderMob.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                    pathfinderMob, 
                    net.minecraft.world.entity.player.Player.class, 
                    true
                ));
                
                // 添加基本的寻路行为
                pathfinderMob.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(pathfinderMob, 1.0D));
                pathfinderMob.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(pathfinderMob));
            }
        }
    }
}