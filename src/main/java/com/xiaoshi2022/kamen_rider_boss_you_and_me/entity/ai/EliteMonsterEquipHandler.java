package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 精英怪物腰带装备处理类 - 监听EliteMonsterNpc的腰带装备并触发变身
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class EliteMonsterEquipHandler {

    // 音效播放冷却时间（20 ticks = 1秒）
    private static final int SOUND_COOLDOWN_TICKS = 20 * 3; // 3秒冷却
    // 存储每个实体的音效播放时间戳，用于控制冷却
    private static final Map<Integer, Long> ENTITY_SOUND_COOLDOWN = new HashMap<>();

    /**
     * 监听实体生活更新事件，处理精英怪物的腰带装备和变身逻辑
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查是否是精英怪物
        if (!(entity instanceof EliteMonsterNpc eliteMonster)) {
            return;
        }
        
        // 检查是否是服务器端（防止在客户端运行）
        if (entity.level().isClientSide()) {
            return;
        }
        
        // 检查是否为实体的首个tick，跳过
        if (entity.tickCount < 5) {
            return;
        }
        
        // 用于检测变身状态的关键变量
        boolean shouldBeTransformed = false;
        ItemStack driverItem = ItemStack.EMPTY;
        
        // 检查Curio槽中是否有腰带
        boolean hasBeltInCurio = BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster);
        
        // 检查手持物品是否是腰带
        ItemStack mainHandItem = eliteMonster.getMainHandItem();
        boolean hasValidDriverInMainHand = isValidDriver(mainHandItem);
        
        // 检查副手是否是腰带
        ItemStack offHandItem = eliteMonster.getOffhandItem();
        boolean hasValidDriverInOffHand = isValidDriver(offHandItem);
        
        // 设置shouldBeTransformed标志
        shouldBeTransformed = hasBeltInCurio || hasValidDriverInMainHand || hasValidDriverInOffHand;
        
        // 检查是否有腰带物品，如果有则优先使用Curio槽的
        if (hasBeltInCurio) {
            Optional<ItemStack> beltOpt = BeltCurioIntegration.getBeltFromCurioSlot(eliteMonster);
            if (beltOpt.isPresent()) {
                driverItem = beltOpt.get();
                shouldBeTransformed = true;
            }
        }
        
        // 移除直接操作NBT的代码，统一使用实体方法
        
        // 如果应该变身但未变身
        if (shouldBeTransformed && !eliteMonster.isTransformed() && !driverItem.isEmpty()) {
            // 使用实体的setTransformed方法设置变身状态
            eliteMonster.setTransformed(true);
            // 触发变身逻辑
            triggerTransformation(eliteMonster, driverItem);
            
            // 强制更新实体状态
            eliteMonster.refreshDimensions();
            eliteMonster.setHealth(eliteMonster.getMaxHealth());
        } 
        // 如果不应该变身但已经变身
        else if (!shouldBeTransformed && eliteMonster.isTransformed()) {
            // 解除变身状态
            eliteMonster.setTransformed(false);
            
            // 清空盔甲装备槽
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    eliteMonster.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            
            // 恢复原始血量（如果需要）
            eliteMonster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(40.0D); // 恢复到原来的血量
            eliteMonster.setHealth(Math.min(eliteMonster.getHealth(), eliteMonster.getMaxHealth()));
            
            // 强制更新实体状态
            eliteMonster.refreshDimensions();
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

    /**
     * 触发精英怪物变身
     */
    private static void triggerTransformation(EliteMonsterNpc eliteMonster, ItemStack beltStack) {
        // 确保变身状态正确设置，这将自动触发隐身功能
        
        // 只有在第一次变身时才设置血量，避免每次调用都重置血量导致无法被击败
        if (eliteMonster.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            // 提升血量到80点
            eliteMonster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(80.0D);
            eliteMonster.setHealth(80.0F);
            
            // 强制刷新实体尺寸和状态
            eliteMonster.refreshDimensions();
        }
        
        // 根据腰带类型调整不同的变身形态或属性
        if (beltStack.getItem() instanceof Genesis_driver) {
            Genesis_driver driver = (Genesis_driver) beltStack.getItem();
            Genesis_driver.BeltMode mode = driver.getMode(beltStack);
            
            // 检查是否已经标记为变身状态
            boolean isAlreadyTransformed = eliteMonster.isTransformed();
            // 检查是否已经装备了盔甲
            boolean isArmorEquipped = !eliteMonster.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            
            // 综合判断是否已经完成变身
            boolean isFullyTransformed = isAlreadyTransformed && isArmorEquipped;
            
            // 根据腰带模式为精英怪物装备对应的盔甲并播放变身音效
            switch (mode) {
                case LEMON -> {
                    // Duke 盔甲
                    equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.DUKE_HELMET.get()));
                    equipArmor(eliteMonster, EquipmentSlot.CHEST, new ItemStack(ModItems.DUKE_CHESTPLATE.get()));
                    equipArmor(eliteMonster, EquipmentSlot.LEGS, new ItemStack(ModItems.DUKE_LEGGINGS.get()));
                    // 播放柠檬变身音效（仅当未完全变身后且不在冷却中）
                    if (!isFullyTransformed && !isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.LEMON_BARON.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        // 设置音效冷却
                        setSoundCooldown(eliteMonster);
                    }
                }
                case MELON -> {
                    // Zangetsu Shin 盔甲
                    equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.ZANGETSU_SHIN_HELMET.get()));
                    equipArmor(eliteMonster, EquipmentSlot.CHEST, new ItemStack(ModItems.ZANGETSU_SHIN_CHESTPLATE.get()));
                    equipArmor(eliteMonster, EquipmentSlot.LEGS, new ItemStack(ModItems.ZANGETSU_SHIN_LEGGINGS.get()));
                    // 播放蜜瓜变身音效（仅当未完全变身后且不在冷却中）
                    if (!isFullyTransformed && !isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.MELONX_ARMS.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        // 设置音效冷却
                        setSoundCooldown(eliteMonster);
                    }
                }
                case CHERRY -> {
                    // Sigurd 盔甲
                    equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.SIGURD_HELMET.get()));
                    equipArmor(eliteMonster, EquipmentSlot.CHEST, new ItemStack(ModItems.SIGURD_CHESTPLATE.get()));
                    equipArmor(eliteMonster, EquipmentSlot.LEGS, new ItemStack(ModItems.SIGURD_LEGGINGS.get()));
                    // 播放樱桃变身音效（仅当未完全变身后且不在冷却中）
                    if (!isFullyTransformed && !isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.CHERRY_ARMS.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        // 设置音效冷却
                        setSoundCooldown(eliteMonster);
                    }
                }
                case PEACH -> {
                    // Marika 盔甲
                    equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.MARIKA_HELMET.get()));
                    equipArmor(eliteMonster, EquipmentSlot.CHEST, new ItemStack(ModItems.MARIKA_CHESTPLATE.get()));
                    equipArmor(eliteMonster, EquipmentSlot.LEGS, new ItemStack(ModItems.MARIKA_LEGGINGS.get()));
                    // 播放桃子变身音效（仅当未完全变身后且不在冷却中）
                    if (!isFullyTransformed && !isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.PEACH_ARMS.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        // 设置音效冷却
                        setSoundCooldown(eliteMonster);
                    }
                }
                case DRAGONFRUIT -> {
                    // Tyrant 盔甲
                    equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.TYRANT_HELMET.get()));
                    equipArmor(eliteMonster, EquipmentSlot.CHEST, new ItemStack(ModItems.TYRANT_CHESTPLATE.get()));
                    equipArmor(eliteMonster, EquipmentSlot.LEGS, new ItemStack(ModItems.TYRANT_LEGGINGS.get()));
                    // 播放火龙果变身音效（仅当未完全变身后且不在冷却中）
                    if (!isFullyTransformed && !isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.DRAGONFRUIT_ARMS.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        // 设置音效冷却
                        setSoundCooldown(eliteMonster);
                    }
                }
                default -> {
                    // 默认模式，装备Duke盔甲
                    equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.DUKE_HELMET.get()));
                    equipArmor(eliteMonster, EquipmentSlot.CHEST, new ItemStack(ModItems.DUKE_CHESTPLATE.get()));
                    equipArmor(eliteMonster, EquipmentSlot.LEGS, new ItemStack(ModItems.DUKE_LEGGINGS.get()));
                    if (!isFullyTransformed && !isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.LEMON_BARON.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        // 设置音效冷却
                        setSoundCooldown(eliteMonster);
                    }
                }
            }
        } else if (beltStack.getItem() instanceof sengokudrivers_epmty) {
            // sengokudrivers_epmty特定处理
            // 这里可以根据需要添加sengokudrivers_epmty的变身逻辑
            if (!isOnSoundCooldown(eliteMonster)) {
                eliteMonster.level().playSound(
                        null,
                        eliteMonster.getX(),
                        eliteMonster.getY(),
                        eliteMonster.getZ(),
                        ModBossSounds.LEMON_BARON.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
                setSoundCooldown(eliteMonster);
            }
        } else if (beltStack.getItem() instanceof Two_sidriver) {
            // Two_sidriver特定处理
            // 这里可以根据需要添加Two_sidriver的变身逻辑
            if (!isOnSoundCooldown(eliteMonster)) {
                eliteMonster.level().playSound(
                        null,
                        eliteMonster.getX(),
                        eliteMonster.getY(),
                        eliteMonster.getZ(),
                        ModBossSounds.LEMON_BARON.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
                setSoundCooldown(eliteMonster);
            }
        }
    }
    
    /**
     * 检查物品是否是有效的腰带
     */
    static boolean isValidDriver(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否是各种腰带类型
        return stack.getItem() instanceof Genesis_driver ||
               stack.getItem() instanceof sengokudrivers_epmty ||
               stack.getItem() instanceof Two_sidriver;
    }
    
    // 辅助方法：装备盔甲，只在当前槽位为空时装备
    private static void equipArmor(LivingEntity entity, EquipmentSlot slot, ItemStack armorStack) {
        if (entity.getItemBySlot(slot).isEmpty()) {
            entity.setItemSlot(slot, armorStack);
        }
    }
}