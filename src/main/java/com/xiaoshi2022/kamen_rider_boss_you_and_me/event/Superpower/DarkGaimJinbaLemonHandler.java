package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 假面骑士黑暗铠武·阵羽柠檬
 * 仅当玩家穿戴整套 DarkOrangels 盔甲（头+胸+腿，无靴）时生效
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DarkGaimJinbaLemonHandler {

    /* ====== 数值常量 ====== */
    private static final int MAX_EVIL_AURA   = 100;   // 邪気上限
    private static final int EVIL_AURA_REGEN = 1;     // 每 tick 回复量
    private static final int GUARD_COST      = 10;    // 阵羽护盾消耗
    private static final int BLAST_COST      = 30;    // 黑暗爆破消耗
    private static final int BOOST_COST      = 20;    // 柠檬加速消耗

    private static final float BLAST_DAMAGE   = 8f;   // 爆破伤害
    private static final float BLAST_RADIUS   = 5f;   // 爆破半径
    private static final float COUNTER_CHANCE = 0.30f;// 反击概率
    private static final float COUNTER_RATIO  = 0.50f;// 反击倍率

    /* ====== 主 Tick：邪気回复 + 套装力量 + 套装检测 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;

        boolean isFull = isJinba(sp);

        /* 邪気只在整套时回复 */
        if (isFull) {
            int aura = getEvilAura(sp);
            if (aura < MAX_EVIL_AURA) {
                setEvilAura(sp, Math.min(MAX_EVIL_AURA, aura + EVIL_AURA_REGEN));
            }
        }

        /* 套装力量：仅在整套时存在，脱下即移除 */
        if (isFull) {
            if (!sp.hasEffect(MobEffects.DAMAGE_BOOST)) {
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false));
            }
        } else {
            sp.removeEffect(MobEffects.DAMAGE_BOOST);
        }
        updateBaseBuff(sp);
    }

    /* ====== 能力触发方法（可被网络包直接调用） ====== */

    /** 阵羽护盾：副手右键，消耗邪気，5 秒抗性 I */
    public static void tryJinbaGuard(ServerPlayer sp) {
        if (!isJinba(sp)) return;
        if (!consumeEvilAura(sp, GUARD_COST)) return;
        sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
        sp.getPersistentData().putBoolean("JinbaGuard", true);
        play(sp, SoundEvents.SHIELD_BLOCK, 1f, 1f);
    }

    /** 黑暗爆破：Shift+主手右键，范围伤害+失明+虚弱,不再误伤自己或盟友 */
    public static void tryDarkSquash(ServerPlayer sp) {
        if (!isJinba(sp)) return;
        if (!consumeEvilAura(sp, BLAST_COST)) return;

        Level level = sp.level();
        AABB box = new AABB(sp.blockPosition()).inflate(BLAST_RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != sp && !e.isAlliedTo(sp)); // 排除使用者 & 盟友

        for (LivingEntity le : targets) {
            le.hurt(damageSource(sp), BLAST_DAMAGE);
            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }

        play(sp, SoundEvents.GENERIC_EXPLODE, 1f, 0.8f);
        spawnCloud(level, sp, BLAST_RADIUS);
    }

    /** 柠檬加速：主手持柠檬锁种右键，消耗邪気+锁种，获得速度/跳跃/力量 */
    public static void tryLemonBoost(ServerPlayer sp) {
        if (!isJinba(sp)) return;
        ItemStack seed = sp.getMainHandItem();
        if (!seed.is(ModItems.LEMON_ENERGY.get())) return;
        if (!consumeEvilAura(sp, BOOST_COST)) return;

        seed.shrink(1); // 消耗锁种
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2));
        sp.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
        sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
        play(sp, SoundEvents.BEACON_ACTIVATE, 1f, 1.2f);
    }

    /* ====== 被动：护盾存在时近战反击 ====== */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!isJinba(sp)) return;
        if (!sp.getPersistentData().getBoolean("JinbaGuard")) return;
        if (!(e.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (sp.getRandom().nextFloat() > COUNTER_CHANCE) return;

        attacker.hurt(damageSource(sp), e.getAmount() * COUNTER_RATIO);
        play(sp, SoundEvents.ANVIL_LAND, 0.6f, 1.5f);
    }

    /* ====== 工具方法 ====== */
    /** 是否穿戴整套盔甲（头/胸/腿，无靴） */
    private static boolean isJinba(ServerPlayer sp) {
        return sp.getInventory().armor.get(3).getItem() instanceof Dark_orangels &&
                sp.getInventory().armor.get(2).getItem() instanceof Dark_orangels &&
                sp.getInventory().armor.get(1).getItem() instanceof Dark_orangels;
    }

    private static DamageSource damageSource(ServerPlayer sp) {
        return sp.damageSources().playerAttack(sp);
    }

    private static int getEvilAura(ServerPlayer sp) {
        return sp.getPersistentData().getInt("evil_aura");
    }

    private static void setEvilAura(ServerPlayer sp, int value) {
        sp.getPersistentData().putInt("evil_aura", value);
    }

    private static boolean consumeEvilAura(ServerPlayer sp, int cost) {
        int current = getEvilAura(sp);
        if (current < cost) return false;
        setEvilAura(sp, current - cost);
        return true;
    }

    private static void play(ServerPlayer sp, SoundEvent sound, float volume, float pitch) {
        sp.level().playSound(null, sp, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void spawnCloud(Level level, Player player, float radius) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, player.getX(), player.getY(), player.getZ());
        cloud.setRadius(radius);
        cloud.setDuration(40);
        cloud.setParticle(ModParticles.AONICX_PARTICLE.get());
        cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
        level.addFreshEntity(cloud);
    }

    /* 禁止实例化 */
    private DarkGaimJinbaLemonHandler() {}

    /* ------------------------------------------------------------------
     * 基础常驻 Buff：穿齐盔甲后自动添加，脱下后自动移除
     * ------------------------------------------------------------------ */

    /** 每 tick 检查并维护基础 Buff */
    private static void updateBaseBuff(ServerPlayer sp) {
        boolean isFull = isJinba(sp);
        MobEffectInstance night = new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false);

        if (isFull) {
            // 只有当玩家没有原版夜视效果时，才添加骑士的夜视效果
            if (!sp.hasEffect(MobEffects.NIGHT_VISION) || 
                (sp.getEffect(MobEffects.NIGHT_VISION).getDuration() < 260 && 
                sp.getEffect(MobEffects.NIGHT_VISION).getDuration() != Integer.MAX_VALUE)) {
                sp.addEffect(night);
            }
        } else {
            // 当玩家不再穿着骑士盔甲时，不移除原版药水的夜视效果
            // 不再自动移除夜视效果，保留原版药水的夜视效果
        }
    }
}