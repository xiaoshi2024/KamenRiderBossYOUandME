package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * 腰带装备辅助类 - 提供简便方法为精英怪物装备腰带
 */
public class BeltEquipHelper {
    
    /**
     * 为精英怪物装备Genesis Driver腰带，并设置为指定模式
     * @param eliteMonster 精英怪物实体
     * @param mode 腰带模式 (LEMON, MELON, CHERRY, PEACH, DRAGONFRUIT)
     */
    public static void equipGenesisDriver(EliteMonsterNpc eliteMonster, Genesis_driver.BeltMode mode) {
        // 创建Genesis Driver物品栈
        ItemStack beltStack = new ItemStack(ModItems.GENESIS_DRIVER.get());
        
        // 设置腰带模式
        Genesis_driver driver = (Genesis_driver) beltStack.getItem();
        driver.setMode(beltStack, mode);
        
        // 装备到主手（也可以选择Curio槽，但需要额外处理）
        eliteMonster.setItemSlot(EquipmentSlot.MAINHAND, beltStack);
        
        // 直接触发变身（可选）
        EliteMonsterEquipHandler.triggerTransformation(eliteMonster, beltStack);
    }
    
    /**
     * 为精英怪物装备Sengoku Driver腰带
     * @param eliteMonster 精英怪物实体
     */
    public static void equipSengokuDriver(EliteMonsterNpc eliteMonster) {
        // 创建Sengoku Driver物品栈
        ItemStack beltStack = new ItemStack(ModItems.SENGOKUDRIVERS_EPMTY.get());
        
        // 装备到主手
        eliteMonster.setItemSlot(EquipmentSlot.MAINHAND, beltStack);
        
        // 直接触发变身
        EliteMonsterEquipHandler.triggerTransformation(eliteMonster, beltStack);
    }
    
    /**
     * 为精英怪物装备Two-Si Driver腰带
     * @param eliteMonster 精英怪物实体
     */
    public static void equipTwoSiDriver(EliteMonsterNpc eliteMonster) {
        // 创建Two-Si Driver物品栈
        ItemStack beltStack = new ItemStack(ModItems.TWO_SIDRIVER.get());
        
        // 装备到主手
        eliteMonster.setItemSlot(EquipmentSlot.MAINHAND, beltStack);
        
        // 直接触发变身
        EliteMonsterEquipHandler.triggerTransformation(eliteMonster, beltStack);
    }
    
    /**
     * 为精英怪物装备Ghost Driver腰带，并设置为指定模式
     * @param eliteMonster 精英怪物实体
     * @param mode 腰带模式 (DEFAULT, DARK_RIDER_EYE, NAPOLEON_GHOST)
     */
    public static void equipGhostDriver(EliteMonsterNpc eliteMonster, GhostDriver.BeltMode mode) {
        // 创建Ghost Driver物品栈
        ItemStack beltStack = new ItemStack(ModItems.GHOST_DRIVER.get());
        
        // 设置腰带模式
        GhostDriver driver = (GhostDriver) beltStack.getItem();
        driver.setMode(beltStack, mode);
        
        // 装备到主手
        eliteMonster.setItemSlot(EquipmentSlot.MAINHAND, beltStack);
        
        // 直接触发变身
        EliteMonsterEquipHandler.triggerTransformation(eliteMonster, beltStack);
    }
    
    /**
     * 使用命令为精英怪物装备腰带的示例（可以在游戏中使用）
     * 
     * 示例命令格式：
     * /summon kamen_rider_boss_you_and_me:elite_monster_npc ~ ~1 ~ {HandItems:[{id:"kamen_rider_boss_you_and_me:genesis_driver",Count:1b,tag:{BeltMode:"LEMON"}},{}]}
     * /summon kamen_rider_boss_you_and_me:elite_monster_npc ~ ~1 ~ {HandItems:[{id:"kamen_rider_boss_you_and_me:ghost_driver",Count:1b,tag:{BeltMode:"DARK_RIDER_EYE"}},{}]}
     * 
     * 这将生成一个手持对应腰带的精英怪物
     */
    // 这个方法不需要实现，只是作为注释提供命令示例
}