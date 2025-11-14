package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.GhostEyeDimensionHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Gamma_s_Entity extends Monster {

    public Gamma_s_Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // 注册实体属性
    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 56.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 29.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D).build();
    }

    // 注册AI目标
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.23D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // 只攻击非眼魔玩家
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> {
            // 检查玩家是否是眼魔种族
            return !GhostEyeDimensionHandler.isGhostEyePlayer((Player) player);
        }));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return target.hurt(this.damageSources().mobAttack(this), damage);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 可以在这里添加特殊的伤害处理逻辑
        return super.hurt(source, amount);
    }

    // 使用原版动画，不需要GeckoLib相关代码
    
    @Override
    public boolean isCustomNameVisible() {
        return false; // 隐藏实体ID显示
    }
    
    // 确保实体ID不显示
    @Override
    public boolean shouldShowName() {
        return false;
    }
}