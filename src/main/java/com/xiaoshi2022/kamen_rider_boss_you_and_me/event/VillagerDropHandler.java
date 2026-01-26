package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class VillagerDropHandler {

    // 当实体死亡并掉落物品时触发
    @SubscribeEvent
    public static void onVillagerDeath(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查是否是MCA村民
        if (MCAUtil.isVillagerEntityMCA(entity)) {
            // 获取村民主手物品
            ItemStack mainHandStack = entity.getMainHandItem();
            
            // 检查是否手持Genesis_driver腰带
            if (mainHandStack.getItem() instanceof Genesis_driver) {
                // 创建一个新的物品栈，复制村民主手的腰带
                ItemStack dropStack = mainHandStack.copy();
                
                // 创建物品实体并添加到掉落列表中
                ItemEntity itemEntity = new ItemEntity(
                        entity.level(),
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        dropStack
                );
                
                // 添加到掉落事件中
                event.getDrops().add(itemEntity);
            }
        }
    }
}