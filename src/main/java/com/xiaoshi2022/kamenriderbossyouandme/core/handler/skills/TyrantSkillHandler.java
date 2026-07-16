package com.xiaoshi2022.kamenriderbossyouandme.core.handler.skills;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.event.SkillEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.PlayerMovementPacket;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.riders.gaim.TyrantConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class TyrantSkillHandler {

    private static final Map<ResourceLocation, Consumer<Player>> SKILL_MAP = new HashMap<>();
    private static final List<ResourceLocation> TAGGED_SKILLS = new ArrayList<>();

    private static final float TYRANT_PUNCH_DAMAGE = 30.0f;
    private static final float TYRANT_KICK_DAMAGE = 50.0f;
    private static final float TYRANT_JUMP_POWER = 2.5f;
    private static final float TYRANT_SPEED = 2.2f;

    // 虚化技能相关
    private static final int INTANGIBILITY_MAX_DURATION = 1200;
    private static final float INTANGIBILITY_BREAK_THRESHOLD = 60.0f;
    private static final Map<UUID, Integer> INTANGIBLE_PLAYERS = new HashMap<>();
    private static final Map<UUID, Integer> INTANGIBILITY_TIMERS = new HashMap<>();
    private static final Map<UUID, Double> TAKE_OFF_HEIGHTS = new HashMap<>();
    private static final double MAX_FLIGHT_HEIGHT_ABOVE_TAKE_OFF = 8.0;

    // 相位传送相关
    private static final int TELEPORT_RANGE = 20;
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static final int TELEPORT_COOLDOWN = 100;


    static {
        SKILL_MAP.put(RiderSkills.TYRANT_KICK, TyrantSkillHandler::executeTyrantKick);
        SKILL_MAP.put(RiderSkills.TYRANT_INTANGIBILITY, TyrantSkillHandler::executeTyrantIntangibility);

        TAGGED_SKILLS.add(RiderSkills.TYRANT_KICK);
    }

    @SubscribeEvent
    public static void onSkill(SkillEvent.Post event) {
        Player player = event.getPlayer();
        ResourceLocation skillId = event.getSkillId();
        handleSkill(player, skillId);
    }

    @SubscribeEvent
    public static void onDamageEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living)) return;
        handleDamageEntity(player, living);
    }

    @SubscribeEvent
    public static void onCollision(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        handleKickCollide(player);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        if (!player.level().isClientSide() && INTANGIBLE_PLAYERS.containsKey(playerId)) {
            if (player.isShiftKeyDown()) {
                performDirectPhasePenetration((ServerPlayer) player);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        if (!player.level().isClientSide() && INTANGIBLE_PLAYERS.containsKey(playerId)) {
            if (player.isShiftKeyDown()) {
                performDirectPhasePenetration((ServerPlayer) player);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        if (!player.level().isClientSide() && INTANGIBLE_PLAYERS.containsKey(playerId)) {
            if (player.isShiftKeyDown()) {
                performDirectPhasePenetration((ServerPlayer) player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (RideBattleAPI.isSpecificForm(player, TyrantConfig.TYRANT_BASE_ID) && player.isAlive()) {
            // ❌ 移除生命回复代码
            // UUID playerId = player.getUUID();
            // long currentTime = player.level().getGameTime();
            // long lastTime = LAST_RECOVERY_TIME.getOrDefault(playerId, 0L);
            // if (player.getHealth() < player.getMaxHealth() && currentTime - lastTime >= RECOVERY_COOLDOWN) {
            //     player.heal(HEAL_AMOUNT);
            //     LAST_RECOVERY_TIME.put(playerId, currentTime);
            // }

            // 只保留药水效果
            addEffectIfBetterOrAbsent(player, MobEffects.DAMAGE_RESISTANCE, 100, 0);
            addEffectIfBetterOrAbsent(player, MobEffects.FIRE_RESISTANCE, 100, 0);
            addEffectIfBetterOrAbsent(player, MobEffects.REGENERATION, 100, 0);
        }

        handleIntangiblePlayerTick(player);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (RideBattleAPI.isSpecificForm(player, TyrantConfig.TYRANT_BASE_ID) && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            float healAmount = target.getMaxHealth() * 0.01f;
            player.heal(healAmount);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        if (INTANGIBLE_PLAYERS.containsKey(playerId)) {
            float damageAmount = event.getNewDamage();

            if (damageAmount < INTANGIBILITY_BREAK_THRESHOLD) {
                event.setNewDamage(0);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.tyrant.damage_immune"), true);
                }
            } else {
                deactivateIntangibility(player);
                event.setNewDamage(damageAmount - INTANGIBILITY_BREAK_THRESHOLD);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.tyrant.intangibility.break"), true);
                }
            }
        }
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

            KamenRiderBossYOUandME.LOGGER.debug("玩家 {} 使用了技能: {}",
                    player.getName().getString(), skillId);
        }
    }

    private static void executeTyrantKick(Player player) {
        int duration = calculateTolerance(60);

        kickSequence(player, duration);
        addTag(player, "skill_tyrant_kick");
        addResistance(player, duration);

        riderKickJump(player, TYRANT_JUMP_POWER);
        riderKickForward(player, TYRANT_SPEED, 10);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(),
                    20, 0.5, 0.2, 0.5, 0.05);
        }

        playAnimation(player, "kick", 2);

        RideBattleAPI.scheduleTicks(duration, () -> {
            removeTag(player, "skill_tyrant_kick");
            removeTag(player, "rider_kicking");
        });
    }

    private static void executeTyrantIntangibility(Player player) {
        UUID playerId = player.getUUID();

        if (INTANGIBLE_PLAYERS.containsKey(playerId)) {
            deactivateIntangibility(player);
        } else {
            activateIntangibility(player);
        }
    }

    private static void performDirectPhasePenetration(ServerPlayer player) {
        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();

        if (TELEPORT_COOLDOWNS.containsKey(playerId)) {
            long lastPenetrationTime = TELEPORT_COOLDOWNS.get(playerId);
            if (currentTime - lastPenetrationTime < TELEPORT_COOLDOWN) {
                int remainingTicks = (int) (TELEPORT_COOLDOWN - (currentTime - lastPenetrationTime));
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.tyrant.teleport.cooldown", remainingTicks / 20), true);
                return;
            }
        }

        TELEPORT_COOLDOWNS.put(playerId, currentTime);

        Vec3 startPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        for (double distance = 1.0; distance <= 10.0; distance += 1.0) {
            Vec3 testPos = startPos.add(lookVec.scale(distance));
            BlockPos blockPos = new BlockPos((int) testPos.x, (int) testPos.y, (int) testPos.z);

            if (isSafePenetrationLocation(player.level(), blockPos, player)) {
                player.teleportTo(testPos.x, testPos.y, testPos.z);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.tyrant.teleport.complete"), true);
                spawnTeleportParticles(player);
                return;
            }
        }

        Vec3 farthestPos = startPos.add(lookVec.scale(10.0));
        BlockPos farthestBlockPos = new BlockPos((int) farthestPos.x, (int) farthestPos.y, (int) farthestPos.z);

        Vec3 forcedPos = findForcedSafePosition(player.level(), farthestBlockPos, player);
        if (forcedPos != null) {
            player.teleportTo(forcedPos.x, forcedPos.y, forcedPos.z);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.tyrant.teleport.complete"), true);
            spawnTeleportParticles(player);
        } else {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.tyrant.teleport.no_target"), true);
        }
    }

    private static void handleKickCollide(Player player) {
        if (!player.getTags().contains("rider_kicking")) return;
        if (!player.getTags().contains("skill_tyrant_kick")) return;

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

        for (LivingEntity entity : entities) {
            createKickExplosion(player, entity, TYRANT_KICK_DAMAGE);
            removeTag(player, "skill_tyrant_kick");
            removeTag(player, "rider_kicking");
            break;
        }
    }

    private static void handleIntangiblePlayerTick(Player player) {
        UUID playerId = player.getUUID();

        if (INTANGIBLE_PLAYERS.containsKey(playerId)) {
            if (!player.level().isClientSide()) {
                int remainingTime = INTANGIBILITY_TIMERS.getOrDefault(playerId, 0) - 1;

                if (remainingTime <= 0) {
                    deactivateIntangibility(player);
                } else {
                    Double takeOffHeight = TAKE_OFF_HEIGHTS.get(playerId);
                    if (takeOffHeight != null) {
                        double currentHeight = player.getY();
                        if (currentHeight - takeOffHeight > MAX_FLIGHT_HEIGHT_ABOVE_TAKE_OFF) {
                            if (player instanceof ServerPlayer serverPlayer) {
                                serverPlayer.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("message.tyrant.intangibility.height_limit"), true);
                            }
                            deactivateIntangibility(player);
                            return;
                        }
                    }

                    INTANGIBILITY_TIMERS.put(playerId, remainingTime);

                    if (player instanceof ServerPlayer serverPlayer) {
                        if (!serverPlayer.getAbilities().mayfly) {
                            serverPlayer.getAbilities().mayfly = true;
                            serverPlayer.getAbilities().flying = true;
                            serverPlayer.onUpdateAbilities();
                        }

                        if (remainingTime % 20 == 0) {
                            spawnPhaseModeParticlesOnServer(serverPlayer);
                        }

                        if (remainingTime % 100 == 0) {
                            int seconds = remainingTime / 20;
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.translatable("message.tyrant.intangibility.time", seconds), true);
                        }
                    }
                }
            }
        }
    }

    private static void activateIntangibility(Player player) {
        UUID playerId = player.getUUID();

        INTANGIBLE_PLAYERS.put(playerId, 0);
        INTANGIBILITY_TIMERS.put(playerId, INTANGIBILITY_MAX_DURATION);
        TAKE_OFF_HEIGHTS.put(playerId, player.getY());
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.tyrant.intangibility.activate"), true);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getAbilities().mayfly = true;
            serverPlayer.getAbilities().flying = true;
            serverPlayer.onUpdateAbilities();

            serverPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
        }
    }

    private static void deactivateIntangibility(Player player) {
        UUID playerId = player.getUUID();

        INTANGIBLE_PLAYERS.remove(playerId);
        INTANGIBILITY_TIMERS.remove(playerId);
        TAKE_OFF_HEIGHTS.remove(playerId);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.tyrant.intangibility.deactivate"), true);

        if (player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
            }
            serverPlayer.onUpdateAbilities();
            serverPlayer.removeEffect(MobEffects.GLOWING);
        }
    }

    private static void kickSequence(Player player, int ticks) {
        RideBattleAPI.scheduleTicks(10, () -> addTag(player, "rider_kicking"));
        RideBattleAPI.scheduleTicks(ticks, () -> removeTag(player, "rider_kicking"));
    }

    private static int calculateTolerance(int origin) {
        // 使用配置的额外容错时间（秒），转换为tick
        int toleranceTicks = Config.SKILL_TOLERANCE_TIME.get() * 20;
        return origin + toleranceTicks;
    }

    public static void addResistance(Player player, int duration) {
        addEffect(player, MobEffects.DAMAGE_RESISTANCE, duration, 4);
    }

    public static void addEffect(Player player, Holder<MobEffect> effect, int duration, int level) {
        player.addEffect(new MobEffectInstance(effect, duration, level, true, false));
    }

    private static void addEffectIfBetterOrAbsent(Player player, Holder<MobEffect> effect, int duration, int amplifier) {
        MobEffectInstance existingEffect = player.getEffect(effect);
        if (existingEffect == null || existingEffect.getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
        }
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

    private static void createExplosion(Player player, double x, double y, double z, float damage) {
        Level level = player.level();
        boolean grief = Config.SKILL_EXPLODE_GRIEF.get();
        Level.ExplosionInteraction interaction = grief ?
                Level.ExplosionInteraction.BLOCK :
                Level.ExplosionInteraction.NONE;
        level.explode(
                player,
                x, y, z,
                damage * 0.1f,
                false,
                interaction
        );
    }

    private static void createKickExplosion(Player player, LivingEntity entity, float damage) {
        BlockPos pos = entity.getOnPos();
        createExplosion(player, pos.getX(), pos.getY() + 1.5, pos.getZ(), damage);

        hurt(player, entity, damage);

        Vec3 angle = player.getLookAngle();
        Vec3 back = new Vec3(-(angle.x * 2), 0.5, -(angle.z * 2));
        setDeltaMovement(player, 0, 0, 0);
        addDeltaMovement(player, back);
    }

    private static void handleDamageEntity(Player player, LivingEntity living) {
        if (RideBattleAPI.isSpecificForm(player, TyrantConfig.TYRANT_BASE_ID)) {
        }
    }

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

    private static void hurt(Player player, LivingEntity target, float amount) {
        if (!target.level().isClientSide() && target.isAlive()) {
            target.hurt(target.damageSources().mobAttack(player), amount);
        }
    }

    private static Vec3 calculateSafeTeleportPosition(Level level, BlockPos hitPos, net.minecraft.core.Direction direction, Player player) {
        for (int distance = 1; distance <= 3; distance++) {
            BlockPos testPos = hitPos.relative(direction, distance);
            if (isSafeTeleportLocation(level, testPos, player)) {
                return new Vec3(
                        testPos.getX() + 0.5,
                        testPos.getY(),
                        testPos.getZ() + 0.5
                );
            }
        }
        return null;
    }

    private static boolean isSafeTeleportLocation(Level level, BlockPos pos, Player player) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }
        return true;
    }

    private static boolean isSafePenetrationLocation(Level level, BlockPos pos, Player player) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }
        return true;
    }

    private static Vec3 findForcedSafePosition(Level level, BlockPos startPos, Player player) {
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos testPos = startPos.offset(x, y, z);
                    if (isSafePenetrationLocation(level, testPos, player)) {
                        return new Vec3(
                                testPos.getX() + 0.5,
                                testPos.getY(),
                                testPos.getZ() + 0.5
                        );
                    }
                }
            }
        }
        return null;
    }

    private static void spawnTeleportParticles(ServerPlayer player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        Vec3 pos = player.position();

        DustParticleOptions brightRedParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.1F, 0.1F),
                1.2F
        );

        serverLevel.sendParticles(
                brightRedParticle,
                pos.x, pos.y, pos.z,
                30, 1.5, 1.5, 1.5, 0.1
        );

        DustParticleOptions lightRedParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.4F, 0.4F),
                0.8F
        );

        serverLevel.sendParticles(
                lightRedParticle,
                pos.x, pos.y, pos.z,
                20, 1.2, 1.2, 1.2, 0.05
        );
    }

    private static void spawnPhaseModeParticlesOnServer(ServerPlayer player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        Vec3 pos = player.position();
        RandomSource random = player.getRandom();

        DustParticleOptions coreParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.0F, 0.0F),
                3.0F
        );

        for (int i = 0; i < 60; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 0.8;

            double offsetX = Math.cos(angle) * radius;
            double offsetY = random.nextGaussian() * 0.3 + 1.0;
            double offsetZ = Math.sin(angle) * radius;

            double velocityX = -offsetX * 0.15;
            double velocityY = 0.02 + random.nextDouble() * 0.02;
            double velocityZ = -offsetZ * 0.15;

            serverLevel.sendParticles(
                    coreParticle,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0,
                    velocityX, velocityY, velocityZ,
                    0.1
            );
        }

        DustParticleOptions middleParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.2F, 0.2F),
                2.0F
        );

        Vec3 movement = player.getDeltaMovement();
        for (int i = 0; i < 100; i++) {
            double tailLength = Math.min(movement.length() * 4, 4.0);
            Vec3 tailDirection = movement.length() > 0 ? movement.normalize().scale(-1) : new Vec3(0, 0, 0);

            double tailOffset = random.nextDouble() * tailLength;
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 1.2;

            double offsetX = tailDirection.x * tailOffset + Math.cos(angle) * radius;
            double offsetY = tailDirection.y * tailOffset + (random.nextDouble() - 0.5) * 1.2;
            double offsetZ = tailDirection.z * tailOffset + Math.sin(angle) * radius;

            double velocityX = tailDirection.x * 0.3 + (random.nextDouble() - 0.5) * 0.1;
            double velocityY = tailDirection.y * 0.3 + (random.nextDouble() - 0.5) * 0.1;
            double velocityZ = tailDirection.z * 0.3 + (random.nextDouble() - 0.5) * 0.1;

            serverLevel.sendParticles(
                    middleParticle,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0,
                    velocityX, velocityY, velocityZ,
                    0.2
            );
        }
    }

    private static void playAnimation(Player player, String animationId, int fadeDuration) {
        if (player instanceof ServerPlayer serverPlayer) {
            BYAnimationPacket packet = new BYAnimationPacket(player.getUUID(), animationId, fadeDuration);
            PacketHandler.sendToClient(serverPlayer, packet);
            PacketHandler.sendToAllTracking(serverPlayer, packet);
        }
    }

    private static void addDeltaMovement(Player player, double x, double y, double z) {
        player.addDeltaMovement(new Vec3(x, y, z));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new PlayerMovementPacket(player.getUUID(), x, y, z, "add"));
        }
    }

    private static void addDeltaMovement(Player player, Vec3 movement) {
        addDeltaMovement(player, movement.x(), movement.y(), movement.z());
    }

    private static void setDeltaMovement(Player player, double x, double y, double z) {
        player.setDeltaMovement(new Vec3(x, y, z));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new PlayerMovementPacket(player.getUUID(), x, y, z, "set"));
        }
    }

    private static void setDeltaMovement(Player player, Vec3 movement) {
        setDeltaMovement(player, movement.x(), movement.y(), movement.z());
    }
}