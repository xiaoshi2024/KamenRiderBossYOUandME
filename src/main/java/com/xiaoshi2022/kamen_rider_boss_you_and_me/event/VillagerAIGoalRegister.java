package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.goal.PickUpDriverGoal;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class VillagerAIGoalRegister {

    // 当实体加入世界时触发
    @SubscribeEvent
    public static void onVillagerJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否是MCA村民
        if (event.getEntity() instanceof VillagerEntityMCA villager) {
            // 为村民添加捡取腰带的AI目标
            // 设置优先级为3，介于LookAtPlayerGoal(通常优先级为1)和RandomStrollGoal(通常优先级为4)之间
            villager.goalSelector.addGoal(3, new PickUpDriverGoal(villager));
            
            // 为村民添加对时劫者的反应行为
            // 这里我们添加一个目标，让村民能够识别并躲避时劫者
            try {
                // 使用反射添加对时劫者的敌对目标，避免编译错误
                Class<?> timeJackerClass = Class.forName("com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity");
                
                // 创建一个目标，让村民能够感知到附近的时劫者
                // 设置较低的优先级，让村民主要专注于日常活动
                villager.targetSelector.addGoal(5, new NearestAttackableTargetGoal(
                    villager, 
                    timeJackerClass, 
                    10, 
                    true, 
                    false, 
                    null
                ));
            } catch (ClassNotFoundException e) {
                // 如果类未找到，忽略这个错误
            }
        }
    }
}