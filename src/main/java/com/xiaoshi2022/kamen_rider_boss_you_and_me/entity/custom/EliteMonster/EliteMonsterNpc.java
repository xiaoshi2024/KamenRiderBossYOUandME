package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.EliteMonsterFindBeltGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DukeKnightEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * 具体的NPC精英怪物实体 - 原版实现
 */
public class EliteMonsterNpc extends AbstractEliteMonster {

    // 盔甲类型常量
    public static final int ARMOR_TYPE_DEFAULT = 0;
    public static final int ARMOR_TYPE_EVIL_RIDER = 1;
    public static final int ARMOR_TYPE_DARK_KNIGHT = 2;
    public static final int ARMOR_TYPE_SHADOW_ASSASSIN = 3;
    public static final int ARMOR_TYPE_DUKE_RIDER = 4;
    


    public EliteMonsterNpc(EntityType<? extends EliteMonsterNpc> entityType, Level level) {
        super(entityType, level);
    }



    @Override
    public String getArmorTexture() {
        // 这个方法现在主要由渲染器使用，这里返回默认路径
        return "textures/entity/elite_monster/base.png";
    }
    
    @Override
    protected void populateDefaultEquipment() {
        // 设置默认装备
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

        // 根据盔甲类型设置不同的盔甲
        int armorType = this.getArmorType();
        if (armorType != ARMOR_TYPE_DEFAULT && this.isShowArmor()) {
            switch (armorType) {
                case ARMOR_TYPE_EVIL_RIDER:
                    this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    break;
                case ARMOR_TYPE_DARK_KNIGHT:
                    this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
                    this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
                    break;
                case ARMOR_TYPE_DUKE_RIDER:
                    // Duke骑士的装备
                    this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
                    this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                    break;
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractEliteMonster.createAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }
    
    /**
     * 重写isInvisible方法，实现变身状态下的隐身功能
     */
    @Override
    public boolean isInvisible() {
        // 当实体处于变身状态时，返回true表示隐身
        return this.isTransformed() || super.isInvisible();
    }
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // 添加近战攻击目标
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        
        // 添加腰带拾取目标
        this.goalSelector.addGoal(3, new EliteMonsterFindBeltGoal(this));
    }
    
    @Override
    protected boolean shouldUseSkill() {
        // 只有Duke Rider类型的精英怪物才能使用Duke骑士技能
        if (this.getArmorType() != ARMOR_TYPE_DUKE_RIDER) {
            return false;
        }
        
        // 获取目标
        LivingEntity target = this.getTarget();
        
        // 只有在有目标且目标在范围内时才考虑使用技能
        if (target != null && isTargetInRange(target)) {
            // 随机决定是否使用技能（30%的概率）
            return this.random.nextFloat() < 0.3f;
        }
        
        return false;
    }
    
    @Override
    protected void useSkill() {
        // 只有Duke Rider类型的精英怪物才能使用此技能
        if (this.getArmorType() != ARMOR_TYPE_DUKE_RIDER) {
            return;
        }
        
        // 召唤Duke骑士
        summonDukeKnights();
        
        // 设置冷却时间
        setSkillCooldown(getDefaultSkillCooldown());
    }
    
    // 召唤Duke骑士的方法
    private void summonDukeKnights() {
        // 只在服务器端处理
        if (this.level().isClientSide) {
            return;
        }
        
        // 获取主手武器
        ItemStack mainHandItem = this.getMainHandItem();
        
        // 获取当前攻击力
        double attackDamage = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        
        // 召唤2个Duke骑士（比玩家少一个，保持平衡性）
        for (int i = 1; i <= 2; i++) {
            DukeKnightEntity dukeKnight = ModEntityTypes.DUKE_KNIGHT.get().create(this.level());
            if (dukeKnight != null) {
                // 设置攻击顺序
                dukeKnight.setAttackOrder(i);
                
                // 设置是否可以攻击：第一个可以立即攻击，第二个初始不可攻击
                dukeKnight.setCanAttack(i == 1);
                
                // 装备相同的武器
                if (!mainHandItem.isEmpty()) {
                    dukeKnight.setWeapon(mainHandItem);
                }
                
                // 设置攻击力
                dukeKnight.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamage * 0.8); //  slightly weaker than the elite monster
                
                // 设置不同的位置，避免实体重叠
                double angle = Math.toRadians(this.getYRot() + (i - 2) * 20);
                double offsetX = Math.sin(angle) * 1.5;
                double offsetZ = Math.cos(angle) * 1.5;
                dukeKnight.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                
                // 设置目标为当前精英怪物的目标
                LivingEntity target = this.getTarget();
                if (target != null) {
                    dukeKnight.setTarget(target);
                }
                
                // 添加实体到世界
                this.level().addFreshEntity(dukeKnight);
                
                // 生成召唤粒子效果
                for (int j = 0; j < 8; j++) {
                    double x = dukeKnight.getX() + this.random.nextFloat() * dukeKnight.getBbWidth() * 2.0F - dukeKnight.getBbWidth();
                    double y = dukeKnight.getY() + this.random.nextFloat() * dukeKnight.getBbHeight();
                    double z = dukeKnight.getZ() + this.random.nextFloat() * dukeKnight.getBbWidth() * 2.0F - dukeKnight.getBbWidth();
                    
                    this.level().addParticle(
                        ParticleTypes.ENCHANTED_HIT,
                        x, y, z,
                        0, 0, 0
                    );
                }
            }
        }
    }
    
    @Override
    protected long getDefaultSkillCooldown() {
        // Duke Rider的技能冷却时间比玩家长一些，设置为15秒
        return 15000; // 15秒
    }
    

}