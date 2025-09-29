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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkKivaSealBarrierEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
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
    private static final int BAT_MODE_DURATION = 9600;    // 蝙蝠形态持续时间（8分钟）
    private static final int BLOOD_SUCK_COOLDOWN = 100;  // 吸血能力冷却（5秒）
    private static final int SONIC_BLAST_COOLDOWN = 200; // 声波爆破冷却（10秒）
    private static final int BLOOD_STEAL_INTERVAL = 100; // 生命偷取间隔（5秒）
    private static final float BLOOD_STEAL_AMOUNT = 2.0f; // 生命偷取量
    private static final int FUUIN_KEKKAI_COOLDOWN = 300; // 封印结界冷却（15秒）
    private static final float FUUIN_KEKKAI_DAMAGE = 100.0f; // 封印结界伤害
    
    private static final float BLOOD_SUCK_RANGE = 4.0f;  // 吸血范围
    private static final float BLOOD_SUCK_AMOUNT = 4.0f; // 吸血量
    private static final float SONIC_BLAST_RANGE = 6.0f; // 声波爆破范围
    private static final float SONIC_BLAST_DAMAGE = 6.0f;// 声波爆破伤害

    /* ====== 主 Tick：处理持续效果和冷却 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            execute(e.player);
            return;
        }
        
        if (!(e.player instanceof ServerPlayer sp)) return;
        
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

    /**
     * 处理玩家飞行能力，参考FlyingHandler的实现
     */
    private static void execute(Entity entity) {
        if (entity == null) {
            return;
        }
        
        // 检查是否是穿着黑暗Kiva盔甲并处于蝙蝠形态的玩家
        if (entity instanceof Player player) {
            boolean hasDarkKivaFlight = false;
            
            if (player instanceof ServerPlayer serverPlayer) {
                KRBVariables.PlayerVariables variables = serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                hasDarkKivaFlight = variables.dark_kiva_bat_mode && DarkKivaItem.isFullArmorEquipped(serverPlayer);
            }
            
            // 检查玩家是否被控制
            boolean isControlled = false;
            if (player instanceof ServerPlayer serverPlayer) {
                isControlled = isPlayerControlled(serverPlayer);
            }
            
            if (hasDarkKivaFlight && !isControlled) {
                // 给予飞行能力（仅当玩家没有被控制时）
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            } else {
                // 检查游戏模式，决定是否保留飞行能力
                boolean checkGamemode = false;
                
                if (player instanceof ServerPlayer serverPlayer) {
                    checkGamemode = serverPlayer.gameMode.getGameModeForPlayer() != GameType.CREATIVE && 
                                  serverPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR;
                }
                
                // 如果玩家被控制，强制取消飞行能力（除非是创造/旁观模式）
                if (isControlled && checkGamemode) {
                    player.getAbilities().mayfly = false;
                    // 如果玩家正在飞行中，立即停止飞行
                    if (player.getAbilities().flying) {
                        player.getAbilities().flying = false;
                    }
                } else {
                    player.getAbilities().mayfly = checkGamemode ? false : true;
                }
                player.onUpdateAbilities();
            }
        }
    }

    /* ====== 能力触发方法（可被网络包调用） ====== */

    /**
     * 蝙蝠形态：用于网络包调用的飞行能力
     */
    public static void tryBatMode(ServerPlayer sp) {
        // 这个方法已经被toggleFlight替代，保留只是为了向后兼容
        toggleFlight(sp);
    }

    /**
     * 吸血能力：B键触发，吸取周围生物的生命
     */
    public static void tryBloodSuck(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
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
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
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
        
        if (variables.dark_kiva_fuuin_kekkai_active && currentTime >= variables.dark_kiva_fuuin_kekkai_cooldown - FUUIN_KEKKAI_COOLDOWN / 2) {
            variables.dark_kiva_fuuin_kekkai_active = false;
        }
    }
    
    /**
     * 封印结界技能：技能1键触发，发射封印结界实体束缚敌人
     */
    public static void tryFuuinKekkai(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();
        
        // 检查冷却
        if (currentTime < variables.dark_kiva_fuuin_kekkai_cooldown) {
            return;
        }
        
        // 消耗能量（如果需要）
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, 30)) {
            return; // 能量不足
        }
        
        Level level = sp.level();
        
        // 获取视线内的目标
        HitResult hitResult = sp.pick(20.0D, 0.0F, false);
        
        // 创建封印结界实体
        DarkKivaSealBarrierEntity sealBarrier = ModEntityTypes.DARK_KIVA_SEAL_BARRIER.get().create(level);
        sealBarrier.setOwner(sp);
        
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            // 击中实体
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity target = entityHit.getEntity();
            
            // 将封印结界实体放置在目标位置
            sealBarrier.setPos(target.getX(), target.getY() + 0.5D, target.getZ());
            // 直接设置为true，因为这个值对封印结界影响不大
            sealBarrier.setOnGround(true);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 击中地面方块
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHit.getBlockPos().above();
            
            // 将封印结界实体放置在方块上方
            sealBarrier.setPos(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
            sealBarrier.setOnGround(true);
        } else {
            // 没有击中任何东西，在前方创建
            Vec3 lookVector = sp.getLookAngle().normalize();
            Vec3 spawnPos = sp.position().add(lookVector.x * 5, sp.getEyeHeight() + lookVector.y * 5, lookVector.z * 5);
            sealBarrier.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        }
        
        // 添加实体到世界
        level.addFreshEntity(sealBarrier);
        
        // 设置冷却和状态
        variables.dark_kiva_fuuin_kekkai_active = true;
        variables.dark_kiva_fuuin_kekkai_cooldown = currentTime + FUUIN_KEKKAI_COOLDOWN;
        variables.syncPlayerVariables(sp);
        
        // 播放技能音效
        playSound(sp, SoundEvents.BELL_BLOCK, 1.0f, 0.8f);
        
        // 生成技能粒子效果
        spawnSealBarrierParticles(sp);
    }
    
    /**
     * 处理封印结界实体的大小调整（鼠标滚轮）
     */
    public static void adjustSealBarrierSize(ServerPlayer sp, double delta) {
        Level level = sp.level();
        
        // 获取视线内的封印结界实体
        HitResult hitResult = sp.pick(10.0D, 0.0F, false);
        
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity entity = entityHit.getEntity();
            
            if (entity instanceof DarkKivaSealBarrierEntity sealBarrier) {
                // 检查是否是当前玩家创建的实体
                LivingEntity owner = sealBarrier.getOwner();
                if (owner != null && owner.getUUID().equals(sp.getUUID())) {
                    // 调整大小
                    sealBarrier.adjustSize(delta * 0.2D);
                    
                    // 播放调整音效
                    playSound(sp, SoundEvents.ITEM_PICKUP, 0.5f, 1.0f + (float) Math.abs(delta) * 0.5f);
                }
            }
        }
    }
    
    /**
     * 处理封印结界实体的左键攻击（拉扯）
     */
    public static void interactWithSealBarrier(ServerPlayer sp, boolean isLeftClick) {
        Level level = sp.level();
        
        // 查找所有由当前玩家创建的封印结界实体
        List<DarkKivaSealBarrierEntity> barriers = level.getEntitiesOfClass(DarkKivaSealBarrierEntity.class, 
                new AABB(sp.blockPosition()).inflate(50), // 搜索玩家周围50格范围内
                entity -> {
                    LivingEntity owner = entity.getOwner();
                    return owner != null && owner.getUUID().equals(sp.getUUID());
                });
        
        // 对所有找到的结界实体进行操作
        for (DarkKivaSealBarrierEntity sealBarrier : barriers) {
            if (isLeftClick) {
                sealBarrier.handleLeftClick(); // 拉向玩家
            } else {
                sealBarrier.handleRightClick(); // 推开
            }
        }
    }
    
    /**
     * 优化的飞行能力：自由起飞
     */
    public static void toggleFlight(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        if (variables.dark_kiva_bat_mode) {
            // 取消飞行模式
            variables.dark_kiva_bat_mode = false;
            sp.removeEffect(MobEffects.MOVEMENT_SPEED);
            playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
        } else {
            // 消耗骑士能量
            if (!RiderEnergyHandler.consumeRiderEnergy(sp, 20)) {
                return; // 能量不足
            }
            
            // 激活飞行模式
            variables.dark_kiva_bat_mode = true;
            variables.dark_kiva_bat_mode_time = sp.level().getGameTime();
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 9999, 1, false, false));
            playSound(sp, SoundEvents.BAT_AMBIENT, 1.0f, 0.8f);
            spawnBatParticles(sp);
        }
        
        variables.syncPlayerVariables(sp);
    }
    
    private static void spawnSealBarrierParticles(ServerPlayer sp) {
        // 封印结界技能粒子效果
        Level level = sp.level();
        for (int i = 0; i < 15; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            double dy = sp.getRandom().nextDouble() * 1.5;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, 
                    sp.getX() + dx, sp.getY() + 1.0 + dy, sp.getZ() + dz, 0.0, 0.1, 0.0);
        }
    }

    /**
     * 检查玩家是否被控（有控制类负面效果）
     */
    private static boolean isPlayerControlled(ServerPlayer player) {
        // 玩家如果在飞行状态下，应能被控制，所以不返回true
        if (player.isFallFlying() || player.getAbilities().flying) {
            return false;
        }
        
        // 检查玩家是否有以下控制类负面效果
        return player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || // 缓慢
               player.hasEffect(MobEffects.BLINDNESS) || // 失明
               player.hasEffect(MobEffects.CONFUSION) || // 反胃
               player.hasEffect(MobEffects.POISON) || // 中毒
               player.hasEffect(MobEffects.WITHER) || // 凋零
               player.hasEffect(MobEffects.WEAKNESS); // 虚弱
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
            
            // 已删除力量效果，避免干扰原版力量药水
            
            // 火焰抗性：只有当玩家没有火焰抗性效果或效果等级低于我们提供的等级时，才添加新效果
            // 这样可以保留玩家通过原版药水获得的更高等级的火焰抗性效果
            MobEffectInstance fireResistance = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 320, 0, false, false);
            if (!sp.hasEffect(MobEffects.FIRE_RESISTANCE) || sp.getEffect(MobEffects.FIRE_RESISTANCE).getAmplifier() < 0) {
                sp.addEffect(fireResistance);
            }
            
            // 缓慢生命恢复：只有当玩家没有生命恢复效果或效果等级低于我们提供的等级时，才添加新效果
            MobEffectInstance regeneration = new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false);
            if (!sp.hasEffect(MobEffects.REGENERATION) || sp.getEffect(MobEffects.REGENERATION).getAmplifier() < 0) {
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
            
            // 自由飞行模式：持续刷新速度效果
            if (variables.dark_kiva_bat_mode) {
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1, false, false));
            }
        } else {
            // 当玩家脱下盔甲时，保留原版药水的效果
            // 我们不再直接移除效果，而是让它们自然到期
            
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