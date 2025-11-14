package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class ZombieAIModifier {

    // 当实体加入世界时，为僵尸添加自定义AI目标
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // 检查是否是僵尸且不是客户端
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Zombie zombie) {
            // 为僵尸添加寻找并装备腰带的AI目标
            // 添加在较低优先级，这样僵尸仍然会优先攻击玩家
            addCustomGoalsToZombie(zombie);
        }
    }

    // 为僵尸添加自定义AI目标
    private static void addCustomGoalsToZombie(Zombie zombie) {
        // 添加寻找腰带的AI目标，优先级设为3，提高优先级让僵尸更积极地寻找腰带
        // 优先级数字越小，优先级越高
        zombie.goalSelector.addGoal(3, new FindAndEquipDriverGoal(zombie));
        
        // 注意：Minecraft中AI优先级通常如下：
        // 0: 最高优先级，通常是紧急行为，如受伤时的逃跑
        // 1-3: 基本生存行为，如寻找目标、攻击
        // 4-6: 次要行为，如寻路、回避危险
        // 7-10: 最低优先级，如游荡、观察
    }
}