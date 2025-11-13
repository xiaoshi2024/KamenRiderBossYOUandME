package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import java.util.Random;

/**
 * 精英怪物的抽象基类 - 原版实现
 */
public abstract class AbstractEliteMonster extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ARMOR_TYPE = SynchedEntityData.defineId(AbstractEliteMonster.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_ARMOR = SynchedEntityData.defineId(AbstractEliteMonster.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_TRANSFORMED = SynchedEntityData.defineId(AbstractEliteMonster.class, EntityDataSerializers.BOOLEAN);
    
    // 技能相关的实体数据
    private long skillCooldown = 0;
    private static final long DEFAULT_SKILL_COOLDOWN = 10000; // 默认10秒冷却时间
    protected final Random random = new Random();

    public AbstractEliteMonster(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setShowArmor(true);
    }

    @Override
    protected void registerGoals() {
        // 保留基本行为和被攻击时的反击行为
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 添加近战攻击目标，确保怪物能够主动攻击玩家
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        
        // 添加主动攻击玩家的目标
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // 保留被攻击时的反击行为
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this)).setAlertOthers());
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 更新技能冷却时间
        updateSkillCooldown();
        
        // 尝试使用技能
        if (!this.level().isClientSide && isSkillReady() && shouldUseSkill()) {
            useSkill();
        }
    }
    
    // 更新技能冷却时间 - 使用游戏tick而不是系统时间
    private void updateSkillCooldown() {
        if (skillCooldown > 0) {
            skillCooldown--;
        }
    }
    
    // 检查技能是否准备就绪
    public boolean isSkillReady() {
        return skillCooldown == 0;
    }
    
    // 设置技能冷却时间 - 使用游戏tick（20tick = 1秒）
    protected void setSkillCooldown(long cooldownTime) {
        // 将毫秒转换为游戏tick（约20tick/秒）
        this.skillCooldown = cooldownTime / 50;
    }
    
    // 获取剩余冷却时间（游戏tick）
    public long getRemainingCooldown() {
        return skillCooldown;
    }
    
    // 判断是否应该使用技能（由子类实现）
    protected abstract boolean shouldUseSkill();
    
    // 使用技能（由子类实现）
    protected abstract void useSkill();

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ARMOR, 5.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ARMOR_TYPE, 0);
        this.entityData.define(DATA_SHOW_ARMOR, true);
        this.entityData.define(DATA_IS_TRANSFORMED, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ArmorType")) {
            this.setArmorType(compound.getInt("ArmorType"));
        }
        if (compound.contains("ShowArmor")) {
            this.setShowArmor(compound.getBoolean("ShowArmor"));
        }
        if (compound.contains("IsTransformed")) {
            this.setTransformed(compound.getBoolean("IsTransformed"));
        }
        // 读取技能冷却时间
        if (compound.contains("SkillCooldown")) {
            this.skillCooldown = compound.getLong("SkillCooldown");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ArmorType", this.getArmorType());
        compound.putBoolean("ShowArmor", this.isShowArmor());
        compound.putBoolean("IsTransformed", this.isTransformed());
        // 保存技能冷却时间
        compound.putLong("SkillCooldown", this.skillCooldown);
    }

    // 获取和设置盔甲类型
    public int getArmorType() {
        return this.entityData.get(DATA_ARMOR_TYPE);
    }

    public void setArmorType(int type) {
        this.entityData.set(DATA_ARMOR_TYPE, type);
    }

    // 获取和设置是否显示盔甲
    public boolean isShowArmor() {
        return this.entityData.get(DATA_SHOW_ARMOR);
    }

    public void setShowArmor(boolean show) {
        this.entityData.set(DATA_SHOW_ARMOR, show);
    }

    // 变身状态
    public boolean isTransformed() {
        return this.entityData.get(DATA_IS_TRANSFORMED);
    }

    public void setTransformed(boolean transformed) {
        this.entityData.set(DATA_IS_TRANSFORMED, transformed);
        if (transformed) {
            // 变身时增强属性
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0D);
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
        } else {
            // 恢复默认属性
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.0D);
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0D);
        }
    }

    // 检查盔甲是否可见
    public boolean isArmorVisible() {
        return this.isShowArmor() && this.getArmorType() != 0;
    }

    // 获取盔甲纹理路径
    public abstract String getArmorTexture();

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        this.populateDefaultEquipment();
        return spawnData;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    protected void populateDefaultEquipment() {
        // 子类重写来设置默认装备
    }
    
    // 获取默认技能冷却时间
    protected long getDefaultSkillCooldown() {
        return DEFAULT_SKILL_COOLDOWN;
    }
    
    // 检查目标是否在攻击范围内
    protected boolean isTargetInRange(LivingEntity target) {
        if (target == null) {
            return false;
        }
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        return this.distanceToSqr(target) <= followRange * followRange;
    }
}