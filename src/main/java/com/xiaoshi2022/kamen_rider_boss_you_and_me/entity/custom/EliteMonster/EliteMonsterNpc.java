package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.EliteMonsterEquipHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.EliteMonsterFindBeltGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 具体的NPC精英怪物实体 - 原版实现
 */
public class EliteMonsterNpc extends AbstractEliteMonster {

    // 盔甲类型常量
    public static final int ARMOR_TYPE_DEFAULT = 0;
    public static final int ARMOR_TYPE_BARON_LEMON = 1;
    public static final int ARMOR_TYPE_DUKE_RIDER = 2;
    public static final int ARMOR_TYPE_ZANGETSU_SHIN = 3;
    public static final int ARMOR_TYPE_SIGURD = 4;
    public static final int ARMOR_TYPE_MARIKA = 5;
    public static final int ARMOR_TYPE_DARK_ORANGE_ANGELS = 6;
    public static final int ARMOR_TYPE_EVIL_BATS_ARMOR = 7;
    public static final int ARMOR_TYPE_TYRANT = 8;
    public static final int ARMOR_TYPE_DARK_KIVA = 9;
    public static final int ARMOR_TYPE_DARK_RIDER_GHOST = 10;
    public static final int ARMOR_TYPE_NAPOLEON_GHOST = 11;

    public EliteMonsterNpc(EntityType<? extends EliteMonsterNpc> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    @javax.annotation.Nullable
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor,
                                                                   net.minecraft.world.DifficultyInstance difficulty,
                                                                   net.minecraft.world.entity.MobSpawnType spawnType,
                                                                   @javax.annotation.Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData,
                                                                   @javax.annotation.Nullable net.minecraft.nbt.CompoundTag compoundTag) {
        net.minecraft.world.entity.SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficulty, spawnType, spawnGroupData, compoundTag);
        
        // 30%概率生成已变身的百界众
        if (this.random.nextDouble() < 0.3) {
            // 标记为已变身
            this.setTransformed(true);
            
            // 设置血量
            this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(80.0D);
            this.setHealth(80.0F);
            
            // 强制刷新实体尺寸和状态
            this.refreshDimensions();
            
            // 调用新方法装备随机腰带并触发变身
            EliteMonsterEquipHandler.equipRandomDriverAndTransform(this);
        }
        
        return data;
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
                case ARMOR_TYPE_BARON_LEMON:
                    LemonBaronSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_DUKE_RIDER:
                    DukeRiderSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_ZANGETSU_SHIN:
                    ZangetsuShinSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_SIGURD:
                    SigurdSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_MARIKA:
                    MarikaSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_DARK_ORANGE_ANGELS:
                    DarkOrangeAngelsSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_EVIL_BATS_ARMOR:
                    EvilBatsArmorSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_TYRANT:
                    TyrantSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_DARK_KIVA:
                    DarkKivaSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_DARK_RIDER_GHOST:
                    DarkRiderGhostSkill.setArmor(this);
                    break;
                case ARMOR_TYPE_NAPOLEON_GHOST:
                    NapoleonGhostSkill.setArmor(this);
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

