package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.goal.PickUpDriverGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil;
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
        if (MCAUtil.isVillagerEntityMCA(event.getEntity())) {
            // 使用反射获取MCA村民的goalSelector和targetSelector
            try {
                // 获取MCA村民实例
                Object villager = event.getEntity();
                
                // 获取goalSelector字段
                java.lang.reflect.Field goalSelectorField = villager.getClass().getDeclaredField("goalSelector");
                goalSelectorField.setAccessible(true);
                Object goalSelector = goalSelectorField.get(villager);
                
                // 获取addGoal方法
                java.lang.reflect.Method addGoalMethod = goalSelector.getClass().getMethod("addGoal", int.class, net.minecraft.world.entity.ai.goal.Goal.class);
                
                // 为村民添加捡取腰带的AI目标
                // 设置优先级为3，介于LookAtPlayerGoal(通常优先级为1)和RandomStrollGoal(通常优先级为4)之间
                if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob) {
                    addGoalMethod.invoke(goalSelector, 3, new PickUpDriverGoal(mob));
                }
                
                // 获取targetSelector字段
                java.lang.reflect.Field targetSelectorField = villager.getClass().getDeclaredField("targetSelector");
                targetSelectorField.setAccessible(true);
                Object targetSelector = targetSelectorField.get(villager);
                
                // 获取addGoal方法
                java.lang.reflect.Method addTargetGoalMethod = targetSelector.getClass().getMethod("addGoal", int.class, net.minecraft.world.entity.ai.goal.target.TargetGoal.class);
                
                // 为村民添加对时劫者的反应行为
                // 这里我们添加一个目标，让村民能够识别并躲避时劫者
                try {
                    // 使用反射添加对时劫者的敌对目标，避免编译错误
                    Class<?> timeJackerClass = Class.forName("com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity");
                    
                    // 创建一个目标，让村民能够感知到附近的时劫者
                    // 设置较低的优先级，让村民主要专注于日常活动
                    if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob) {
                        NearestAttackableTargetGoal targetGoal = new NearestAttackableTargetGoal(
                            mob, 
                            timeJackerClass, 
                            10, 
                            true, 
                            false, 
                            null
                        );
                        
                        // 调用addGoal方法添加目标
                        addTargetGoalMethod.invoke(targetSelector, 5, targetGoal);
                    }
                    
                    
                } catch (ClassNotFoundException e) {
                    // 如果类未找到，忽略这个错误
                }
            } catch (Exception e) {
                // 如果反射操作失败，忽略这个错误
            }
        }
    }
}