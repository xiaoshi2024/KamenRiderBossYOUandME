package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.goal.PickUpDriverGoal;
import forge.net.mca.entity.VillagerEntityMCA;
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
        }
    }
}