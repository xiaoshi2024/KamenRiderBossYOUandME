package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.goal;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.AABB;
import java.util.EnumSet;
import java.util.List;

/**
 * 时劫者寻找村民并将其转化为异类骑士的AI目标
 */
public class TimeJackerAnotherRiderGoal extends Goal {
    private final TimeJackerEntity timeJacker;
    private VillagerEntityMCA targetVillager;
    private int cooldown = 0;
    private static final int CHECK_COOLDOWN = 20; // 每秒检查一次
    private static final double SEARCH_RADIUS = 10.0D; // 搜索半径
    private static final double INTERACTION_DISTANCE = 2.0D; // 互动距离

    public TimeJackerAnotherRiderGoal(TimeJackerEntity timeJacker) {
        this.timeJacker = timeJacker;
        this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE));
    }
    
    @Override
    public boolean canUse() {
        // 如果已经使用过表盘，不再执行
        if (timeJacker.hasUsedAnotherWatch()) {
            return false;
        }
        
        // 如果冷却中，不执行
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        
        // 重置冷却
        cooldown = CHECK_COOLDOWN;
        
        // 寻找附近的MCA村民
        this.findNearbyVillager();
        return this.targetVillager != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !timeJacker.hasUsedAnotherWatch() && 
               this.targetVillager != null && 
               this.targetVillager.isAlive() && 
               this.targetVillager.distanceToSqr(this.timeJacker) <= (SEARCH_RADIUS * 1.5) * (SEARCH_RADIUS * 1.5);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void tick() {
        // 健康检查：当生命值低于阈值时尝试逃离
        if (this.timeJacker.getHealth() <= this.timeJacker.getMaxHealth() * 0.3) {
            // 模拟受到伤害触发逃离逻辑
            // 使用一个假的伤害源和适量伤害值来触发tryEscapeOnHighDamage方法
            this.timeJacker.tryEscapeOnHighDamage(this.timeJacker.damageSources().generic(), this.timeJacker.getMaxHealth() * 0.4f);
        }
        
        // 如果已经使用过表盘或者正在逃离，不再执行转化逻辑
        if (timeJacker.hasUsedAnotherWatch()) {
            this.stop();
            return;
        }
        
        if (this.targetVillager == null || !this.targetVillager.isAlive()) {
            // 如果目标不存在或已死亡，寻找新目标
            this.findNearbyVillager();
            return;
        }
        
        // 移动到时劫者附近
        PathNavigation navigation = this.timeJacker.getNavigation();
        if (!navigation.isDone()) {
            navigation.moveTo(this.targetVillager, 1.0D);
        } else {
            // 如果到达互动距离，转化村民
            if (this.timeJacker.distanceToSqr(this.targetVillager) <= INTERACTION_DISTANCE * INTERACTION_DISTANCE) {
                this.transformVillager();
            } else {
                // 重新计算路径
                navigation.moveTo(this.targetVillager, 1.0D);
            }
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        this.targetVillager = null;
        this.timeJacker.getNavigation().stop();
    }
    
    private void findNearbyVillager() {
        AABB boundingBox = this.timeJacker.getBoundingBox().inflate(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
        
        // 获取附近的所有MCA村民
        List<VillagerEntityMCA> nearbyVillagers = this.timeJacker.level().getEntitiesOfClass(
                VillagerEntityMCA.class,
                boundingBox,
                villager -> villager.isAlive() && 
                           villager != this.timeJacker &&
                           !(villager instanceof TimeJackerEntity) // 排除其他时劫者
        );
        
        // 如果找到了村民，选择第一个作为目标
        if (!nearbyVillagers.isEmpty()) {
            this.targetVillager = nearbyVillagers.get(0);
        } else {
            this.targetVillager = null;
        }
    }

    private void transformVillager() {
        if (this.targetVillager != null && !timeJacker.hasUsedAnotherWatch()) {
            // 调用时劫者的创建异类骑士方法，将村民转化为异类骑士
            this.timeJacker.createAnotherRider(this.targetVillager);
            
            // 清除目标
            this.targetVillager = null;
        }
    }
}