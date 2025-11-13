package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

// 本类负责处理村民装备腰带的逻辑，包括菜单打开和模型恢复功能
// 当村民手持Genesis_driver腰带时，会根据腰带模式自动装备对应的骑士盔甲并播放变身音效
// 同时处理菜单交互逻辑，允许玩家与村民进行装备切换

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class VillagerEquipDriverHandler {

    // 音效播放冷却时间（20 ticks = 1秒）
    private static final int SOUND_COOLDOWN_TICKS = 20 * 3; // 3秒冷却
    // 存储每个实体的音效播放时间戳，用于控制冷却
    private static final Map<Integer, Long> ENTITY_SOUND_COOLDOWN = new HashMap<>();

    // 当生物装备发生变化时触发
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        // 检查是否是MCA村民
        if (entity instanceof VillagerEntityMCA) {
            // 检查主手是否持有Genesis_driver
            ItemStack mainHandStack = entity.getMainHandItem();
            if (mainHandStack.getItem() instanceof Genesis_driver) {
                // 添加NBT标签标记这是一个变身村民
                addTransformationNBT(entity);
                handleVillagerWithDriver(entity, mainHandStack);
            } else {
                // 如果不再持有腰带，移除NBT标签
                removeTransformationNBT(entity);
            }
        }
    }
    
    // 检查实体是否在音效冷却中
    private static boolean isOnSoundCooldown(LivingEntity entity) {
        int entityId = entity.getId();
        if (!ENTITY_SOUND_COOLDOWN.containsKey(entityId)) {
            return false;
        }
        
        long currentTime = entity.level().getGameTime();
        long lastSoundTime = ENTITY_SOUND_COOLDOWN.get(entityId);
        
        return (currentTime - lastSoundTime) < SOUND_COOLDOWN_TICKS;
    }
    
    // 设置实体的音效冷却时间
    private static void setSoundCooldown(LivingEntity entity) {
        int entityId = entity.getId();
        long currentTime = entity.level().getGameTime();
        ENTITY_SOUND_COOLDOWN.put(entityId, currentTime);
    }
    
    // 清理无效的冷却记录（当实体被移除时）
    private static void cleanupSoundCooldowns() {
        // 这个方法可以在适当的时候调用，比如定期清理不再存在的实体记录
        // 为了简化，这里省略具体实现
    }

    // 当生物tick时检查，确保即使没有装备变化也能正确装备盔甲
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查是否是MCA村民实体
        if (entity instanceof VillagerEntityMCA && !entity.level().isClientSide) {
            // 每20tick（1秒）检查一次，避免过于频繁
            if (entity.tickCount % 20 == 0) {
                // 检查村民是否手持腰带或在Curio槽装备了腰带
                boolean hasDriver = false;
                AtomicReference<ItemStack> beltStack = new AtomicReference<>(ItemStack.EMPTY);
                
                // 检查主手
                ItemStack mainHandStack = entity.getMainHandItem();
                if (mainHandStack.getItem() instanceof Genesis_driver) {
                    hasDriver = true;
                    beltStack.set(mainHandStack);
                }
                
                // 检查Curio槽
                if (!hasDriver && BeltCurioIntegration.hasBeltInCurioSlot(entity)) {
                    hasDriver = true;
                    // 获取Curio槽中的腰带
                    BeltCurioIntegration.getBeltFromEntity(entity).ifPresent(beltStack::set);
                }
                
                // 根据是否持有腰带更新NBT标签
                if (hasDriver) {
                    addTransformationNBT(entity);
                    // 只有当村民没有装备头盔时才执行，避免重复装备和重复播放音效
                    if (entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !beltStack.get().isEmpty()) {
                        handleVillagerWithDriver(entity, beltStack.get());
                    }
                } else {
                    // 如果不再持有腰带，移除NBT标签
                    removeTransformationNBT(entity);
                }
            }
        }
    }

    /**
     * 为实体添加变身NBT标签
     */
    private static void addTransformationNBT(LivingEntity entity) {
        entity.getPersistentData().putBoolean("is_transformed_ridder", true);
    }
    
    // 为MCA村民装备音速弓
    private static void giveSonicBowToTransformedEntity(LivingEntity entity) {
        // 确保这是一个MCA村民
        if (!(entity instanceof VillagerEntityMCA)) {
            return;
        }
        
        // 检查是否已经装备了音速弓，如果没有则装备
        ItemStack mainHand = entity.getMainHandItem();
        // 避免替换已经持有的腰带
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof Genesis_driver)) {
            try {
                // 尝试获取音速弓物品实例
                Class<?> modItemsClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems");
                java.lang.reflect.Field sonicArrowField = modItemsClass.getDeclaredField("SONICARROW");
                sonicArrowField.setAccessible(true);
                Object sonicArrow = sonicArrowField.get(null);
                
                if (sonicArrow instanceof net.minecraftforge.registries.RegistryObject) {
                    // 获取RegistryObject实例
                    net.minecraftforge.registries.RegistryObject<?> registryObject = 
                            (net.minecraftforge.registries.RegistryObject<?>) sonicArrow;
                    
                    if (registryObject.isPresent()) {
                        // 获取Item对象并直接创建ItemStack
                        Object itemObj = registryObject.get();
                        if (itemObj instanceof net.minecraft.world.item.Item) {
                            net.minecraft.world.item.Item item = (net.minecraft.world.item.Item) itemObj;
                            ItemStack sonicArrowStack = new ItemStack(item);
                            // 设置到主手，除非已经持有腰带
                            entity.setItemSlot(EquipmentSlot.MAINHAND, sonicArrowStack);
                            
                            // 确保实体能够使用这把弓进行射击
                            setupRangedAttackAI(entity);
                        }
                    }
                }
            } catch (Exception e) {
                // 如果反射失败，记录异常但不崩溃
                e.printStackTrace();
            }
        }
    }
    
    // 设置MCA村民的远程攻击AI，使村民能够使用音速弓射击
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
    
    /**
     * 为实体移除变身NBT标签
     */
    private static void removeTransformationNBT(LivingEntity entity) {
        entity.getPersistentData().remove("is_transformed_ridder");
    }
    
    // 处理手持腰带的村民装备盔甲和播放音效的逻辑
    static void handleVillagerWithDriver(LivingEntity villager, ItemStack driverStack) {
        // 首先检查物品是否是Genesis_driver类型
        if (!(driverStack.getItem() instanceof Genesis_driver)) {
            // 如果不是Genesis_driver类型，记录警告但不崩溃
            System.out.println("警告：尝试为非Genesis_driver类型的腰带处理村民变身");
            return; // 直接返回，避免类型转换错误
        }
        
        Genesis_driver driver = (Genesis_driver) driverStack.getItem();
        Genesis_driver.BeltMode mode = driver.getMode(driverStack);

        // 检查村民是否已经标记为变身状态
        boolean isAlreadyTransformed = villager.getPersistentData().getBoolean("is_transformed_ridder");
        // 检查是否已经装备了盔甲
        boolean isArmorEquipped = !villager.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        
        // 综合判断是否已经完成变身
        boolean isFullyTransformed = isAlreadyTransformed && isArmorEquipped;
        
        // 只有在第一次变身时才设置血量，避免每次调用都重置血量导致无法被击败
        if (!isAlreadyTransformed || !isArmorEquipped) {
            // 提升血量到40点
            villager.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(40.0D);
            villager.setHealth(40.0F);
        }

        // 给予音速弓
        giveSonicBowToTransformedEntity(villager);

        // 根据腰带模式为村民装备对应的盔甲并播放变身音效
        switch (mode) {
            case MELON -> {
                // Zangetsu Shin 盔甲
                equipArmor(villager, EquipmentSlot.HEAD, new ItemStack(ModItems.ZANGETSU_SHIN_HELMET.get()));
                equipArmor(villager, EquipmentSlot.CHEST, new ItemStack(ModItems.ZANGETSU_SHIN_CHESTPLATE.get()));
                equipArmor(villager, EquipmentSlot.LEGS, new ItemStack(ModItems.ZANGETSU_SHIN_LEGGINGS.get()));
                // 播放蜜瓜变身音效（仅当未完全变身后且不在冷却中）
                if (!isFullyTransformed && !isOnSoundCooldown(villager)) {
                    villager.level().playSound(
                            null,
                            villager.getX(),
                            villager.getY(),
                            villager.getZ(),
                            ModBossSounds.MELONX_ARMS.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                    // 标记为已变身
                    villager.getPersistentData().putBoolean("is_transformed_ridder", true);
                    // 设置音效冷却
                    setSoundCooldown(villager);
                }
            }
            case LEMON -> {
                // duke 盔甲
                equipArmor(villager, EquipmentSlot.HEAD, new ItemStack(ModItems.DUKE_HELMET.get()));
                equipArmor(villager, EquipmentSlot.CHEST, new ItemStack(ModItems.DUKE_CHESTPLATE.get()));
                equipArmor(villager, EquipmentSlot.LEGS, new ItemStack(ModItems.DUKE_LEGGINGS.get()));
                // 播放柠檬变身音效（仅当未完全变身后且不在冷却中）
                if (!isFullyTransformed && !isOnSoundCooldown(villager)) {
                    villager.level().playSound(
                            null,
                            villager.getX(),
                            villager.getY(),
                            villager.getZ(),
                            ModBossSounds.LEMON_BARON.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                    // 标记为已变身
                    villager.getPersistentData().putBoolean("is_transformed_ridder", true);
                    // 设置音效冷却
                    setSoundCooldown(villager);
                }
            }
            case CHERRY -> {
                // Sigurd 盔甲
                equipArmor(villager, EquipmentSlot.HEAD, new ItemStack(ModItems.SIGURD_HELMET.get()));
                equipArmor(villager, EquipmentSlot.CHEST, new ItemStack(ModItems.SIGURD_CHESTPLATE.get()));
                equipArmor(villager, EquipmentSlot.LEGS, new ItemStack(ModItems.SIGURD_LEGGINGS.get()));
                // 播放樱桃变身音效（仅当未完全变身后且不在冷却中）
                if (!isFullyTransformed && !isOnSoundCooldown(villager)) {
                    villager.level().playSound(
                            null,
                            villager.getX(),
                            villager.getY(),
                            villager.getZ(),
                            ModBossSounds.CHERRY_ARMS.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                    // 标记为已变身
                    villager.getPersistentData().putBoolean("is_transformed_ridder", true);
                    // 设置音效冷却
                    setSoundCooldown(villager);
                }
            }
            case PEACH -> {
                // Marika 盔甲
                equipArmor(villager, EquipmentSlot.HEAD, new ItemStack(ModItems.MARIKA_HELMET.get()));
                equipArmor(villager, EquipmentSlot.CHEST, new ItemStack(ModItems.MARIKA_CHESTPLATE.get()));
                equipArmor(villager, EquipmentSlot.LEGS, new ItemStack(ModItems.MARIKA_LEGGINGS.get()));
                // 播放桃子变身音效（仅当未完全变身后且不在冷却中）
                if (!isFullyTransformed && !isOnSoundCooldown(villager)) {
                    villager.level().playSound(
                            null,
                            villager.getX(),
                            villager.getY(),
                            villager.getZ(),
                            ModBossSounds.PEACH_ARMS.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                    // 标记为已变身
                    villager.getPersistentData().putBoolean("is_transformed_ridder", true);
                    // 设置音效冷却
                    setSoundCooldown(villager);
                }
            }
            case DRAGONFRUIT -> {
                // Tyrant 盔甲
                equipArmor(villager, EquipmentSlot.HEAD, new ItemStack(ModItems.TYRANT_HELMET.get()));
                equipArmor(villager, EquipmentSlot.CHEST, new ItemStack(ModItems.TYRANT_CHESTPLATE.get()));
                equipArmor(villager, EquipmentSlot.LEGS, new ItemStack(ModItems.TYRANT_LEGGINGS.get()));
                // 播放火龙果变身音效（仅当未完全变身后且不在冷却中）
                if (!isFullyTransformed && !isOnSoundCooldown(villager)) {
                    villager.level().playSound(
                            null,
                            villager.getX(),
                            villager.getY(),
                            villager.getZ(),
                            ModBossSounds.DRAGONFRUIT_ARMS.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                    // 标记为已变身
                    villager.getPersistentData().putBoolean("is_transformed_ridder", true);
                    // 设置音效冷却
                    setSoundCooldown(villager);
                }
            }
            case DEFAULT -> {
                // 默认模式不装备盔甲和播放音效
            }
        }
    }

    // 辅助方法：装备盔甲，只在当前槽位为空时装备
    private static void equipArmor(LivingEntity villager, EquipmentSlot slot, ItemStack armorStack) {
        if (villager.getItemBySlot(slot).isEmpty()) {
            villager.setItemSlot(slot, armorStack);
        }
    }
    
    /**
     * 移除村民的所有盔甲并恢复本体模型
     * 当村民不再需要装备盔甲时调用此方法
     */
    private static void removeAllArmor(LivingEntity villager) {
        // 清除所有盔甲槽位的物品
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD, 
            EquipmentSlot.CHEST, 
            EquipmentSlot.LEGS, 
            EquipmentSlot.FEET
        }) {
            if (!villager.getItemBySlot(slot).isEmpty()) {
                // 将物品掉落
                villager.spawnAtLocation(villager.getItemBySlot(slot));
                villager.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        
        // 清除变身状态标记
        villager.getPersistentData().remove("is_transformed_ridder");
        
        // 移除变身NBT标签，确保本体模型可见
        removeTransformationNBT(villager);
        
        // 注意：保留Curio槽位中的腰带，不再移除它
    }
    
    // 为LivingTickEvent添加更完善的逻辑，确保在不需要装备时移除盔甲
    @SubscribeEvent
    public static void onVillagerIdle(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 只处理MCA村民
        if (!(entity instanceof VillagerEntityMCA)) {
            return;
        }
        
        // 每100tick（5秒）检查一次空闲状态，避免过于频繁
        if (entity.tickCount % 100 != 0) {
            return;
        }
        
        // 检查村民是否有盔甲
        boolean hasArmor = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD, 
            EquipmentSlot.CHEST, 
            EquipmentSlot.LEGS
        }) {
            if (!entity.getItemBySlot(slot).isEmpty()) {
                hasArmor = true;
                break;
            }
        }
        
        // 检查村民是否持有腰带（主手或Curio槽）
        boolean hasDriver = false;
        
        // 检查主手
        if (entity.getMainHandItem().getItem() instanceof Genesis_driver) {
            hasDriver = true;
        }
        
        // 检查Curio槽 - 这个是关键，确保不会错误地移除Curio槽中的腰带
        if (!hasDriver && BeltCurioIntegration.hasBeltInCurioSlot(entity)) {
            hasDriver = true;
        }
        
        // 只有当村民有盔甲但完全没有腰带（主手和Curio槽都没有）时，才移除所有盔甲
        if (hasArmor && !hasDriver) {
            removeAllArmor(entity);
        }
    }
}