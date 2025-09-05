package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 假面骑士黑暗Kiva能力处理器
 * 实现蝙蝠形态、吸血能力和声波爆破能力
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DarkKivaAbilityHandler {

    /* ====== 数值常量 ====== */
    private static final int BAT_MODE_DURATION = 200;    // 蝙蝠形态持续时间（10秒）
    private static final int BLOOD_SUCK_COOLDOWN = 100;  // 吸血能力冷却（5秒）
    private static final int SONIC_BLAST_COOLDOWN = 200; // 声波爆破冷却（10秒）
    private static final int BLOOD_STEAL_INTERVAL = 100; // 生命偷取间隔（5秒）
    private static final float BLOOD_STEAL_AMOUNT = 2.0f; // 生命偷取量
    
    private static final float BLOOD_SUCK_RANGE = 4.0f;  // 吸血范围
    private static final float BLOOD_SUCK_AMOUNT = 4.0f; // 吸血量
    private static final float SONIC_BLAST_RANGE = 6.0f; // 声波爆破范围
    private static final float SONIC_BLAST_DAMAGE = 6.0f;// 声波爆破伤害

    /* ====== 主 Tick：处理持续效果和冷却 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        // 检查是否穿着全套黑暗Kiva盔甲
        boolean isDarkKiva = DarkKivaItem.isFullArmorEquipped(sp);
        
        // 处理蝙蝠形态
        handleBatMode(sp, variables, isDarkKiva);
        
        // 处理冷却时间
        handleCooldowns(variables, sp.level().getGameTime());
        
        // 更新套装基础效果
        updateBaseEffects(sp, variables, isDarkKiva);
    }

    /* ====== 能力触发方法（可被网络包调用） ====== */

    /**
     * 蝙蝠形态：V键触发，给予飞行能力和速度提升
     */
    public static void tryBatMode(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive()) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        // 检查是否已经处于蝙蝠形态
        if (variables.dark_kiva_bat_mode) {
            // 取消蝙蝠形态
            variables.dark_kiva_bat_mode = false;
            sp.removeEffect(MobEffects.LEVITATION);
            sp.removeEffect(MobEffects.MOVEMENT_SPEED);
            playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
        } else {
            // 激活蝙蝠形态
            variables.dark_kiva_bat_mode = true;
            variables.dark_kiva_bat_mode_time = sp.level().getGameTime();
            sp.addEffect(new MobEffectInstance(MobEffects.LEVITATION, BAT_MODE_DURATION, 0, false, false));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BAT_MODE_DURATION, 1, false, false));
            playSound(sp, SoundEvents.BAT_AMBIENT, 1.0f, 0.8f);
            spawnBatParticles(sp);
        }
        
        variables.syncPlayerVariables(sp);
    }

    /**
     * 吸血能力：B键触发，吸取周围生物的生命
     */
    public static void tryBloodSuck(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive()) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();
        
        // 检查冷却
        if (currentTime < variables.dark_kiva_blood_suck_cooldown) {
            return;
        }
        
        Level level = sp.level();
        AABB box = new AABB(sp.blockPosition()).inflate(BLOOD_SUCK_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, 
                e -> e != sp && !e.isAlliedTo(sp) && e.isAlive());
        
        if (!targets.isEmpty()) {
            // 吸取生命
            float totalHeal = 0.0f;
            for (LivingEntity target : targets) {
                float damage = Math.min(BLOOD_SUCK_AMOUNT, target.getHealth());
                target.hurt(damageSource(sp), damage);
                totalHeal += damage * 0.8f; // 80%的伤害转化为治疗
            }
            
            sp.heal(totalHeal);
            variables.dark_kiva_blood_suck_active = true;
            variables.dark_kiva_blood_suck_cooldown = currentTime + BLOOD_SUCK_COOLDOWN;
            playSound(sp, SoundEvents.PLAYER_HURT_ON_FIRE, 0.8f, 0.9f);
            spawnBloodParticles(sp);
        }
        
        variables.syncPlayerVariables(sp);
    }

    /**
     * 声波爆破：N键触发，范围伤害和击退
     */
    public static void trySonicBlast(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive()) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();
        
        // 检查冷却
        if (currentTime < variables.dark_kiva_sonic_blast_cooldown) {
            return;
        }
        
        Level level = sp.level();
        AABB box = new AABB(sp.blockPosition()).inflate(SONIC_BLAST_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, 
                e -> e != sp && !e.isAlliedTo(sp));
        
        // 对范围内的目标造成伤害和击退
        for (LivingEntity target : targets) {
            target.hurt(damageSource(sp), SONIC_BLAST_DAMAGE);
            target.push(target.position().subtract(sp.position()).normalize().x * 0.5, 0.2, target.position().subtract(sp.position()).normalize().z * 0.5);
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
        }
        
        variables.dark_kiva_sonic_blast_active = true;
        variables.dark_kiva_sonic_blast_cooldown = currentTime + SONIC_BLAST_COOLDOWN;
        playSound(sp, SoundEvents.WITHER_BREAK_BLOCK, 1.0f, 1.5f);
        spawnSoundParticles(sp, SONIC_BLAST_RANGE);
        
        variables.syncPlayerVariables(sp);
    }

    /* ====== 工具方法 ====== */
    
    private static void handleBatMode(ServerPlayer sp, KRBVariables.PlayerVariables variables, boolean isDarkKiva) {
        if (!isDarkKiva) {
            // 如果没有穿全套盔甲，取消蝙蝠形态
            if (variables.dark_kiva_bat_mode) {
                variables.dark_kiva_bat_mode = false;
                sp.removeEffect(MobEffects.LEVITATION);
                sp.removeEffect(MobEffects.MOVEMENT_SPEED);
                variables.syncPlayerVariables(sp);
            }
            return;
        }
        
        // 检查蝙蝠形态是否到期
        if (variables.dark_kiva_bat_mode) {
            long currentTime = sp.level().getGameTime();
            if (currentTime - variables.dark_kiva_bat_mode_time > BAT_MODE_DURATION) {
                variables.dark_kiva_bat_mode = false;
                sp.removeEffect(MobEffects.LEVITATION);
                sp.removeEffect(MobEffects.MOVEMENT_SPEED);
                variables.syncPlayerVariables(sp);
                playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
            }
        }
    }

    private static void handleCooldowns(KRBVariables.PlayerVariables variables, long currentTime) {
        // 重置已完成的冷却标记
        if (variables.dark_kiva_blood_suck_active && currentTime >= variables.dark_kiva_blood_suck_cooldown - BLOOD_SUCK_COOLDOWN / 2) {
            variables.dark_kiva_blood_suck_active = false;
        }
        
        if (variables.dark_kiva_sonic_blast_active && currentTime >= variables.dark_kiva_sonic_blast_cooldown - SONIC_BLAST_COOLDOWN / 2) {
            variables.dark_kiva_sonic_blast_active = false;
        }
    }

    private static void updateBaseEffects(ServerPlayer sp, KRBVariables.PlayerVariables variables, boolean isDarkKiva) {
        long currentTime = sp.level().getGameTime();
        
        if (isDarkKiva) {
            // 夜视效果：只有当玩家没有原版夜视效果时，才添加骑士的夜视效果
            MobEffectInstance nightVision = new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false);
            if (!sp.hasEffect(MobEffects.NIGHT_VISION) || 
                (sp.getEffect(MobEffects.NIGHT_VISION).getDuration() < 260 && 
                sp.getEffect(MobEffects.NIGHT_VISION).getDuration() != Integer.MAX_VALUE)) {
                sp.addEffect(nightVision);
            }
            
            // 攻击力提升：基础伤害增加
            MobEffectInstance damageBoost = new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false);
            if (!sp.hasEffect(MobEffects.DAMAGE_BOOST)) {
                sp.addEffect(damageBoost);
            }
            
            // 火焰抗性：抵抗火焰伤害
            MobEffectInstance fireResistance = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 320, 0, false, false);
            if (!sp.hasEffect(MobEffects.FIRE_RESISTANCE) || sp.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() < 260) {
                sp.addEffect(fireResistance);
            }
            
            // 缓慢生命恢复：体现吸血鬼特性
            MobEffectInstance regeneration = new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false);
            if (!sp.hasEffect(MobEffects.REGENERATION)) {
                sp.addEffect(regeneration);
            }
            
            // 每5秒触发一次吸血，但只在玩家存活时执行
            if (currentTime >= variables.dark_kiva_blood_steal_cooldown && sp.isAlive()) {
                // 简单判定：最近5秒内没有受伤 → 非战斗
                long lastHurt = sp.getLastHurtByMobTimestamp();
                if (currentTime - lastHurt > 100) { // 5秒内没被打
                    sp.heal(BLOOD_STEAL_AMOUNT);
                    variables.dark_kiva_blood_steal_cooldown = currentTime + BLOOD_STEAL_INTERVAL;
                    spawnBatParticles(sp);
                    variables.syncPlayerVariables(sp);
                }
            }
        } else {
            // 当玩家脱下盔甲时，不移除原版药水的夜视效果
            // 只移除其他效果，保留原版药水的夜视效果
            sp.removeEffect(MobEffects.DAMAGE_BOOST);
            sp.removeEffect(MobEffects.FIRE_RESISTANCE);
            sp.removeEffect(MobEffects.REGENERATION);
            
            // 重置冷却时间
            variables.dark_kiva_blood_steal_cooldown = 0;
            variables.syncPlayerVariables(sp);
        }
    }

    private static DamageSource damageSource(ServerPlayer sp) {
        return sp.damageSources().playerAttack(sp);
    }

    private static void playSound(ServerPlayer sp, SoundEvent sound, float volume, float pitch) {
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void spawnBatParticles(ServerPlayer sp) {
        // 粒子效果：可以在这里添加蝙蝠相关的粒子
        // 示例：在玩家周围生成一些烟雾粒子
        Level level = sp.level();
        for (int i = 0; i < 8; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            double dy = sp.getRandom().nextDouble() * 1.5;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, 
                    sp.getX() + dx, sp.getY() + 0.5 + dy, sp.getZ() + dz, 0.0, 0.05, 0.0);
        }
    }

    private static void spawnBloodParticles(ServerPlayer sp) {
        // 吸血粒子效果
        Level level = sp.level();
        for (int i = 0; i < 12; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 1.5;
            double dy = sp.getRandom().nextDouble() * 1.0;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 1.5;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ENTITY_EFFECT, 
                    sp.getX() + dx, sp.getY() + 0.5 + dy, sp.getZ() + dz, 0.8, 0.0, 0.0);
        }
    }

    private static void spawnSoundParticles(ServerPlayer sp, float range) {
        // 声波爆破粒子效果
        Level level = sp.level();
        for (int i = 0; i < 20; i++) {
            double angle = sp.getRandom().nextDouble() * Math.PI * 2;
            double radius = sp.getRandom().nextDouble() * range;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.NOTE, 
                    sp.getX() + dx, sp.getY() + 0.5, sp.getZ() + dz, 
                    sp.getRandom().nextDouble(), sp.getRandom().nextDouble(), sp.getRandom().nextDouble());
        }
    }

    /* 禁止实例化 */
    private DarkKivaAbilityHandler() {}
}