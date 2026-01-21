package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkKivaSealBarrierEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DarkKivaAbilityHandler {

    // 修改飞行时间：50秒 = 1000 ticks (20 ticks/秒)
    private static final int BAT_MODE_DURATION = 1000;
    private static final int BLOOD_SUCK_COOLDOWN = 150;
    private static final int SONIC_BLAST_COOLDOWN = 250;
    private static final int BLOOD_STEAL_INTERVAL = 150;
    private static final float BLOOD_STEAL_AMOUNT = 2.0f;
    private static final int FUUIN_KEKKAI_COOLDOWN = 600;

    private static final float BLOOD_SUCK_RANGE = 4.0f;
    private static final float BLOOD_SUCK_AMOUNT = 4.0f;
    private static final float SONIC_BLAST_RANGE = 6.0f;
    private static final float SONIC_BLAST_DAMAGE = 6.0f;
    private static final double MAX_BAT_MODE_HEIGHT_ABOVE_TAKEOFF = 14.0; // 蝙蝠形态最大飞行高度（相对于起飞高度）
    private static final int BAT_MODE_COOLDOWN = 600; // 蝙蝠形态冷却时间（30秒）

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!(e.player instanceof ServerPlayer sp)) return;

        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        boolean isDarkKiva = DarkKivaItem.isFullArmorEquipped(sp);

        // 检查玩家是否被控制
        if (isPlayerSealBarrierControlled(sp)) {
            // 如果玩家被控制，强制取消飞行能力
            sp.getAbilities().mayfly = false;
            sp.getAbilities().flying = false;
            sp.onUpdateAbilities();
        } else {
            // 处理正常能力
            handleBatMode(sp, variables, isDarkKiva);
            handleCooldowns(variables, sp.level().getGameTime());
            updateBaseEffects(sp, variables, isDarkKiva);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 玩家死亡时强制释放所有控制
            forceReleasePlayerFromSealBarriers(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 玩家重生时确保状态正常
            player.setNoGravity(false);
            player.hurtMarked = false;
            player.getPersistentData().remove("SealBarrierControlled");
            player.getPersistentData().remove("SealBarrierOwner");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 玩家退出时释放所有控制
            forceReleasePlayerFromSealBarriers(player);
        }
    }

    public static void toggleFlight(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerSealBarrierControlled(sp)) return;

        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();

        // 检查是否已经有其他飞行模式在控制
        if (variables.currentFlightController != null && !variables.currentFlightController.equals("DarkKiva") && variables.dark_kiva_bat_mode) {
            // 如果有其他飞行模式在控制且当前是DarkKiva模式，退出DarkKiva模式
            variables.dark_kiva_bat_mode = false;
            sp.removeEffect(MobEffects.MOVEMENT_SPEED);
            variables.currentFlightController = null;
            
            // 设置冷却时间
            variables.dark_kiva_bat_mode_cooldown = currentTime + BAT_MODE_COOLDOWN;
            
            // 恢复飞行能力状态（如果不是创造模式或旁观者模式，取消飞行能力）
            if (!sp.isCreative() && !sp.isSpectator()) {
                sp.getAbilities().mayfly = false;
                sp.getAbilities().flying = false;
            }
            sp.onUpdateAbilities();
            
            playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
        } else if (variables.dark_kiva_bat_mode) {
            // 关闭飞行模式
            variables.dark_kiva_bat_mode = false;
            sp.removeEffect(MobEffects.MOVEMENT_SPEED);
            variables.currentFlightController = null;
            
            // 设置冷却时间
            variables.dark_kiva_bat_mode_cooldown = currentTime + BAT_MODE_COOLDOWN;
            
            // 恢复飞行能力状态（如果不是创造模式或旁观者模式，取消飞行能力）
            if (!sp.isCreative() && !sp.isSpectator()) {
                sp.getAbilities().mayfly = false;
                sp.getAbilities().flying = false;
            }
            sp.onUpdateAbilities();
            
            playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
        } else {
            // 检查冷却时间
            if (currentTime < variables.dark_kiva_bat_mode_cooldown) {
                int remainingTicks = (int) (variables.dark_kiva_bat_mode_cooldown - currentTime);
                sp.displayClientMessage(net.minecraft.network.chat.Component.literal("蝙蝠形态冷却中，还剩 " + remainingTicks / 20 + " 秒"), true);
                return;
            }
            
            if (!RiderEnergyHandler.consumeRiderEnergy(sp, 20)) return;

            // 开启飞行模式
            variables.dark_kiva_bat_mode = true;
            variables.dark_kiva_bat_mode_time = currentTime;
            variables.dark_kiva_bat_mode_takeoff_height = sp.getY(); // 记录起飞高度
            variables.currentFlightController = "DarkKiva";
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 9999, 1, false, false));
            
            // 设置飞行能力
            sp.getAbilities().mayfly = true;
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            
            playSound(sp, SoundEvents.BAT_AMBIENT, 1.0f, 0.8f);
            spawnBatParticles(sp);
        }

        variables.syncPlayerVariables(sp);
    }
    
    private static void handleBatMode(ServerPlayer sp, KRBVariables.PlayerVariables variables, boolean isDarkKiva) {
        if (!isDarkKiva) {
            if (variables.dark_kiva_bat_mode) {
                variables.dark_kiva_bat_mode = false;
                sp.removeEffect(MobEffects.MOVEMENT_SPEED);
                
                // 恢复飞行能力状态
                if (!sp.isCreative() && !sp.isSpectator()) {
                    sp.getAbilities().mayfly = false;
                    sp.getAbilities().flying = false;
                }
                sp.onUpdateAbilities();
                
                variables.syncPlayerVariables(sp);
            }
            return;
        }

        if (variables.dark_kiva_bat_mode) {
            long currentTime = sp.level().getGameTime();
            
            // 检查飞行高度限制
            double currentHeight = sp.getY();
            double takeoffHeight = variables.dark_kiva_bat_mode_takeoff_height;
            if (currentHeight - takeoffHeight > MAX_BAT_MODE_HEIGHT_ABOVE_TAKEOFF) {
                // 超过高度限制，解除蝙蝠形态
                variables.dark_kiva_bat_mode = false;
                sp.removeEffect(MobEffects.MOVEMENT_SPEED);
                
                // 设置冷却时间
                variables.dark_kiva_bat_mode_cooldown = currentTime + BAT_MODE_COOLDOWN;
                
                // 恢复飞行能力状态
                if (!sp.isCreative() && !sp.isSpectator()) {
                    sp.getAbilities().mayfly = false;
                    sp.getAbilities().flying = false;
                }
                sp.onUpdateAbilities();
                
                sp.displayClientMessage(net.minecraft.network.chat.Component.literal("飞行高度超过限制，蝙蝠形态已解除"), true);
                variables.syncPlayerVariables(sp);
                playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
                return;
            }
            
            // 检查持续时间
            if (currentTime - variables.dark_kiva_bat_mode_time > BAT_MODE_DURATION) {
                variables.dark_kiva_bat_mode = false;
                sp.removeEffect(MobEffects.MOVEMENT_SPEED);
                
                // 设置冷却时间
                variables.dark_kiva_bat_mode_cooldown = currentTime + BAT_MODE_COOLDOWN;
                
                // 恢复飞行能力状态
                if (!sp.isCreative() && !sp.isSpectator()) {
                    sp.getAbilities().mayfly = false;
                    sp.getAbilities().flying = false;
                }
                sp.onUpdateAbilities();
                
                variables.syncPlayerVariables(sp);
                playSound(sp, SoundEvents.BAT_TAKEOFF, 1.0f, 1.2f);
            } else {
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1, false, false));
                
                // 确保飞行能力保持开启
                if (!sp.getAbilities().mayfly) {
                    sp.getAbilities().mayfly = true;
                    sp.getAbilities().flying = true;
                    sp.onUpdateAbilities();
                }
            }
        }
    }

    public static void tryBloodSuck(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerSealBarrierControlled(sp)) return;

        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();

        if (currentTime < variables.dark_kiva_blood_suck_cooldown) {
            int remainingTicks = (int) (variables.dark_kiva_blood_suck_cooldown - currentTime);
            sp.displayClientMessage(net.minecraft.network.chat.Component.literal("吸血技能冷却中，还剩 " + remainingTicks / 20 + " 秒"), true);
            return;
        }

        Level level = sp.level();
        AABB box = new AABB(sp.blockPosition()).inflate(BLOOD_SUCK_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != sp && !e.isAlliedTo(sp) && e.isAlive());

        if (!targets.isEmpty()) {
            float totalHeal = 0.0f;
            for (LivingEntity target : targets) {
                float damage = Math.min(BLOOD_SUCK_AMOUNT, target.getHealth());
                target.hurt(sp.damageSources().playerAttack(sp), damage);
                totalHeal += damage * 0.8f;
            }

            sp.heal(totalHeal);
            variables.dark_kiva_blood_suck_cooldown = currentTime + BLOOD_SUCK_COOLDOWN;
            playSound(sp, SoundEvents.PLAYER_HURT_ON_FIRE, 0.8f, 0.9f);
            spawnBloodParticles(sp);
        }

        variables.syncPlayerVariables(sp);
    }

    public static void trySonicBlast(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerSealBarrierControlled(sp)) return;

        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();

        if (currentTime < variables.dark_kiva_sonic_blast_cooldown) {
            int remainingTicks = (int) (variables.dark_kiva_sonic_blast_cooldown - currentTime);
            sp.displayClientMessage(net.minecraft.network.chat.Component.literal("声波爆破冷却中，还剩 " + remainingTicks / 20 + " 秒"), true);
            return;
        }

        Level level = sp.level();
        AABB box = new AABB(sp.blockPosition()).inflate(SONIC_BLAST_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != sp && !e.isAlliedTo(sp));

        for (LivingEntity target : targets) {
            target.hurt(sp.damageSources().playerAttack(sp), SONIC_BLAST_DAMAGE);
            Vec3 pushVec = target.position().subtract(sp.position()).normalize().scale(0.5);
            target.push(pushVec.x, 0.2, pushVec.z);
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
        }

        variables.dark_kiva_sonic_blast_cooldown = currentTime + SONIC_BLAST_COOLDOWN;
        playSound(sp, SoundEvents.WITHER_BREAK_BLOCK, 1.0f, 1.5f);
        spawnSoundParticles(sp, SONIC_BLAST_RANGE);

        variables.syncPlayerVariables(sp);
    }

    public static void tryFuuinKekkai(ServerPlayer sp) {
        if (!DarkKivaItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerSealBarrierControlled(sp)) return;

        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();

        if (currentTime < variables.dark_kiva_fuuin_kekkai_cooldown) {
            int remainingTicks = (int) (variables.dark_kiva_fuuin_kekkai_cooldown - currentTime);
            sp.displayClientMessage(net.minecraft.network.chat.Component.literal("封印结界冷却中，还剩 " + remainingTicks / 20 + " 秒"), true);
            return;
        }
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, 30)) return;

        Level level = sp.level();
        HitResult hitResult = sp.pick(20.0D, 0.0F, false);

        DarkKivaSealBarrierEntity sealBarrier = ModEntityTypes.DARK_KIVA_SEAL_BARRIER.get().create(level);
        if (sealBarrier == null) return;

        sealBarrier.setOwner(sp);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            sealBarrier.setPos(entityHit.getEntity().getX(), entityHit.getEntity().getY() + 0.5, entityHit.getEntity().getZ());
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHit.getBlockPos().above();
            sealBarrier.setPos(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        } else {
            Vec3 lookVector = sp.getLookAngle().normalize();
            Vec3 spawnPos = sp.position().add(lookVector.x * 5, sp.getEyeHeight() + lookVector.y * 5, lookVector.z * 5);
            sealBarrier.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        }

        level.addFreshEntity(sealBarrier);
        variables.dark_kiva_fuuin_kekkai_cooldown = currentTime + FUUIN_KEKKAI_COOLDOWN;
        variables.syncPlayerVariables(sp);

        playSound(sp, SoundEvents.BELL_BLOCK, 1.0f, 0.8f);
        spawnSealBarrierParticles(sp);
    }

    public static void adjustSealBarrierSize(ServerPlayer sp, double delta) {
        Level level = sp.level();
        HitResult hitResult = sp.pick(10.0D, 0.0F, false);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity entity = entityHit.getEntity();

            if (entity instanceof DarkKivaSealBarrierEntity sealBarrier) {
                LivingEntity owner = sealBarrier.getOwner();
                if (owner != null && owner.getUUID().equals(sp.getUUID())) {
                    sealBarrier.adjustSize(delta * 0.2D);
                    playSound(sp, SoundEvents.ITEM_PICKUP, 0.5f, 1.0f + (float) Math.abs(delta) * 0.5f);
                }
            }
        }
    }

    public static void interactWithSealBarrier(ServerPlayer sp, boolean isLeftClick) {
        Level level = sp.level();
        List<DarkKivaSealBarrierEntity> barriers = level.getEntitiesOfClass(DarkKivaSealBarrierEntity.class,
                new AABB(sp.blockPosition()).inflate(50),
                entity -> {
                    LivingEntity owner = entity.getOwner();
                    return owner != null && owner.getUUID().equals(sp.getUUID());
                });

        for (DarkKivaSealBarrierEntity sealBarrier : barriers) {
            if (isLeftClick) {
                sealBarrier.handleLeftClick();
            } else {
                sealBarrier.handleRightClick();
            }
        }
    }

    /**
     * 检查玩家是否被封印结界控制
     */
    public static boolean isPlayerSealBarrierControlled(Player player) {
        return player.getPersistentData().getBoolean("SealBarrierControlled");
    }

    /**
     * 强制释放玩家从所有封印结界中
     */
    public static void forceReleasePlayerFromSealBarriers(ServerPlayer player) {
        Level level = player.level();
        List<DarkKivaSealBarrierEntity> barriers = level.getEntitiesOfClass(DarkKivaSealBarrierEntity.class,
                new AABB(player.blockPosition()).inflate(100));

        for (DarkKivaSealBarrierEntity barrier : barriers) {
            if (barrier.isEntityTrapped(player)) {
                barrier.forceReleaseEntity(player);
            }
        }

        player.setNoGravity(false);
        player.hurtMarked = false;
        player.getPersistentData().remove("SealBarrierControlled");
        player.getPersistentData().remove("SealBarrierOwner");
    }



    private static void handleCooldowns(KRBVariables.PlayerVariables variables, long currentTime) {
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

    private static void updateBaseEffects(ServerPlayer sp, KRBVariables.PlayerVariables variables, boolean isDarkKiva) {
        long currentTime = sp.level().getGameTime();

        if (isDarkKiva) {
            // 夜视效果
            if (!sp.hasEffect(MobEffects.NIGHT_VISION) ||
                    (sp.getEffect(MobEffects.NIGHT_VISION).getDuration() < 260 &&
                            sp.getEffect(MobEffects.NIGHT_VISION).getDuration() != Integer.MAX_VALUE)) {
                sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
            }

            // 火焰抗性
            if (!sp.hasEffect(MobEffects.FIRE_RESISTANCE) || sp.getEffect(MobEffects.FIRE_RESISTANCE).getAmplifier() < 0) {
                sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 320, 0, false, false));
            }

            // 生命恢复
            if (!sp.hasEffect(MobEffects.REGENERATION) || sp.getEffect(MobEffects.REGENERATION).getAmplifier() < 0) {
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
            }

            // 自动吸血
            if (currentTime >= variables.dark_kiva_blood_steal_cooldown && sp.isAlive()) {
                long lastHurt = sp.getLastHurtByMobTimestamp();
                if (currentTime - lastHurt > 100) {
                    sp.heal(BLOOD_STEAL_AMOUNT);
                    variables.dark_kiva_blood_steal_cooldown = currentTime + BLOOD_STEAL_INTERVAL;
                    spawnBatParticles(sp);
                    variables.syncPlayerVariables(sp);
                }
            }
        } else {
            variables.dark_kiva_blood_steal_cooldown = 0;
            variables.syncPlayerVariables(sp);
        }
    }

    private static void playSound(ServerPlayer sp, SoundEvent sound, float volume, float pitch) {
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void spawnBatParticles(ServerPlayer sp) {
        Level level = sp.level();
        for (int i = 0; i < 8; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            double dy = sp.getRandom().nextDouble() * 1.5;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMOKE,
                    sp.getX() + dx, sp.getY() + 0.5 + dy, sp.getZ() + dz, 0.0, 0.05, 0.0);
        }
    }

    private static void spawnBloodParticles(ServerPlayer sp) {
        Level level = sp.level();
        for (int i = 0; i < 12; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 1.5;
            double dy = sp.getRandom().nextDouble() * 1.0;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 1.5;
            level.addParticle(ParticleTypes.ENTITY_EFFECT,
                    sp.getX() + dx, sp.getY() + 0.5 + dy, sp.getZ() + dz, 0.8, 0.0, 0.0);
        }
    }

    private static void spawnSoundParticles(ServerPlayer sp, float range) {
        Level level = sp.level();
        for (int i = 0; i < 20; i++) {
            double angle = sp.getRandom().nextDouble() * Math.PI * 2;
            double radius = sp.getRandom().nextDouble() * range;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            level.addParticle(ParticleTypes.NOTE,
                    sp.getX() + dx, sp.getY() + 0.5, sp.getZ() + dz,
                    sp.getRandom().nextDouble(), sp.getRandom().nextDouble(), sp.getRandom().nextDouble());
        }
    }

    private static void spawnSealBarrierParticles(ServerPlayer sp) {
        Level level = sp.level();
        for (int i = 0; i < 15; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            double dy = sp.getRandom().nextDouble() * 1.5;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.ENCHANT,
                    sp.getX() + dx, sp.getY() + 1.0 + dy, sp.getZ() + dz, 0.0, 0.1, 0.0);
        }
    }

    private DarkKivaAbilityHandler() {}

    /**
     * 蝙蝠形态：用于网络包调用的飞行能力
     */
    public static void tryBatMode(ServerPlayer sp) {
        // 这个方法已经被toggleFlight替代，保留只是为了向后兼容
        toggleFlight(sp);
    }
}