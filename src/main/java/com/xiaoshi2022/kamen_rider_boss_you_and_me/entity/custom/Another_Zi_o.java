package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.TeleportBehindGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
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
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class Another_Zi_o extends Monster implements GeoEntity {

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

    public Another_Zi_o(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // 在 Another_Zi_o 里加一行
    public RandomSource getRNG() { return this.random; }

    /* ---------- AI ---------- */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        /* 在 registerGoals 里追加一行 */
        this.goalSelector.addGoal(2, new TeleportBehindGoal(this));
        this.goalSelector.addGoal(2, new PrecogDodgeGoal(this)); // 闪避 goal
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.23D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    /* ---------- 主 tick ---------- */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        /* 半血狂暴 */
        if (!enraged && this.getHealth() < this.getMaxHealth() / 2) {
            enraged = true;
            this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 1.0F);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35);
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 1));
        }

        /* 更新预测 */
        if (--precogTimer <= 0) {
            precogTimer = PRECOG_COOLDOWN;
            precog.clear();
            if (getTarget() instanceof Player p && distanceToSqr(p) < PRECOG_RANGE_SQ) {
                predictPath(p);
            }
        }
        if (teleportCooldown > 0) teleportCooldown--;
    }

    /* ---------- 预测轨迹 ---------- */
    private void predictPath(Player player) {
        Vec3 pos = player.position();
        Vec3 vel = player.getDeltaMovement();
        for (int i = 0; i < PRECOG_FRAMES; i++) {
            vel = vel.add(0, -0.08, 0).scale(0.98);
            pos = pos.add(vel);
            if (pos.y < 0) pos = new Vec3(pos.x, 0, pos.z);
            precog.add(PrecogSnapshot.of(pos, vel));
        }
    }

    /* ---------- 给 AI 用的接口 ---------- */
    public Vec3 getPrecog(int ticks) {
        int idx = ticks / 10;
        return idx < 0 || idx >= precog.size() ? null : precog.get(idx).pos;
    }

    /* ---------- Geckolib ---------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 5, e ->
                e.isMoving() ? e.setAndContinue(WALK) : e.setAndContinue(IDLE)));
        controllers.add(new AnimationController<>(this, "attack", 5, e ->
                e.getController().getAnimationState() == AnimationController.State.STOPPED && swinging
                        ? e.setAndContinue(ATTACK) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    /* ---------- 闪避 Goal ---------- */
    private static class PrecogDodgeGoal extends Goal {
        private final Another_Zi_o mob;
        private int strafeTimer = 0;

        PrecogDodgeGoal(Another_Zi_o mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.JUMP)); // 只抢跳跃权
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() instanceof Player
                    && mob.distanceToSqr(mob.getTarget()) < PRECOG_RANGE_SQ;
        }

        @Override
        public void tick() {
            Vec3 future = mob.getPrecog(10);
            if (future == null) return;

            Vec3 to = future.subtract(mob.position()).normalize();
            if (to.dot(mob.getLookAngle()) > 0.8) {
                if (--strafeTimer <= 0) {
                    Vec3 side = new Vec3(-to.z, 0, to.x).normalize();
                    side = mob.getRandom().nextBoolean() ? side : side.scale(-1);
                    mob.setDeltaMovement(side.scale(0.4));
                    strafeTimer = 20;
                }
            }
        }
    }

    /* ---------- 快照 ---------- */
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

    /* ---------- 掉落 ---------- */
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hit) {
        super.dropCustomDeathLoot(source, looting, hit);

        /* 1. 必掉带剩余耐久的表盘 */
        int remain = this.getPersistentData().getInt("DropDamage");
        ItemStack disk = new ItemStack(ModItems.AIZIOWC.get());
        aiziowc.setUseCount(disk, aiziowc.MAX_DAMAGE - remain);
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
}