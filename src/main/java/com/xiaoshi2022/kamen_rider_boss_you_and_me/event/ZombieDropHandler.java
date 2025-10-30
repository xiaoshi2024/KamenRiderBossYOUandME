package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class ZombieDropHandler {

    // 当生物死亡并掉落物品时触发
    @SubscribeEvent
    public static void onZombieDeath(LivingDropsEvent event) {
        // 检查是否是僵尸且不是客户端
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Zombie zombie) {
            // 获取僵尸主手物品
            ItemStack mainHandItem = zombie.getMainHandItem();
            
            // 检查主手物品是否是腰带
            if (!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof Genesis_driver) {
                // 移除默认的掉落计算逻辑，确保100%掉落腰带
                event.getDrops().clear(); // 清除所有默认掉落
                
                // 手动添加腰带物品到掉落列表
                // 创建一个副本以确保原始物品的NBT数据也被保留
                ItemStack dropStack = mainHandItem.copy();
                
                // 添加到掉落列表，位置设为僵尸死亡位置
                event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                        zombie.level(),
                        zombie.getX(),
                        zombie.getY(),
                        zombie.getZ(),
                        dropStack
                ));
                
                // 同时也保留僵尸可能掉落的其他装备（如盔甲）
                // 检查并添加头盔
                if (!zombie.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()) {
                    ItemStack helmetStack = zombie.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).copy();
                    event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                            zombie.level(),
                            zombie.getX() + 0.2,
                            zombie.getY(),
                            zombie.getZ() + 0.2,
                            helmetStack
                    ));
                }
                
                // 检查并添加胸甲
                if (!zombie.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()) {
                    ItemStack chestStack = zombie.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).copy();
                    event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                            zombie.level(),
                            zombie.getX() - 0.2,
                            zombie.getY(),
                            zombie.getZ() + 0.2,
                            chestStack
                    ));
                }
                
                // 检查并添加护腿
                if (!zombie.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).isEmpty()) {
                    ItemStack legsStack = zombie.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).copy();
                    event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                            zombie.level(),
                            zombie.getX() + 0.2,
                            zombie.getY(),
                            zombie.getZ() - 0.2,
                            legsStack
                    ));
                }
                
                // 确保掉落事件不会被其他监听器取消
                event.setCanceled(false);
            }
        }
    }
}