package com.xiaoshi2022.kamenriderbossyouandme.core.handler.skills;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.event.SkillEvent;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.riders.driver.BrainConfig;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.PlayerMovementPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;
import java.util.function.Consumer;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class BrainSkillHandler {

    private static final Map<ResourceLocation, Consumer<Player>> SKILL_MAP = new HashMap<>();
    private static final List<ResourceLocation> TAGGED_SKILLS = new ArrayList<>();

    // Brain骑士数据
    private static final float BRAIN_PUNCH_DAMAGE = 17.5f;      // 拳力 17.5t
    private static final float BRAIN_KICK_DAMAGE = 25.1f;      // 踢力 25.1t
    private static final float BRAIN_HEADBUTT_DAMAGE = 33.3f;  // 头槌力 33.3t
    private static final float BRAIN_JUMP_POWER = 2.5f;        // 跳跃力 42.0m (游戏内换算)
    private static final float BRAIN_SPEED = 1.8f;             // 跑速 100m/3.0s

    static {
        // 注册Brain的三个技能
        SKILL_MAP.put(RiderSkills.BRAIN_HEADBUTT, BrainSkillHandler::executeBrainHeadbutt);
        SKILL_MAP.put(RiderSkills.BRAIN_POISON, BrainSkillHandler::executeBrainPoison);
        SKILL_MAP.put(RiderSkills.BRAIN_KICK, BrainSkillHandler::executeBrainKick);

        // 标记需要添加标签的技能
        TAGGED_SKILLS.add(RiderSkills.BRAIN_HEADBUTT);
        TAGGED_SKILLS.add(RiderSkills.BRAIN_KICK);
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
        handleHeadbuttCollide(player);
    }

    private static void handleSkill(Player player, ResourceLocation skillId) {
        // 添加技能标签
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

    // ==================== Brain技能实现 ====================

    private static void executeBrainHeadbutt(Player player) {
        int duration = calculateTolerance(30); // 1.5秒

        // 头槌序列
        headbuttSequence(player, duration);

        // 添加抗性
        addResistance(player, duration);

        // 向前冲刺 (基于Brain的跑速)
        riderJump(player, 0.3); // 轻微跳跃
        riderForward(player, BRAIN_SPEED * 1.2, 5); // 基于跑速的冲刺

        // 播放音效和粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
        }

        // 播放动画
        playAnimation(player, "brain_headbutt", 2);

        // 定时移除标签
        RideBattleAPI.scheduleTicks(duration, () -> removeTag(player, "skill_brain_headbutt"));
    }

    private static void executeBrainPoison(Player player) {
        // 检查是否有纸
        boolean hasPaper = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                hasPaper = true;
                break;
            }
        }

        if (!hasPaper) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c没有纸无法使用剧毒手帕！"), true);
            return;
        }

        // 添加抗性
        addResistance(player, 20);

        // 播放音效和粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.SPIT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 2.0, 1.0, 2.0, 0.1);

            // 延迟产生毒雾效果
            RideBattleAPI.scheduleTicks(5, () -> {
                AABB area = player.getBoundingBox().inflate(5.0);
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class, area,
                        entity -> entity != player && entity.isAlive()
                );

                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
                    target.hurt(target.damageSources().playerAttack(player), BRAIN_PUNCH_DAMAGE * 0.5f); // 毒手帕伤害约为拳力一半
                }
            });
        }

        // 播放动画
        playAnimation(player, "stamps", 2);
    }

    private static void executeBrainKick(Player player) {
        int duration = calculateTolerance(60); // 3秒

        // 踢击序列
        kickSequence(player, duration);

        // 添加技能标签
        addTag(player, "skill_brain_kick");

        // 添加抗性
        addResistance(player, duration);

        // 骑士踢跳跃和前进 (基于Brain的跳跃力和跑速)
        riderKickJump(player, BRAIN_JUMP_POWER); // 跳跃高度 42.0m
        riderKickForward(player, BRAIN_SPEED, 10); // 向前飞踢

        // 播放音效和粒子
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(),
                    20, 0.5, 0.2, 0.5, 0.05);
        }

        // 播放动画
        playAnimation(player, "kick", 2);

        // 定时移除标签
        RideBattleAPI.scheduleTicks(duration, () -> {
            removeTag(player, "skill_brain_kick");
            removeTag(player, "rider_kicking");
        });
    }

    // ==================== 碰撞检测 ====================

    private static void handleKickCollide(Player player) {
        if (!player.getTags().contains("rider_kicking")) return;

        // 获取踢击技能标签
        List<String> skillTags = player.getTags().stream()
                .filter(tag -> tag.startsWith("skill_") && tag.contains("brain_kick"))
                .toList();
        if (skillTags.isEmpty()) return;

        Level level = player.level();

        // 水平方向（避免踢到天上）
        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();

        // 前方扩展距离
        double forwardDistance = 0.8;

        // 扩展玩家碰撞盒
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
            for (String skillTag : skillTags) {
                if (skillTag.equals("skill_brain_kick")) {
                    // 创建踢击爆炸 (使用Brain的踢力)
                    createKickExplosion(player, entity, BRAIN_KICK_DAMAGE);

                    // 移除标签
                    removeTag(player, skillTag);
                    removeTag(player, "rider_kicking");
                }
            }
            break; // 只攻击第一个
        }
    }

    private static void handleHeadbuttCollide(Player player) {
        if (!player.getTags().contains("rider_headbutting")) return;

        // 获取头槌技能标签
        List<String> skillTags = player.getTags().stream()
                .filter(tag -> tag.startsWith("skill_") && tag.contains("brain_headbutt"))
                .toList();
        if (skillTags.isEmpty()) return;

        Level level = player.level();
        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();

        // 前方扩展距离
        double forwardDistance = 0.6;

        // 扩展玩家碰撞盒
        AABB headbuttBox = player.getBoundingBox()
                .expandTowards(horizontalLook.scale(forwardDistance))
                .inflate(0.5);

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                headbuttBox,
                e -> e != player && e.isAlive()
        );

        if (entities.isEmpty()) return;

        for (LivingEntity entity : entities) {
            for (String skillTag : skillTags) {
                if (skillTag.equals("skill_brain_headbutt")) {
                    // 造成伤害 (使用Brain的头槌力)
                    hurt(player, entity, BRAIN_HEADBUTT_DAMAGE);
                    knockBack(player, entity, 3.5f); // 头槌击退更强

                    // 粒子效果
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                                15, 0.5, 0.5, 0.5, 0.2);
                    }

                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // 移除标签
                    removeTag(player, skillTag);
                    removeTag(player, "rider_headbutting");
                }
            }
            break; // 只攻击第一个
        }
    }

    // ==================== 辅助方法 ====================

    private static void kickSequence(Player player, int ticks) {
        RideBattleAPI.scheduleTicks(10, () -> addTag(player, "rider_kicking"));
        RideBattleAPI.scheduleTicks(ticks, () -> removeTag(player, "rider_kicking"));
    }

    private static void headbuttSequence(Player player, int ticks) {
        RideBattleAPI.scheduleTicks(5, () -> addTag(player, "rider_headbutting"));
        RideBattleAPI.scheduleTicks(ticks, () -> removeTag(player, "rider_headbutting"));
    }

    private static int calculateTolerance(int origin) {
        return origin; // 可以添加配置
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

    private static void riderJump(Player player, double jumpHeight) {
        if (player == null) return;
        Vec3 currentMovement = player.getDeltaMovement();
        Vec3 jump = new Vec3(currentMovement.x, currentMovement.y + jumpHeight, currentMovement.z);
        addDeltaMovement(player, jump);
    }

    private static void riderForward(Player player, double norm, int ticks) {
        if (player == null) return;
        RideBattleAPI.scheduleTicks(ticks, () -> {
            Vec3 lookVec = player.getLookAngle();
            Vec3 movement = player.getDeltaMovement();
            Vec3 forward = new Vec3(
                    movement.x + lookVec.x * norm,
                    movement.y + lookVec.y * norm * 0.5,
                    movement.z + lookVec.z * norm
            );
            addDeltaMovement(player, forward);
        });
    }

    // ✅ 修复：1.21.1 中 addDeltaMovement 只接受 Vec3
    private static void addDeltaMovement(Player player, double x, double y, double z) {
        player.addDeltaMovement(new Vec3(x, y, z));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new PlayerMovementPacket(player.getUUID(), x, y, z, "add"));
        }
    }

    private static void addDeltaMovement(Player player, Vec3 movement) {
        addDeltaMovement(player, movement.x(), movement.y(), movement.z());
    }

    // ✅ 修复：1.21.1 中 setDeltaMovement 只接受 Vec3
    private static void setDeltaMovement(Player player, double x, double y, double z) {
        player.setDeltaMovement(new Vec3(x, y, z));  // ✅ 使用 Vec3
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new PlayerMovementPacket(player.getUUID(), x, y, z, "set"));
        }
    }

    private static void setDeltaMovement(Player player, Vec3 movement) {
        setDeltaMovement(player, movement.x(), movement.y(), movement.z());
    }

    private static void createExplosion(Player player, double x, double y, double z, float damage) {
        Level level = player.level();
        level.explode(
                player,
                x, y, z,
                damage * 0.1f, // 爆炸威力按伤害比例缩小
                false,
                Level.ExplosionInteraction.NONE
        );
    }

    private static void createExplosion(Player player, LivingEntity entity, float damage) {
        BlockPos pos = entity.getOnPos();
        createExplosion(player, pos.getX(), pos.getY(), pos.getZ(), damage);
    }

    private static void createKickExplosion(Player player, LivingEntity entity, float damage) {
        BlockPos pos = entity.getOnPos();
        createExplosion(player, pos.getX(), pos.getY() + 1.5, pos.getZ(), damage);

        hurt(player, entity, damage);

        Vec3 angle = player.getLookAngle();
        Vec3 current = player.getKnownMovement();
        Vec3 back = new Vec3(-(angle.x * current.x), 0.5, -(angle.z * current.z));
        setDeltaMovement(player, 0, 0, 0);
        addDeltaMovement(player, back);
    }

    private static void handleDamageEntity(Player player, LivingEntity living) {
        if (RideBattleAPI.isSpecificForm(player, BrainConfig.BRAIN_BASE_ID)) {
            // 可以添加特殊效果
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

    private static void knockBack(Player player, LivingEntity target, float amount) {
        if (!target.level().isClientSide() && target.isAlive()) {
            target.knockback(amount, -player.getLookAngle().x, -player.getLookAngle().z);
        }
    }

    private static void playAnimation(Player player, String animationId, int fadeDuration) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToClient(serverPlayer,
                    new BYAnimationPacket(player.getUUID(), animationId, fadeDuration));
        }
    }
}