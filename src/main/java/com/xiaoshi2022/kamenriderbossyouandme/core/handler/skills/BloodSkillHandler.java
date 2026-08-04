package com.xiaoshi2022.kamenriderbossyouandme.core.handler.skills;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.server.event.SkillEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.PlayerMovementPacket;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.riders.build.BloodConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class BloodSkillHandler {

    private static final Map<ResourceLocation, Consumer<Player>> SKILL_MAP = new HashMap<>();

    // Blood数据
    private static final float BLOOD_WAVE_DAMAGE = 20.0f;
    private static final float BLOOD_WAVE_RANGE = 20.0f;
    private static final float BLOOD_WAVE_SPEED = 1.5f;  // 减慢速度，让球体更明显

    private static final int BARRIER_DURATION = 100; // 5秒
    private static final float BARRIER_ABSORPTION = 30.0f;

    private static final float GRAVITY_RADIUS = 6.0f;
    private static final float GRAVITY_DAMAGE = 25.0f;
    private static final float GRAVITY_PULL_STRENGTH = 0.4f;

    // 屏障状态追踪
    private static final Map<UUID, Integer> BARRIER_ACTIVE = new HashMap<>();
    private static final Map<UUID, Float> BARRIER_ABSORBED = new HashMap<>();

    // 能量波追踪
    private static final Map<UUID, BloodWaveData> ACTIVE_WAVES = new HashMap<>();

    static {
        SKILL_MAP.put(RiderSkills.BLOOD_WAVE, BloodSkillHandler::executeBloodWave);
        SKILL_MAP.put(RiderSkills.BLOOD_BARRIER, BloodSkillHandler::executeBloodBarrier);
        SKILL_MAP.put(RiderSkills.BLOOD_GRAVITY_COLLAPSE, BloodSkillHandler::executeGravityCollapse);
        // ✅ 添加通用骑士踢
        SKILL_MAP.put(RiderSkills.RIDER_KICK, BloodSkillHandler::executeBloodRiderKick);
    }

    // 添加骑士踢实现
// ==================== 通用骑士踢（Blood强化版） ====================

    private static void executeBloodRiderKick(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        int duration = calculateTolerance(60); // 3秒

        // 踢击序列
        kickSequence(player, duration);

        // 添加技能标签
        addTag(player, "rider_kicking");
        addTag(player, "skill_blood_kick");

        // 添加抗性
        addResistance(player, duration);

        // Blood骑士踢 - 更强的跳跃和速度（血族强化）
        float BLOOD_KICK_JUMP = 3.0f;   // 比普通骑士踢更高
        float BLOOD_KICK_SPEED = 2.5f;  // 比普通骑士踢更快

        riderKickJump(player, BLOOD_KICK_JUMP);
        riderKickForward(player, BLOOD_KICK_SPEED, 10);

        // 播放音效和粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 血红色粒子
            DustParticleOptions bloodParticle = new DustParticleOptions(
                    new Vector3f(0.8f, 0.0f, 0.0f), 2.0f
            );
            for (int i = 0; i < 30; i++) {
                double radius = 0.5 + player.getRandom().nextDouble() * 0.8;
                double theta = player.getRandom().nextDouble() * Math.PI * 2;
                serverLevel.sendParticles(bloodParticle,
                        player.getX() + Math.cos(theta) * radius,
                        player.getY() + 0.5 + player.getRandom().nextDouble() * 1.0,
                        player.getZ() + Math.sin(theta) * radius,
                        0, 0, 0, 0, 0);
            }
        }

        // 播放动画
        playAnimation(player, "kick", 2);

        // 定时移除标签
        RideBattleAPI.scheduleTicks(duration, () -> {
            removeTag(player, "rider_kicking");
            removeTag(player, "skill_blood_kick");
        });
    }

    // 添加骑士踢碰撞检测
    private static void handleBloodKickCollide(Player player) {
        if (!player.getTags().contains("rider_kicking")) return;
        if (!player.getTags().contains("skill_blood_kick")) return;

        Level level = player.level();

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();
        double forwardDistance = 0.8;

        AABB kickBox = player.getBoundingBox()
                .expandTowards(horizontalLook.scale(forwardDistance))
                .inflate(0.3);

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                kickBox,
                e -> e != player && e.isAlive()
        );

        if (entities.isEmpty()) return;

        // Blood骑士踢伤害（比普通骑士踢更强）
        float BLOOD_KICK_DAMAGE = 35.0f;

        for (LivingEntity entity : entities) {
            createBloodKickExplosion(player, entity, BLOOD_KICK_DAMAGE);
            removeTag(player, "rider_kicking");
            removeTag(player, "skill_blood_kick");
            break;
        }
    }

    // Blood骑士踢爆炸效果
    private static void createBloodKickExplosion(Player player, LivingEntity entity, float damage) {
        BlockPos pos = entity.getOnPos();
        createExplosion(player, pos.getX(), pos.getY() + 1.5, pos.getZ(), damage);

        hurt(player, entity, damage);

        Vec3 angle = player.getLookAngle();
        Vec3 back = new Vec3(-(angle.x * 2), 0.5, -(angle.z * 2));
        setDeltaMovement(player, 0, 0, 0);
        addDeltaMovement(player, back);

        // 血红色爆炸粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            DustParticleOptions explosionParticle = new DustParticleOptions(
                    new Vector3f(0.9f, 0.0f, 0.0f), 3.0f
            );
            for (int i = 0; i < 40; i++) {
                double radius = 0.5 + player.getRandom().nextDouble() * 2.0;
                double theta = player.getRandom().nextDouble() * Math.PI * 2;
                double phi = player.getRandom().nextDouble() * Math.PI;
                serverLevel.sendParticles(explosionParticle,
                        entity.getX() + Math.sin(phi) * Math.cos(theta) * radius,
                        entity.getY() + 1.0 + Math.cos(phi) * radius * 0.5,
                        entity.getZ() + Math.sin(phi) * Math.sin(theta) * radius,
                        0, 0, 0, 0, 0);
            }
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.7F);
        }
    }

    @SubscribeEvent
    public static void onSkill(SkillEvent.Post event) {
        Player player = event.getPlayer();
        ResourceLocation skillId = event.getSkillId();
        handleSkill(player, skillId);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        // 更新能量波
        updateWave(player);

        // 更新屏障
        updateBarrier(player);

        // ✅ 添加Blood骑士踢碰撞检测
        handleBloodKickCollide(player);
    }

    private static void handleSkill(Player player, ResourceLocation skillId) {
        String tag = RiderSkills.SKILL_TAGS.get(skillId);
        if (tag != null) {
            addTag(player, tag);
        }

        Consumer<Player> skillConsumer = SKILL_MAP.get(skillId);
        if (skillConsumer != null) {
            skillConsumer.accept(player);
            player.hurtMarked = true;

            KamenRiderBossYOUandME.LOGGER.debug("Blood玩家 {} 使用了技能: {}",
                    player.getName().getString(), skillId);
        }
    }

    // ==================== 技能1：球状能量波 ====================

    private static void executeBloodWave(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 播放音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);

        // 播放动画
        playAnimation(player, "blood_wave", 2);

        // 向前发射能量球
        Vec3 lookVec = player.getLookAngle();
        Vec3 startPos = player.getEyePosition().add(lookVec.scale(1.5));

        // 粒子效果 - 血红色能量球聚集
        ServerLevel serverLevel = (ServerLevel) player.level();
        DustParticleOptions bloodParticle = new DustParticleOptions(
                new Vector3f(0.8f, 0.0f, 0.0f), 1.5f
        );

        // 创建球状粒子效果
        for (int i = 0; i < 50; i++) {
            double theta = player.getRandom().nextDouble() * Math.PI * 2;
            double phi = player.getRandom().nextDouble() * Math.PI;
            double radius = 0.8 + player.getRandom().nextDouble() * 0.5;
            serverLevel.sendParticles(bloodParticle,
                    startPos.x + Math.sin(phi) * Math.cos(theta) * radius,
                    startPos.y + Math.cos(phi) * radius + 0.5,
                    startPos.z + Math.sin(phi) * Math.sin(theta) * radius,
                    0, 0, 0, 0, 0);
        }

        // 创建能量波数据（球状）
        BloodWaveData waveData = new BloodWaveData(
                startPos,
                lookVec,
                player.getUUID(),
                BLOOD_WAVE_RANGE,
                BLOOD_WAVE_SPEED
        );
        waveData.radius = 0.8f;  // 初始球体半径
        ACTIVE_WAVES.put(player.getUUID(), waveData);
    }

    private static void updateWave(Player player) {
        BloodWaveData waveData = ACTIVE_WAVES.get(player.getUUID());
        if (waveData == null || !waveData.active) return;

        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 移动能量波
        Vec3 oldPos = waveData.position;
        waveData.position = waveData.position.add(waveData.direction.scale(waveData.speed));
        waveData.traveled += waveData.speed;

        // 球体逐渐变大再变小（呼吸效果）
        float lifeProgress = waveData.traveled / waveData.maxRange;
        waveData.radius = 0.6f + (float)Math.sin(lifeProgress * Math.PI) * 0.6f;

        // 超出范围
        if (waveData.traveled > waveData.maxRange) {
            waveData.active = false;
            ACTIVE_WAVES.remove(player.getUUID());
            // 最终爆炸
            createWaveExplosion(serverLevel, waveData.position, player);
            return;
        }

        // 渲染球状粒子
        Vec3 pos = waveData.position;
        float radius = waveData.radius;

        // 外层：血红色粒子外壳
        DustParticleOptions outerParticle = new DustParticleOptions(
                new Vector3f(0.9f, 0.1f, 0.1f), 1.2f
        );

        // 内层：亮白色核心
        DustParticleOptions coreParticle = new DustParticleOptions(
                new Vector3f(1.0f, 0.5f, 0.5f), 0.8f
        );

        // 绘制球体表面的粒子
        for (int i = 0; i < 20; i++) {
            double theta = player.getRandom().nextDouble() * Math.PI * 2;
            double phi = player.getRandom().nextDouble() * Math.PI;
            double r = radius * (0.85 + player.getRandom().nextDouble() * 0.15);
            serverLevel.sendParticles(outerParticle,
                    pos.x + Math.sin(phi) * Math.cos(theta) * r,
                    pos.y + Math.cos(phi) * r + 0.5,
                    pos.z + Math.sin(phi) * Math.sin(theta) * r,
                    0, 0, 0, 0, 0);
        }

        // 核心粒子
        for (int i = 0; i < 8; i++) {
            double theta = player.getRandom().nextDouble() * Math.PI * 2;
            double phi = player.getRandom().nextDouble() * Math.PI;
            double r = radius * 0.4 * player.getRandom().nextDouble();
            serverLevel.sendParticles(coreParticle,
                    pos.x + Math.sin(phi) * Math.cos(theta) * r,
                    pos.y + Math.cos(phi) * r + 0.5,
                    pos.z + Math.sin(phi) * Math.sin(theta) * r,
                    0, 0, 0, 0, 0);
        }

        // 拖尾粒子
        Vec3 trailDir = waveData.direction.scale(-1);
        for (int i = 0; i < 5; i++) {
            double t = player.getRandom().nextDouble() * 0.5;
            Vec3 trailPos = pos.add(trailDir.scale(t * 1.5));
            serverLevel.sendParticles(DustParticleOptions.REDSTONE,
                    trailPos.x + (player.getRandom().nextDouble() - 0.5) * radius * 0.8,
                    trailPos.y + (player.getRandom().nextDouble() - 0.5) * radius * 0.8 + 0.5,
                    trailPos.z + (player.getRandom().nextDouble() - 0.5) * radius * 0.8,
                    1, 0, 0, 0, 0);
        }

        // 检测碰撞（球体范围）
        AABB hitBox = new AABB(
                pos.x - radius, pos.y - radius + 0.5, pos.z - radius,
                pos.x + radius, pos.y + radius + 0.5, pos.z + radius
        );

        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(
                LivingEntity.class, hitBox,
                e -> e != player && e.isAlive()
        );

        if (!entities.isEmpty()) {
            waveData.active = false;
            ACTIVE_WAVES.remove(player.getUUID());

            for (LivingEntity target : entities) {
                target.hurt(target.damageSources().playerAttack(player), BLOOD_WAVE_DAMAGE);
                target.knockback(1.5f, -waveData.direction.x, -waveData.direction.z);
            }

            createWaveExplosion(serverLevel, pos, player);
        }
    }

    private static void createWaveExplosion(ServerLevel level, Vec3 pos, Player player) {
        // 球状爆炸粒子
        DustParticleOptions explosionParticle = new DustParticleOptions(
                new Vector3f(0.9f, 0.0f, 0.0f), 2.0f
        );
        DustParticleOptions coreExplosion = new DustParticleOptions(
                new Vector3f(1.0f, 0.3f, 0.3f), 1.5f
        );

        // 外层爆炸
        for (int i = 0; i < 60; i++) {
            double radius = 1.0 + player.getRandom().nextDouble() * 3.0;
            double theta = player.getRandom().nextDouble() * Math.PI * 2;
            double phi = player.getRandom().nextDouble() * Math.PI;
            level.sendParticles(explosionParticle,
                    pos.x + Math.sin(phi) * Math.cos(theta) * radius,
                    pos.y + Math.cos(phi) * radius * 0.5 + 0.5,
                    pos.z + Math.sin(phi) * Math.sin(theta) * radius,
                    0, 0, 0, 0, 0);
        }

        // 核心爆炸
        for (int i = 0; i < 30; i++) {
            double radius = player.getRandom().nextDouble() * 1.5;
            double theta = player.getRandom().nextDouble() * Math.PI * 2;
            double phi = player.getRandom().nextDouble() * Math.PI;
            level.sendParticles(coreExplosion,
                    pos.x + Math.sin(phi) * Math.cos(theta) * radius,
                    pos.y + Math.cos(phi) * radius * 0.5 + 0.5,
                    pos.z + Math.sin(phi) * Math.sin(theta) * radius,
                    0, 0, 0, 0, 0);
        }

        // 冲击波粒子（环形）
        for (int ring = 0; ring < 3; ring++) {
            double ringRadius = 1.0 + ring * 1.0;
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI * 2 / 16;
                level.sendParticles(ParticleTypes.POOF,
                        pos.x + Math.cos(angle) * ringRadius,
                        pos.y + 0.5 + Math.sin(angle * 2) * 0.3,
                        pos.z + Math.sin(angle) * ringRadius,
                        1, 0, 0, 0, 0);
            }
        }

        // 爆炸音效
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.7F);

        // 爆炸伤害（范围）
        boolean grief = Config.SKILL_EXPLODE_GRIEF.get();
        Level.ExplosionInteraction interaction = grief ?
                Level.ExplosionInteraction.BLOCK :
                Level.ExplosionInteraction.NONE;
        level.explode(player, pos.x, pos.y, pos.z, 2.5f, false, interaction);
    }

    // ==================== 技能2：次元屏障 ====================

    private static void executeBloodBarrier(Player player) {
        UUID playerId = player.getUUID();

        // 如果已有屏障，则移除
        if (BARRIER_ACTIVE.containsKey(playerId)) {
            deactivateBarrier(player);
            return;
        }

        // 激活屏障
        BARRIER_ACTIVE.put(playerId, BARRIER_DURATION);
        BARRIER_ABSORBED.put(playerId, 0.0f);

        // 播放音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);

        // 粒子效果 - 紫色屏障
        if (player.level() instanceof ServerLevel serverLevel) {
            DustParticleOptions barrierParticle = new DustParticleOptions(
                    new Vector3f(0.6f, 0.0f, 0.8f), 1.5f
            );

            for (int i = 0; i < 40; i++) {
                double radius = 1.5;
                double theta = player.getRandom().nextDouble() * Math.PI * 2;
                double phi = player.getRandom().nextDouble() * Math.PI;
                serverLevel.sendParticles(barrierParticle,
                        player.getX() + Math.sin(phi) * Math.cos(theta) * radius,
                        player.getY() + Math.cos(phi) * radius + 1.0,
                        player.getZ() + Math.sin(phi) * Math.sin(theta) * radius,
                        0, 0, 0, 0, 0);
            }
        }

        // 添加抗性效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BARRIER_DURATION, 3));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BARRIER_DURATION, 1));

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§5§l血族·次元障壁 展开！"),
                true
        );
    }

    private static void updateBarrier(Player player) {
        UUID playerId = player.getUUID();
        if (!BARRIER_ACTIVE.containsKey(playerId)) return;

        int remaining = BARRIER_ACTIVE.get(playerId) - 1;

        if (remaining <= 0) {
            deactivateBarrier(player);
            return;
        }

        BARRIER_ACTIVE.put(playerId, remaining);

        // 每10tick显示粒子
        if (remaining % 10 == 0 && player.level() instanceof ServerLevel serverLevel) {
            DustParticleOptions particle = new DustParticleOptions(
                    new Vector3f(0.5f, 0.0f, 0.7f), 0.8f
            );

            for (int i = 0; i < 10; i++) {
                double radius = 1.8;
                double theta = player.getRandom().nextDouble() * Math.PI * 2;
                serverLevel.sendParticles(particle,
                        player.getX() + Math.cos(theta) * radius,
                        player.getY() + 1.0 + player.getRandom().nextDouble() * 1.5,
                        player.getZ() + Math.sin(theta) * radius,
                        0, 0, 0, 0, 0);
            }
        }

        // 显示剩余时间
        if (remaining == 20 || remaining == 40 || remaining == 60 || remaining == 80) {
            int seconds = remaining / 20;
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§d屏障剩余 " + seconds + " 秒"),
                    true
            );
        }
    }

    private static void deactivateBarrier(Player player) {
        UUID playerId = player.getUUID();
        BARRIER_ACTIVE.remove(playerId);
        BARRIER_ABSORBED.remove(playerId);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§c次元障壁 解除"),
                true
        );

        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.removeEffect(MobEffects.REGENERATION);

        // 解除粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            DustParticleOptions particle = new DustParticleOptions(
                    new Vector3f(0.7f, 0.0f, 0.9f), 1.0f
            );
            for (int i = 0; i < 20; i++) {
                double radius = player.getRandom().nextDouble() * 2.0;
                double theta = player.getRandom().nextDouble() * Math.PI * 2;
                serverLevel.sendParticles(particle,
                        player.getX() + Math.cos(theta) * radius,
                        player.getY() + 1.0 + player.getRandom().nextDouble() * 1.5,
                        player.getZ() + Math.sin(theta) * radius,
                        0, 0, 0, 0, 0);
            }
        }
    }

    /**
     * 检查是否被屏障保护（供伤害事件调用）
     */
    public static boolean isBarrierActive(UUID playerId) {
        return BARRIER_ACTIVE.containsKey(playerId);
    }

    /**
     * 屏障吸收伤害（供伤害事件调用）
     */
    public static float absorbDamage(UUID playerId, float damage) {
        if (!BARRIER_ACTIVE.containsKey(playerId)) return damage;

        float absorbed = BARRIER_ABSORBED.getOrDefault(playerId, 0f);
        float remaining = BARRIER_ABSORPTION - absorbed;

        if (remaining <= 0) {
            deactivateBarrierForPlayer(playerId);
            return damage;
        }

        float absorbedAmount = Math.min(damage, remaining);
        BARRIER_ABSORBED.put(playerId, absorbed + absorbedAmount);

        if (absorbedAmount >= remaining) {
            deactivateBarrierForPlayer(playerId);
        }

        return damage - absorbedAmount;
    }

    private static void deactivateBarrierForPlayer(UUID playerId) {
        BARRIER_ACTIVE.remove(playerId);
        BARRIER_ABSORBED.remove(playerId);
    }

    // ==================== 技能3：重力崩坏 ====================

    private static void executeGravityCollapse(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 获取目标位置（玩家前方10格）
        Vec3 lookVec = player.getLookAngle();
        Vec3 targetPos = player.getEyePosition().add(lookVec.scale(10));

        // 播放音效
        player.level().playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 2.0F, 0.5F);

        // 播放动画
        playAnimation(player, "gravity_collapse", 3);

        // 创建重力场粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            // 黑色/紫色重力粒子
            DustParticleOptions gravityParticle = new DustParticleOptions(
                    new Vector3f(0.2f, 0.0f, 0.3f), 2.0f
            );

            DustParticleOptions coreParticle = new DustParticleOptions(
                    new Vector3f(0.8f, 0.0f, 0.8f), 3.0f
            );

            BlockPos targetBlockPos = BlockPos.containing(targetPos.x, targetPos.y, targetPos.z);

            // 中心点
            serverLevel.sendParticles(coreParticle,
                    targetPos.x, targetPos.y + 1.0, targetPos.z,
                    10, 0.3, 0.3, 0.3, 0.05);

            // 外围粒子环
            for (int ring = 0; ring < 3; ring++) {
                double radius = GRAVITY_RADIUS * (ring + 1) / 3;
                for (int i = 0; i < 20; i++) {
                    double angle = i * Math.PI * 2 / 20;
                    serverLevel.sendParticles(gravityParticle,
                            targetPos.x + Math.cos(angle) * radius,
                            targetPos.y + 1.0 + Math.sin(angle * 2) * 0.5,
                            targetPos.z + Math.sin(angle) * radius,
                            0, 0, 0, 0, 0);
                }
            }

            // 重力场效果 - 持续拉拽并造成伤害
            serverLevel.getServer().execute(() -> {
                for (int tick = 0; tick < 40; tick++) {
                    final int currentTick = tick;
                    RideBattleAPI.scheduleTicks(tick, () -> {
                        BlockPos checkPos = BlockPos.containing(targetPos.x, targetPos.y, targetPos.z);
                        if (!serverLevel.isLoaded(checkPos)) return;

                        // 拉拽实体
                        AABB area = new AABB(
                                targetPos.x - GRAVITY_RADIUS,
                                targetPos.y - GRAVITY_RADIUS,
                                targetPos.z - GRAVITY_RADIUS,
                                targetPos.x + GRAVITY_RADIUS,
                                targetPos.y + GRAVITY_RADIUS,
                                targetPos.z + GRAVITY_RADIUS
                        );

                        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(
                                LivingEntity.class, area,
                                e -> e != player && e.isAlive()
                        );

                        for (LivingEntity entity : entities) {
                            // 拉向中心 - 修复 Vec3.with 问题
                            Vec3 toCenter = targetPos.subtract(entity.position());
                            toCenter = new Vec3(toCenter.x, 0, toCenter.z).normalize();

                            double distance = entity.distanceToSqr(targetPos.x, targetPos.y, targetPos.z);
                            if (distance > 1.0) {
                                double strength = Math.min(0.5, 0.2 + 0.3 / (distance + 0.5));
                                Vec3 pull = toCenter.scale(strength);
                                entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
                                entity.hurtMarked = true;
                            }

                            // 每10tick造成伤害
                            if (currentTick % 10 == 0) {
                                entity.hurt(entity.damageSources().indirectMagic(player, player),
                                        GRAVITY_DAMAGE / 4);
                            }

                            // 粒子特效
                            if (currentTick % 5 == 0) {
                                serverLevel.sendParticles(ParticleTypes.PORTAL,
                                        entity.getX(), entity.getY() + 0.5, entity.getZ(),
                                        2, 0.1, 0.1, 0.1, 0.02);
                            }
                        }

                        // 重力场粒子（持续收缩）
                        double shrinkRadius = GRAVITY_RADIUS * (1.0 - currentTick / 40.0);
                        for (int i = 0; i < 10; i++) {
                            double angle = i * Math.PI * 2 / 10;
                            double r = shrinkRadius * (0.5 + 0.5 * (1.0 - currentTick / 40.0));
                            serverLevel.sendParticles(DustParticleOptions.REDSTONE,
                                    targetPos.x + Math.cos(angle + currentTick * 0.05) * r,
                                    targetPos.y + 1.0 + Math.sin(angle * 2 + currentTick * 0.05) * 0.5,
                                    targetPos.z + Math.sin(angle + currentTick * 0.05) * r,
                                    1, 0, 0, 0, 0);
                        }
                    });
                }

                // 最终爆炸
                RideBattleAPI.scheduleTicks(40, () -> {
                    BlockPos explodePos = BlockPos.containing(targetPos.x, targetPos.y, targetPos.z);
                    if (!serverLevel.isLoaded(explodePos)) return;

                    // 爆炸效果
                    boolean grief = Config.SKILL_EXPLODE_GRIEF.get();
                    Level.ExplosionInteraction interaction = grief ?
                            Level.ExplosionInteraction.BLOCK :
                            Level.ExplosionInteraction.NONE;
                    serverLevel.explode(player, targetPos.x, targetPos.y + 1, targetPos.z,
                            3.0f, false, interaction);

                    // 额外伤害
                    AABB finalArea = new AABB(
                            targetPos.x - 3, targetPos.y - 2, targetPos.z - 3,
                            targetPos.x + 3, targetPos.y + 4, targetPos.z + 3
                    );
                    List<LivingEntity> finalEntities = serverLevel.getEntitiesOfClass(
                            LivingEntity.class, finalArea,
                            e -> e != player && e.isAlive()
                    );
                    for (LivingEntity entity : finalEntities) {
                        entity.hurt(entity.damageSources().playerAttack(player), GRAVITY_DAMAGE * 0.5f);
                    }

                    // 大爆炸粒子
                    for (int i = 0; i < 60; i++) {
                        double radius = 1.0 + player.getRandom().nextDouble() * 3.0;
                        double theta = player.getRandom().nextDouble() * Math.PI * 2;
                        double phi = player.getRandom().nextDouble() * Math.PI;
                        serverLevel.sendParticles(DustParticleOptions.REDSTONE,
                                targetPos.x + Math.sin(phi) * Math.cos(theta) * radius,
                                targetPos.y + 1.0 + Math.cos(phi) * radius * 0.5,
                                targetPos.z + Math.sin(phi) * Math.sin(theta) * radius,
                                1, 0, 0, 0, 0);
                    }
                });
            });
        }

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§5§l血族·重力崩坏！"),
                true
        );
    }

    // ==================== 辅助类 ====================

    private static class BloodWaveData {
        Vec3 position;
        Vec3 direction;
        UUID owner;
        float maxRange;
        float speed;
        float traveled;
        float radius;  // 球体半径
        boolean active;

        BloodWaveData(Vec3 position, Vec3 direction, UUID owner, float maxRange, float speed) {
            this.position = position;
            this.direction = direction;
            this.owner = owner;
            this.maxRange = maxRange;
            this.speed = speed;
            this.traveled = 0;
            this.radius = 0.8f;
            this.active = true;
        }
    }

    // ==================== 辅助方法 ====================

    private static void addTag(Player player, String tag) {
        if (!player.getTags().contains(tag)) {
            player.addTag(tag);
        }
    }

    private static void removeTag(Player player, String tag) {
        if (player.getTags().contains(tag)) {
            player.removeTag(tag);
        }
    }

    private static void playAnimation(Player player, String animationId, int fadeDuration) {
        if (player instanceof ServerPlayer serverPlayer) {
            BYAnimationPacket packet = new BYAnimationPacket(player.getUUID(), animationId, fadeDuration);
            PacketHandler.sendToClient(serverPlayer, packet);
            PacketHandler.sendToAllTracking(serverPlayer, packet);
        }
    }

    /**
     * 伤害事件中调用，用于屏障吸收
     */
    public static boolean isBloodBarrierActive(Player player) {
        return BARRIER_ACTIVE.containsKey(player.getUUID());
    }

    /**
     * 处理屏障吸收伤害（在伤害事件中调用）
     */
    public static float handleBarrierAbsorption(Player player, float damage) {
        UUID playerId = player.getUUID();
        if (!BARRIER_ACTIVE.containsKey(playerId)) return damage;

        float absorbed = BARRIER_ABSORBED.getOrDefault(playerId, 0f);
        float remaining = BARRIER_ABSORPTION - absorbed;

        if (remaining <= 0) {
            deactivateBarrier(player);
            return damage;
        }

        float absorbedAmount = Math.min(damage, remaining);
        BARRIER_ABSORBED.put(playerId, absorbed + absorbedAmount);

        if (absorbedAmount >= remaining) {
            deactivateBarrier(player);
        }

        return damage - absorbedAmount;
    }

    // 辅助方法
    private static void kickSequence(Player player, int ticks) {
        RideBattleAPI.scheduleTicks(10, () -> addTag(player, "rider_kicking"));
        RideBattleAPI.scheduleTicks(ticks, () -> removeTag(player, "rider_kicking"));
    }

    private static int calculateTolerance(int origin) {
        int toleranceTicks = Config.SKILL_TOLERANCE_TIME.get() * 20;
        return origin + toleranceTicks;
    }

    public static void addResistance(Player player, int duration) {
        addEffect(player, MobEffects.DAMAGE_RESISTANCE, duration, 4);
    }

    public static void addEffect(Player player, Holder<MobEffect> effect, int duration, int level) {
        player.addEffect(new MobEffectInstance(effect, duration, level, true, false));
    }

    private static void riderKickJump(Player player, double jumpHeight) {
        if (player == null) return;
        Vec3 currentMovement = player.getDeltaMovement();
        Vec3 jump = new Vec3(currentMovement.x, currentMovement.y + jumpHeight, currentMovement.z);
        addDeltaMovement(player, jump);
    }

    private static void riderKickForward(Player player, double norm, int ticks) {
        if (player == null) return;
        RideBattleAPI.scheduleTicks(ticks, () -> {
            Vec3 lookVec = player.getLookAngle();
            Vec3 movement = player.getDeltaMovement();
            Vec3 kick = new Vec3(
                    movement.x + lookVec.x * norm * 1.5,
                    movement.y + lookVec.y * norm,
                    movement.z + lookVec.z * norm * 1.5
            );
            addDeltaMovement(player, kick);
        });
    }

    private static void addDeltaMovement(Player player, Vec3 movement) {
        player.addDeltaMovement(movement);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new PlayerMovementPacket(player.getUUID(), movement.x(), movement.y(), movement.z(), "add"));
        }
    }

    private static void setDeltaMovement(Player player, double x, double y, double z) {
        player.setDeltaMovement(new Vec3(x, y, z));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new PlayerMovementPacket(player.getUUID(), x, y, z, "set"));
        }
    }

    private static void setDeltaMovement(Player player, Vec3 movement) {
        setDeltaMovement(player, movement.x(), movement.y(), movement.z());
    }

    private static void hurt(Player player, LivingEntity target, float amount) {
        if (!target.level().isClientSide() && target.isAlive()) {
            target.hurt(target.damageSources().mobAttack(player), amount);
        }
    }

    private static void createExplosion(Player player, double x, double y, double z, float damage) {
        Level level = player.level();
        boolean grief = Config.SKILL_EXPLODE_GRIEF.get();
        Level.ExplosionInteraction interaction = grief ?
                Level.ExplosionInteraction.BLOCK :
                Level.ExplosionInteraction.NONE;
        level.explode(player, x, y, z, damage * 0.1f, false, interaction);
    }
}