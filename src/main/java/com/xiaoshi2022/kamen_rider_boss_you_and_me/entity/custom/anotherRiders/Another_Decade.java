package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.TeleportBehindGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class Another_Decade extends Monster implements GeoEntity {

    /* ---------- Geckolib ---------- */
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    /* ---------- 预知系统 ---------- */
    private static final int PRECOG_COOLDOWN = 20;       // 每 20 tick 跑一次
    private static final int PRECOG_FRAMES   = 4;        // 只存 4 帧
    private static final double PRECOG_RANGE_SQ = 6.0 * 6.0;

    /* 放在字段区，与 precog 列表并列 */
    private static final int TELEPORT_COOLDOWN = 60;   // 3 秒冷却
    public int teleportCooldown = 0;

    private final List<PrecogSnapshot> precog = new ObjectArrayList<>(PRECOG_FRAMES);
    private int precogTimer = 0;

    /* 半血狂暴 */
    private boolean enraged = false;

    public Another_Decade(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // 获取随机源
    public RandomSource getRNG() { return this.random; }

    /* ---------- AI ---------- */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        // 移除不兼容的TeleportBehindGoal
        this.goalSelector.addGoal(2, new PrecogDodgeGoal(this)); // 闪避 goal
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.23D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        // 如果目标是时劫者，不能攻击
        if (target instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity) {
            return false;
        }
        // 否则使用默认行为
        return super.canAttack(target);
    }

    /* ---------- 主 tick ---------- */
    @Override
    public void tick() {
        super.tick();

        // 冷却计时
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

        // 半血狂暴检查
        if (this.getHealth() <= this.getMaxHealth() * 0.5 && !enraged) {
            enraged = true;
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 999999, 1));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 999999, 1));
        }

        // 预知系统更新
        if (precogTimer > 0) {
            precogTimer--;
        } else {
            precogTimer = PRECOG_COOLDOWN;
            Player player = this.level().getNearestPlayer(this, 16.0D);
            if (player != null && this.distanceToSqr(player) < PRECOG_RANGE_SQ) {
                predictPath(player);
            }
        }
    }
    
    @Override
    public void aiStep() {
        super.aiStep();

        // 半血时给予玩家减速效果
        if (enraged) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8.0D));
            for (Player player : players) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // 使用自定义攻击力属性
        if (target instanceof LivingEntity livingTarget) {
            // 获取自定义攻击力值
            double customDamage = this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
            // 应用伤害
            float damage = (float) customDamage;
            // 如果处于狂暴状态，额外增加伤害
            if (enraged) {
                damage *= 1.2F; // 狂暴状态额外20%伤害
            }
            
            boolean result = livingTarget.hurt(this.damageSources().mobAttack(this), damage);
            if (result) {
                hurtNearbyEntities();
                // 播放攻击音效
                this.playSound(SoundEvents.GENERIC_HURT, 1.0F, 1.0F);
            }
            return result;
        }
        return false;
    }

    public void hurtNearbyEntities() {
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(3.0D), entity -> 
                        entity != this && entity instanceof Player && 
                        !(entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity));

        // 获取自定义攻击力的一半作为范围伤害
        double customDamage = this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
        float areaDamage = (float) (customDamage * 0.5);
        
        for (LivingEntity entity : nearbyEntities) {
            // 对周围敌人造成额外伤害
            entity.hurt(this.damageSources().mobAttack(this), areaDamage);
            // 击退效果
            Vec3 vec3 = entity.position().subtract(this.position()).normalize().scale(0.5D);
            entity.push(vec3.x, 0.2D, vec3.z);
        }
    }

    private void predictPath(Player player) {
        // 预测玩家路径
        if (precog.size() >= PRECOG_FRAMES) {
            precog.remove(0);
        }
        precog.add(PrecogSnapshot.of(player.position(), player.getDeltaMovement()));
    }

    public Vec3 getPrecog(int ticks) {
        if (precog.isEmpty() || ticks >= precog.size()) {
            return null;
        }
        return precog.get(ticks).pos.add(precog.get(ticks).vel.scale(ticks * 0.5));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (state.isMoving()) {
                state.setAnimation(WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "attackController", 2, state -> {
            if (this.swinging && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                state.setAnimation(ATTACK);
                this.swinging = false;
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private static class PrecogDodgeGoal extends Goal {
        private final Another_Decade mob;
        private int strafeTimer = 0;

        PrecogDodgeGoal(Another_Decade mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.JUMP)); // 只抢跳跃权
        }

        @Override
        public boolean canUse() {
            Player player = mob.level().getNearestPlayer(mob, 16.0D);
            if (player == null || mob.distanceToSqr(player) > PRECOG_RANGE_SQ) {
                return false;
            }
            
            Vec3 predictedPos = mob.getPrecog(1);
            if (predictedPos == null) {
                return false;
            }
            
            // 计算预测位置与自身的距离
            double distance = predictedPos.distanceTo(mob.position());
            return distance < 2.0D && strafeTimer == 0;
        }

        @Override
        public void tick() {
            if (strafeTimer == 0) {
                // 随机向左右闪避
                double strafe = mob.random.nextBoolean() ? 1.0D : -1.0D;
                mob.push(strafe, 0.3D, 0.0D);
                strafeTimer = 20; // 冷却20tick
            } else {
                strafeTimer--;
            }
        }
    }

    private static final class PrecogSnapshot {
        Vec3 pos;
        Vec3 vel;
        static PrecogSnapshot of(Vec3 p, Vec3 v) {
            PrecogSnapshot s = new PrecogSnapshot();
            s.pos = p;
            s.vel = v;
            return s;
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hit) {
        super.dropCustomDeathLoot(source, looting, hit);

        /* 1. 必掉带剩余耐久的表盘 - DCD模式 */
        int remain = this.getPersistentData().getInt("DropDamage");
        ItemStack disk = new ItemStack(ModItems.AIZIOWC.get());
        // 设置为DCD模式
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.setUseCount(disk, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.MAX_DAMAGE - remain);
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc item = (com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc) disk.getItem();
        item.switchMode(disk, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.Mode.DCD);
        spawnAtLocation(disk);

        /* 2. 额外 2-3 个骑士电路板 */
        ItemStack boards = new ItemStack(
                com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.RIDER_CIRCUIT_BOARD.get(),
                2 + this.random.nextInt(2));
        spawnAtLocation(boards);

        /* 3. 把村民还原 */
        CompoundTag villagerTag = this.getPersistentData().getCompound("StoredVillager");
        String entityId = villagerTag.getString("id");
        EntityType<?> originalType = EntityType.byString(entityId)
                .orElse(EntitiesMCA.MALE_VILLAGER.get());
        VillagerEntityMCA restored = (VillagerEntityMCA) originalType.create((ServerLevel) this.level());
        restored.load(villagerTag);
        restored.setPos(this.getX(), this.getY(), this.getZ());
        restored.setHealth(restored.getMaxHealth());
        ((ServerLevel) this.level()).addFreshEntity(restored);
    }

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }
}