        // 添加近战攻击目标，优先级为3，这样怪物会优先使用武器攻击
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        
        // 保留腰带拾取目标
        this.goalSelector.addGoal(2, new EliteMonsterFindBeltGoal(this));
    }

    @Override
    protected boolean shouldUseSkill() {
        // 获取目标
        LivingEntity target = this.getTarget();

        // 只有在有目标且目标在范围内时才考虑使用技能
        if (target != null && isTargetInRange(target)) {
            // 提高技能使用概率，从25%增加到40%，让技能更容易触发
            return this.random.nextFloat() < 0.4f;
        }

        return false;
    }
    
    @Override
    protected void useSkill() {
        // 随机决定使用哪种技能
        // 降低骑士踢概率，让更多使用特定武器技能
        if (this.random.nextFloat() < 0.2f) { // 降低到20%
            // 使用骑士踢
            performRiderKick();
        } else {
            // 根据盔甲类型使用特定技能
            switch (this.getArmorType()) {
                case ARMOR_TYPE_BARON_LEMON:
                    LemonBaronSkill.perform(this);
                    break;
                case ARMOR_TYPE_DUKE_RIDER:
                    DukeRiderSkill.perform(this);
                    break;
                case ARMOR_TYPE_ZANGETSU_SHIN:
                    ZangetsuShinSkill.perform(this);
                    break;
                case ARMOR_TYPE_SIGURD:
                    SigurdSkill.perform(this);
                    break;
                case ARMOR_TYPE_MARIKA:
                    MarikaSkill.perform(this);
                    break;
                case ARMOR_TYPE_DARK_ORANGE_ANGELS:
                    DarkOrangeAngelsSkill.perform(this);
                    break;
                case ARMOR_TYPE_EVIL_BATS_ARMOR:
                    EvilBatsArmorSkill.perform(this);
                    break;
                case ARMOR_TYPE_TYRANT:
                    TyrantSkill.perform(this);
                    break;
                case ARMOR_TYPE_DARK_KIVA:
                    DarkKivaSkill.perform(this);
                    break;
                case ARMOR_TYPE_DARK_RIDER_GHOST:
                    DarkRiderGhostSkill.perform(this);
                    break;
                case ARMOR_TYPE_NAPOLEON_GHOST:
                    NapoleonGhostSkill.perform(this);
                    break;
                default:
                    // 默认使用骑士踢
                    performRiderKick();
                    break;
            }
        }

        // 设置冷却时间 - 这里会自动转换为游戏tick
        setSkillCooldown(getDefaultSkillCooldown());
    }

    // ========== 通用技能方法 ==========

    /**
     * 执行骑士踢技能
     */
    private void performRiderKick() {
        // 只在服务器端处理
        if (this.level().isClientSide) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        // 骑士踢的动画效果将由客户端处理
        // 在这里设置一个标志，表示正在执行骑士踢
        this.setIsPerformingRiderKick(true);

        // 计算从当前位置到目标的方向
        double directionX = target.getX() - this.getX();
        double directionZ = target.getZ() - this.getZ();
        double distance = Math.sqrt(directionX * directionX + directionZ * directionZ);

        if (distance > 0) {
            // 标准化方向向量
            directionX /= distance;
            directionZ /= distance;

            // 向目标方向跳跃并冲刺
            this.push(directionX * 2.0, 0.8, directionZ * 2.0);

            // 创建踢前蓄力效果
            createKickChargeEffect(serverLevel, this.getX(), this.getY(), this.getZ());

            // 创建一个任务，在延迟后对目标造成伤害
            double finalDirectionX = directionX;
            double finalDirectionZ = directionZ;
            this.level().getServer().execute(() -> {
                if (this.isAlive() && target.isAlive()) {
                    // 计算最终伤害（基于基础攻击力，骑士踢有伤害加成）
                    double baseDamage = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    double kickDamage = baseDamage * 1.5; // 50%伤害加成

                    // 对目标造成伤害
                    boolean damaged = target.hurt(this.damageSources().mobAttack(this), (float) kickDamage);

                    // 击退效果
                    if (damaged) {
                        target.knockback(1.5, finalDirectionX, finalDirectionZ);
                    }

                    // 生成踢中时的冲击效果
                    createKickImpactEffect(serverLevel, target.getX(), target.getY(), target.getZ(), finalDirectionX, finalDirectionZ);

                    // 创建踢后的残像效果
                    createAfterimageEffect(serverLevel, this.getX(), this.getY(), this.getZ());
                }

                // 重置骑士踢状态
                this.setIsPerformingRiderKick(false);
            });
        }
    }

    /**
     * 创建骑士踢蓄力效果
     */
    private void createKickChargeEffect(ServerLevel level, double x, double y, double z) {
        // 生成脚部蓄力光效
        for (int i = 0; i < 20; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.8;
            double offsetY = this.random.nextDouble() * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.8;

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0,
                    0,
                    0.1
            );

            level.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.05,
                    0,
                    0.05
            );
        }
    }

    /**
     * 创建骑士踢冲击效果
     */
    private void createKickImpactEffect(ServerLevel level, double x, double y, double z, double directionX, double directionZ) {
        // 核心冲击效果
        for (int i = 0; i < 25; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
            double offsetY = this.random.nextDouble() * 1.2;
            double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;

            // 冲击方向的粒子
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    directionX * 0.8,
                    0.4,
                    directionZ * 0.8,
                    0.2
            );

            // 爆炸状粒子
            level.sendParticles(
                    ParticleTypes.EXPLOSION,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0,
                    0,
                    0.05
            );
        }

        // 环形扩散效果
        for (int i = 0; i < 36; i += 4) {
            double angle = Math.toRadians(i);
            double spreadX = Math.cos(angle) * 1.2;
            double spreadZ = Math.sin(angle) * 1.2;

            level.sendParticles(
                    ParticleTypes.FLAME,
                    x,
                    y + 0.5,
                    z,
                    1,
                    spreadX,
                    0,
                    spreadZ,
                    0.1
            );
        }
    }

    /**
     * 创建踢后的残像效果
     */
    private void createAfterimageEffect(ServerLevel level, double x, double y, double z) {
        // 创建三个残像
        for (int i = 0; i < 3; i++) {
            double afterimageX = x - (this.random.nextDouble() - 0.5) * 0.8;
            double afterimageY = y;
            double afterimageZ = z - (this.random.nextDouble() - 0.5) * 0.8;

            level.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    afterimageX,
                    afterimageY,
                    afterimageZ,
                    5,
                    0,
                    0.2,
                    0,
                    0.05
            );

            level.sendParticles(
                    ParticleTypes.WITCH,
                    afterimageX,
                    afterimageY,
                    afterimageZ,
                    3,
                    0,
                    0.1,
                    0,
                    0.03
            );
        }
    }

    // 用于跟踪是否正在执行骑士踢的标志
    private boolean isPerformingRiderKick = false;

    public boolean isPerformingRiderKick() {
        return this.isPerformingRiderKick;
    }

    public void setIsPerformingRiderKick(boolean performing) {
        this.isPerformingRiderKick = performing;
    }

    @Override
    protected long getDefaultSkillCooldown() {
        // 骑士踢技能冷却时间设置为10秒
        return 10000; // 10秒
    }

    // 检查目标是否在攻击范围内
    protected boolean isTargetInRange(LivingEntity target) {
        if (target == null) {
            return false;
        }
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        // 骑士踢的有效范围比普通攻击稍大
        return this.distanceToSqr(target) <= (followRange * followRange * 1.5);
    }
}