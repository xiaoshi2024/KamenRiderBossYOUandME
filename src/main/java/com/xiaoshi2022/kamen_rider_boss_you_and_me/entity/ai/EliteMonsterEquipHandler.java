package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
        
        // 检查主手是否有腰带，如果有且Curio槽位为空，则尝试装备到Curio槽位
        ItemStack mainHandItem = eliteMonster.getMainHandItem();
        if (!mainHandItem.isEmpty() && isValidDriver(mainHandItem) && !BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster)) {
            if (BeltCurioIntegration.tryEquipBeltToCurioSlot(eliteMonster, mainHandItem)) {
                // 成功装备到Curio槽位后，清空主手
                eliteMonster.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
        
        // 主动拾取附近的腰带物品 - 从EliteMonsterFindBeltGoal添加的逻辑
        if (!BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster)) {
            // 寻找附近的腰带物品
            double searchRange = 3.0D;
            eliteMonster.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, 
                    eliteMonster.getBoundingBox().inflate(searchRange), 
                    item -> item.isAlive() && !item.hasPickUpDelay() && isValidDriver(item.getItem())
            ).stream().findFirst().ifPresent(targetItem -> {
                // 当接近物品时，主动拾取
                ItemStack stack = targetItem.getItem().copy();
                // 优先拾取到Curio槽位
                if (BeltCurioIntegration.tryEquipBeltToCurioSlot(eliteMonster, stack)) {
                    // 从世界中移除物品
                    targetItem.discard();
                } else if (eliteMonster.getMainHandItem().isEmpty()) {
                    // 如果Curio槽位无法装备，则装备到主手
                    eliteMonster.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack.copy());
                    targetItem.discard();
                }
            });
        }
        
        // 用于检测变身状态的关键变量
        boolean shouldBeTransformed = false;
        ItemStack driverItem = ItemStack.EMPTY;
        
        // 检查Curio槽中是否有腰带（只支持腰带槽位）
        boolean hasBeltInCurio = BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster);
        
        // 设置shouldBeTransformed标志，只基于Curio槽中的腰带
        shouldBeTransformed = hasBeltInCurio;
        
        // 检查是否有腰带物品，只检查Curio槽
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
    static void triggerTransformation(EliteMonsterNpc eliteMonster, ItemStack beltStack) {
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
            
            // 为非玩家实体强制设置showing状态并禁用其他状态
            driver.setShowing(beltStack, true);
            driver.setActive(beltStack, false);
            driver.setHenshin(beltStack, false);
            driver.setRelease(beltStack, false);
            
            // 检查是否已经标记为变身状态
            boolean isAlreadyTransformed = eliteMonster.isTransformed();
            // 检查是否已经装备了盔甲
            boolean isArmorEquipped = !eliteMonster.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            
            // 综合判断是否已经完成变身
            boolean isFullyTransformed = isAlreadyTransformed && isArmorEquipped;
            
            // 为非玩家实体处理变身动画
            if (!isFullyTransformed) {
                // 对于首次变身，触发变身动画序列 - 注释掉以解决单人游戏中无故发送cherry_move数据包的问题
            // driver.startHenshinAnimation(eliteMonster, beltStack);
            } else {
                // 对于已变身的实体，确保它直接使用show动画而不是尝试播放idles
                // 在客户端，我们需要直接触发show动画
                if (eliteMonster.level().isClientSide) {
                    driver.triggerAnim(eliteMonster, "controller", "show");
                }
            }
            
            // 根据腰带模式为精英怪物装备对应的盔甲并播放变身音效
            switch (mode) {
                case LEMON -> {
                    // Duke 盔甲
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.DukeRiderSkill.setArmor(eliteMonster);
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
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.ZangetsuShinSkill.setArmor(eliteMonster);
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
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.SigurdSkill.setArmor(eliteMonster);
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
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.MarikaSkill.setArmor(eliteMonster);
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
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.TyrantSkill.setArmor(eliteMonster);
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
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.DukeRiderSkill.setArmor(eliteMonster);
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
            // 这里变成对应黑暗橙子盔甲！
            // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
            com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.DarkOrangeAngelsSkill.setArmor(eliteMonster);
            if (!isOnSoundCooldown(eliteMonster)) {
                eliteMonster.level().playSound(
                        null,
                        eliteMonster.getX(),
                        eliteMonster.getY(),
                        eliteMonster.getZ(),
                        ModBossSounds.JIMBAR_LEMON.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
                setSoundCooldown(eliteMonster);
            }
        } else if (beltStack.getItem() instanceof GhostDriver) {
            // GhostDriver特定处理
            GhostDriver driver = (GhostDriver) beltStack.getItem();
            GhostDriver.BeltMode mode = driver.getMode(beltStack);
            
            // 根据不同模式装备不同盔甲
            switch (mode) {
                case DARK_RIDER_EYE -> {
                    // 装备Dark Rider Eye盔甲
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.DarkRiderGhostSkill.setArmor(eliteMonster);
                    // 播放变身音效
                    if (!isOnSoundCooldown(eliteMonster)) {
                        //黑暗灵骑和拿破仑魂音效！
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.DARK_GHOST.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        setSoundCooldown(eliteMonster);

                    }
                }
                case NAPOLEON_GHOST -> {
                    // 装备Napoleon Ghost盔甲
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.NapoleonGhostSkill.setArmor(eliteMonster);
                    //播放拿破仑魂音效！
                    if (!isOnSoundCooldown(eliteMonster)) {
                        eliteMonster.level().playSound(
                                null,
                                eliteMonster.getX(),
                                eliteMonster.getY(),
                                eliteMonster.getZ(),
                                ModBossSounds.NAPOLEON_GHOST.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F);
                        setSoundCooldown(eliteMonster);
                    }
                }
                default -> {
                    // 默认模式，装备基础Ghost盔甲
                    // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.DarkRiderGhostSkill.setArmor(eliteMonster);
                }
            }

        } else if (beltStack.getItem() instanceof Two_sidriver) {
            // Two_sidriver特定处理
            // 这里变成对应的evil盔甲！
            // 调用技能类的setArmor方法，这样会同时设置盔甲和武器
            com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.EvilBatsArmorSkill.setArmor(eliteMonster);
            if (!isOnSoundCooldown(eliteMonster)) {
                eliteMonster.level().playSound(
                        null,
                        eliteMonster.getX(),
                        eliteMonster.getY(),
                        eliteMonster.getZ(),
                        ModBossSounds.EVILR.get(),
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
               stack.getItem() instanceof GhostDriver ||
               stack.getItem() instanceof sengokudrivers_epmty ||
               stack.getItem() instanceof Two_sidriver;
    }
    
    /**
     * 为已变身的百界众自动装备随机腰带并触发变身效果
     * 用于生成时已经设置为变身状态的百界众
     */
    public static void equipRandomDriverAndTransform(EliteMonsterNpc eliteMonster) {
        if (eliteMonster.level().isClientSide()) {
            return;
        }
        
        // 随机选择一种腰带类型
        RandomSource random = eliteMonster.getRandom();
        ItemStack driverStack;
        
        // 50%概率装备Genesis_driver (百界众腰带)
        if (random.nextDouble() < 0.5) {
            driverStack = new ItemStack(ModItems.GENESIS_DRIVER.get());
            Genesis_driver driver = (Genesis_driver) driverStack.getItem();
            
            // 随机选择一种模式
            Genesis_driver.BeltMode[] modes = Genesis_driver.BeltMode.values();
            Genesis_driver.BeltMode selectedMode = modes[random.nextInt(modes.length)];
            driver.setMode(driverStack, selectedMode);
            
            // 设置为显示状态
            driver.setShowing(driverStack, true);
        } 
        // 25%概率装备GhostDriver
        else if (random.nextDouble() < 0.5) {
            driverStack = new ItemStack(ModItems.GHOST_DRIVER.get());
            GhostDriver driver = (GhostDriver) driverStack.getItem();
            
            // 随机选择一种模式
            GhostDriver.BeltMode[] modes = GhostDriver.BeltMode.values();
            GhostDriver.BeltMode selectedMode = modes[random.nextInt(modes.length)];
            driver.setMode(driverStack, selectedMode);
        }
        // 25%概率装备Two_sidriver
        else {
            driverStack = new ItemStack(ModItems.TWO_SIDRIVER.get());
        }
        
        // 尝试装备到Curio槽位
        if (BeltCurioIntegration.tryEquipBeltToCurioSlot(eliteMonster, driverStack)) {
            // 直接触发变身逻辑
            triggerTransformation(eliteMonster, driverStack);
        }
    }
    
    // 辅助方法：装备盔甲，只在当前槽位为空时装备
    private static void equipArmor(LivingEntity entity, EquipmentSlot slot, ItemStack armorStack) {
        if (entity.getItemBySlot(slot).isEmpty()) {
            entity.setItemSlot(slot, armorStack);
        }
    }
}