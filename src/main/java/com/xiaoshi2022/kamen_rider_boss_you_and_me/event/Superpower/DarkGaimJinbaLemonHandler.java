package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

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
    private static final double KICK_ENHANCE_COST = 15.0; // 踢力增强消耗骑士能量
    private static final double BLINDNESS_FIELD_COST = 25.0; // 失明领域消耗骑士能量
    private static final double HELHEIM_CRACK_COST = 40.0; // 赫尔海姆裂缝消耗骑士能量

    private static final float DAMAGE_BOOST_MULTIPLIER = 1.3f; // 伤害提升倍数
    private static final int BLINDNESS_FIELD_RADIUS = 5; // 失明领域半径
    private static final int BLINDNESS_DURATION = 100; // 失明效果持续时间(刻)
    private static final int HELHEIM_CRACK_COOLDOWN = 400; // 赫尔海姆裂缝冷却时间(刻)，20秒

    /* ====== 主 Tick：邪気回复 + 套装力量 + 套装检测 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;

        // 1. 检测玩家是否穿戴完整的DarkOrangels盔甲
        boolean isFull = isJinba(sp);

        /* 邪気只在整套时回复 */
        if (isFull) {
            int aura = getEvilAura(sp);
            if (aura < MAX_EVIL_AURA) {
                setEvilAura(sp, Math.min(MAX_EVIL_AURA, aura + EVIL_AURA_REGEN));
            }
        }

        /* 2. 套装力量：已在Dark_orangels.java的tick方法中通过applyStrengthEffect实现 */
        // 不再重复应用力量效果，避免控制台重复输出
        updateBaseBuff(sp);
        
        // 3. 检查并移除过期的踢力增强效果
        checkAndRemoveKickEnhance(sp);
    }

    /* ====== 能力触发方法（可被网络包直接调用） ====== */

    /** 技能1：踢力增强 - 消耗骑士能量，伤害提升1.3倍，并生成黑色蒸汽粒子 */
    public static void activateKickEnhance(ServerPlayer sp) {
        if (!isJinba(sp)) return;
        
        // 消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, KICK_ENHANCE_COST)) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)KICK_ENHANCE_COST + " 点能量"));
            return;
        }
        
        // 设置踢力增强状态，持续30秒
        sp.getPersistentData().putBoolean("DarkGaimKickEnhance", true);
        sp.getPersistentData().putLong("DarkGaimKickEnhanceExpiry", sp.level().getGameTime() + 600);
        
        // 生成黑色蒸汽粒子
        spawnDarkSteamParticles(sp);
        
        // 播放音效
        play(sp, SoundEvents.ARMOR_EQUIP_NETHERITE, 1.0f, 0.8f);
        
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("踢力增强已激活！伤害提升1.3倍"));
    }
    
    /** 技能2：失明领域 - 消耗骑士能量，在5x5范围内生成失明效果和黑色烟雾粒子 */
    public static void activateBlindnessField(ServerPlayer sp) {
        if (!isJinba(sp)) return;
        
        // 消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, BLINDNESS_FIELD_COST)) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)BLINDNESS_FIELD_COST + " 点能量"));
            return;
        }
        
        Level level = sp.level();
        BlockPos centerPos = sp.blockPosition();
        
        // 获取范围内的所有实体
        AABB box = new AABB(centerPos).inflate(BLINDNESS_FIELD_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
        
        // 为范围内的实体添加失明效果
        for (LivingEntity entity : entities) {
            if (entity != sp) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION, 0));
            }
        }
        
        // 生成黑色烟雾粒子
        spawnBlackSmokeParticles(level, centerPos, BLINDNESS_FIELD_RADIUS);
        
        // 播放音效
        play(sp, SoundEvents.SMOKER_SMOKE, 1.0f, 1.0f);
        
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("失明领域已激活！5x5范围内的敌人被致盲"));
    }
    
    /** 技能3：赫尔海姆裂缝 - 消耗骑士能量，打开裂缝将敌人吸入赫尔海姆维度 */
    public static void activateHelheimCrack(ServerPlayer sp) {
        if (!isJinba(sp)) return;
        
        // 检查冷却时间
        long currentTime = sp.level().getGameTime();
        long cooldownEndTime = sp.getPersistentData().getLong("DarkGaimHelheimCrackCooldown");
        
        if (currentTime < cooldownEndTime) {
            // 计算剩余冷却时间（秒）
            int remainingSeconds = (int)((cooldownEndTime - currentTime) / 20);
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("赫尔海姆裂缝正在冷却中，还需 " + remainingSeconds + " 秒"));
            return;
        }
        
        // 消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, HELHEIM_CRACK_COST)) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)HELHEIM_CRACK_COST + " 点能量"));
            return;
        }
        
        // 设置冷却时间
        sp.getPersistentData().putLong("DarkGaimHelheimCrackCooldown", currentTime + HELHEIM_CRACK_COOLDOWN);
        
        // 生成赫尔海姆裂缝并将敌人吸入
        generateHelheimCrackAndSuckEnemies(sp);
        
        // 播放音效
        play(sp, SoundEvents.PORTAL_TRIGGER, 1.0f, 0.8f);
        
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("赫尔海姆裂缝已打开！造成伤害并施加失明和缓慢效果"));
    }

    /* ====== 被动：伤害提升效果处理 ====== */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;
        if (!isJinba(sp)) return;
        
        // 检查是否激活了踢力增强
        if (sp.getPersistentData().getBoolean("DarkGaimKickEnhance")) {
            // 将伤害提升1.3倍
            float originalDamage = e.getAmount();
            float boostedDamage = originalDamage * DAMAGE_BOOST_MULTIPLIER;
            e.setAmount(boostedDamage);

            // 为攻击添加粒子效果
            if (e.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) e.getEntity();
                spawnKickImpactParticles(sp, target);
            }
        }
    }

    /* ====== 粒子效果方法 ====== */
    
    /** 生成黑色蒸汽粒子 */
    private static void spawnDarkSteamParticles(ServerPlayer sp) {
        Level level = sp.level();
        for (int i = 0; i < 30; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            double dy = sp.getRandom().nextDouble() * 1.5;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 2.0;
            
            // 使用大型烟雾粒子作为黑色蒸汽
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    sp.getX() + dx,
                    sp.getY() + 0.2 + dy,
                    sp.getZ() + dz,
                    0.0, 0.05, 0.0
            );
        }
    }
    
    /** 生成黑色烟雾粒子 */
    private static void spawnBlackSmokeParticles(Level level, BlockPos centerPos, int radius) {
        // 确保在服务端正确生成粒子并同步到客户端
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 200; i++) {  // 增加粒子数量以增强效果
                double dx = centerPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * radius * 2;
                double dy = centerPos.getY() + 0.5 + level.random.nextDouble() * 3;
                double dz = centerPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * radius * 2;
                
                // 使用大型烟雾粒子增强可见度
                serverLevel.sendParticles(
                        ParticleTypes.LARGE_SMOKE, // 使用更明显的大型烟雾粒子
                        dx,
                        dy,
                        dz,
                        1, // 每个位置发送1个粒子
                        (level.random.nextDouble() - 0.5) * 0.2,
                        level.random.nextDouble() * 0.1 + 0.05,
                        (level.random.nextDouble() - 0.5) * 0.2,
                        0.01
                );
            }
        }
    }
    
    /** 生成踢击冲击粒子 */
    private static void spawnKickImpactParticles(ServerPlayer sp, LivingEntity target) {
        Level level = sp.level();
        for (int i = 0; i < 15; i++) {
            double dx = (sp.getRandom().nextDouble() - 0.5) * 1.0;
            double dy = sp.getRandom().nextDouble() * 1.0;
            double dz = (sp.getRandom().nextDouble() - 0.5) * 1.0;
            
            // 使用火焰粒子作为冲击特效
            level.addParticle(
                    ParticleTypes.FLAME,
                    target.getX() + dx,
                    target.getY() + 0.5 + dy,
                    target.getZ() + dz,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }
    
    /* ====== 赫尔海姆裂缝相关方法 ====== */
    
    /** 生成赫尔海姆裂缝，造成伤害并施加状态效果 */
    private static void generateHelheimCrackAndSuckEnemies(ServerPlayer sp) {
        Level level = sp.level();
        BlockPos centerPos = sp.blockPosition();
        
        // 生成2个赫尔海姆裂缝方块
        for (int i = 0; i < 2; i++) {
            int offsetX = sp.getRandom().nextInt(5) - 2;
            int offsetY = sp.getRandom().nextInt(3);
            int offsetZ = sp.getRandom().nextInt(5) - 2;
            
            BlockPos crackPos = centerPos.offset(offsetX, offsetY, offsetZ);
            
            // 检查位置是否为空
            if (level.isEmptyBlock(crackPos)) {
                // 设置赫尔海姆裂缝方块
                level.setBlockAndUpdate(
                        crackPos,
                        ModBlocks.HELHEIM_CRACK_BLOCK.get().defaultBlockState()
                );
                
                // 生成裂缝粒子效果
                spawnCrackParticles(level, crackPos.getX() + 0.5, crackPos.getY() + 0.5, crackPos.getZ() + 0.5);
            }
        }
        
        // 检测范围内的敌人
        AABB livingDetectionArea = new AABB(centerPos).inflate(6.0);
        List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, livingDetectionArea,
                e -> e != sp && !e.isAlliedTo(sp) && !e.isPassenger() && !e.isVehicle());
        
        // 为范围内的敌人添加失明和缓慢效果
        for (LivingEntity entity : livingEntities) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0)); // 失明效果，持续10秒
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1)); // 缓慢2效果，持续10秒
        }
        
        // 在裂缝中心位置产生爆炸，造成90点伤害
        createExplosion(level, centerPos, sp);
    }
    
    /** 创建爆炸效果，造成90点伤害 */
    private static void createExplosion(Level level, BlockPos pos, ServerPlayer sp) {
        if (level instanceof ServerLevel serverLevel) {
            // 创建一个不会破坏方块的爆炸
            serverLevel.explode(
                    sp,                   // 爆炸源（玩家）
                    pos.getX() + 0.5,     // 爆炸X坐标
                    pos.getY() + 0.5,     // 爆炸Y坐标
                    pos.getZ() + 0.5,     // 爆炸Z坐标
                    2.0f,                 // 爆炸半径
                    Level.ExplosionInteraction.NONE  // 不破坏方块
            );
            
            // 手动对范围内的实体造成90点伤害
            AABB explosionArea = new AABB(pos).inflate(3.0);
            List<LivingEntity> affectedEntities = level.getEntitiesOfClass(LivingEntity.class, explosionArea,
                    e -> e != sp && !e.isAlliedTo(sp));
            
            for (LivingEntity entity : affectedEntities) {
                // 创建伤害来源
                DamageSource explosionDamage = sp.damageSources().explosion(sp, null);
                
                // 对实体造成90点伤害，忽略盔甲防御
                entity.hurt(explosionDamage, 90.0f);
                
                // 添加爆炸冲击粒子
                spawnExplosionParticles(level, entity);
            }
        }
    }
    
    /** 生成爆炸冲击粒子 */
    private static void spawnExplosionParticles(Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double dx = (level.random.nextDouble() - 0.5) * 1.0;
                double dy = level.random.nextDouble() * 1.0;
                double dz = (level.random.nextDouble() - 0.5) * 1.0;
                
                // 使用火焰粒子作为爆炸特效
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        entity.getX() + dx,
                        entity.getY() + 0.5 + dy,
                        entity.getZ() + dz,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                );
            }
        }
    }
    
    /** 检查实体是否面向传送门 */
    private static boolean isFacingPortal(LivingEntity entity, BlockPos portalPos, Direction portalFacing) {
        // 计算实体朝向与传送门方向的夹角
        Vec3 entityLook = entity.getLookAngle().normalize();
        Vec3 portalDirection = Vec3.atLowerCornerOf(portalFacing.getNormal());
        
        // 计算点积，判断实体是否大致面向传送门
        double dotProduct = entityLook.dot(portalDirection);
        return dotProduct > 0.5; // 0.5是一个阈值，表示实体至少面向传送门的方向
    }
    
    /** 处理实体传送 */
    private static void handleEntityPortal(LivingEntity entity, BlockPos portalPos, ServerPlayer sp) {
        Level level = sp.level();
        
        // 将实体传送到武器模组中的赫尔海姆维度
        if (level instanceof ServerLevel serverLevel) {
            try {
                // 获取赫尔海姆维度
                ResourceLocation helheimLocation = new ResourceLocation("kamen_rider_weapon_craft", "helheim");
                ServerLevel helheimLevel = null;
                
                // 遍历所有已加载的维度，寻找赫尔海姆维度
                for (ServerLevel loadedLevel : serverLevel.getServer().getAllLevels()) {
                    if (loadedLevel.dimension().location().equals(helheimLocation)) {
                        helheimLevel = loadedLevel;
                        break;
                    }
                }
                
                if (helheimLevel != null) {
                    // 对于玩家实体，使用专门的teleportTo方法
                    if (entity instanceof net.minecraft.server.level.ServerPlayer) {
                        net.minecraft.server.level.ServerPlayer serverPlayer = (net.minecraft.server.level.ServerPlayer) entity;
                        
                        // 在赫尔海姆维度中随机选择一个安全的位置
                        double targetX = entity.getX() + (sp.getRandom().nextDouble() - 0.5) * 100;
                        double targetZ = entity.getZ() + (sp.getRandom().nextDouble() - 0.5) * 100;
                        int targetY = findSafeTeleportY(helheimLevel, (int)targetX, (int)targetZ);
                        
                        // 执行玩家传送
                        serverPlayer.teleportTo(helheimLevel, targetX, targetY, targetZ, entity.getYRot(), entity.getXRot());
                    } else {
                        // 对于非玩家实体（包括怪物），先检查是否能安全传送
                        if (entity.isAlive() && !entity.isRemoved()) {
                            try {
                                // 在赫尔海姆维度中随机选择一个安全的位置
                                double targetX = entity.getX() + (sp.getRandom().nextDouble() - 0.5) * 100;
                                double targetZ = entity.getZ() + (sp.getRandom().nextDouble() - 0.5) * 100;
                                int targetY = findSafeTeleportY(helheimLevel, (int)targetX, (int)targetZ);
                                
                                // 在目标维度创建新实体
                                if (entity instanceof net.minecraft.world.entity.LivingEntity) {
                                    net.minecraft.world.entity.LivingEntity livingEntity = (net.minecraft.world.entity.LivingEntity) entity;
                                    // 复制实体属性
                                    net.minecraft.world.entity.EntityType<?> type = livingEntity.getType();
                                    net.minecraft.world.entity.Entity newEntity = type.create(helheimLevel);
                                    if (newEntity != null) {
                                        // 设置位置和旋转
                                        newEntity.setPos(targetX, targetY, targetZ);
                                        newEntity.setYRot(entity.getYRot());
                                        newEntity.setXRot(entity.getXRot());
                                        
                                        // 复制生命值等属性
                                        if (newEntity instanceof net.minecraft.world.entity.LivingEntity) {
                                            ((net.minecraft.world.entity.LivingEntity)newEntity).setHealth(livingEntity.getHealth());
                                        }
                                        
                                        // 确保实体在新维度中生成
                                        helheimLevel.addFreshEntity(newEntity);
                                    }
                                }
                                
                                // 移除原维度中的实体
                                entity.remove(net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION);
                            } catch (Exception e) {
                                // 发生错误时提供更详细的信息
                                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("传送实体时发生错误：" + e.getMessage()));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 如果获取赫尔海姆维度失败，提供更详细的错误信息
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("无法找到赫尔海姆维度：" + e.getMessage()));
            }
        }
    }
    
    /** 专门处理物品传送的方法 */
    private static void handleItemPortal(ItemEntity item, BlockPos pos) {
        if (item.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            ResourceLocation helheimLocation = new ResourceLocation("kamen_rider_weapon_craft", "helheim");
            ServerLevel helheimLevel = null;
            
            // 遍历所有已加载的维度，寻找赫尔海姆维度
            for (ServerLevel loadedLevel : server.getAllLevels()) {
                if (loadedLevel.dimension().location().equals(helheimLocation)) {
                    helheimLevel = loadedLevel;
                    break;
                }
            }
            
            if (helheimLevel != null) {
                // 在赫尔海姆维度中随机选择一个安全的位置
                double targetX = pos.getX() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 10;
                double targetZ = pos.getZ() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 10;
                int targetY = findSafeTeleportY(helheimLevel, (int)targetX, (int)targetZ);
                
                // 创建新的物品实体到目标维度
                ItemEntity newItem = new ItemEntity(
                        helheimLevel,
                        targetX,
                        targetY,
                        targetZ,
                        item.getItem()
                );
                newItem.setDeltaMovement(item.getDeltaMovement());
                helheimLevel.addFreshEntity(newItem);
                
                // 移除原物品
                item.discard();
            }
        }
    }
    
    /** 为物品实体生成吸入粒子效果 */
    private static void spawnItemSuckParticles(Level level, ItemEntity item) {
        // 生成粒子效果（与原spawnSuckParticles方法类似，但专门为物品实体设计）
        for (int i = 0; i < 5; i++) {
            double dx = item.getX() + (level.random.nextDouble() - 0.5) * 1.0;
            double dy = item.getY() + 0.5 + level.random.nextDouble() * 1.0;
            double dz = item.getZ() + (level.random.nextDouble() - 0.5) * 1.0;
            
            // 使用末地粒子作为吸入特效
            level.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    dx,
                    dy,
                    dz,
                    0.0,
                    -0.1,
                    0.0
            );
        }
    }
    
    /** 在目标维度中找到一个安全的传送Y坐标 */
    private static int findSafeTeleportY(ServerLevel level, int x, int z) {
        // 尝试在地面附近寻找一个安全的位置
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
        
        // 确保不会传送到虚空或过高的地方
        if (y < 10) y = 60; // 至少在Y=60以上
        if (y > 200) y = 200; // 最多在Y=200以下
        
        return y;
    }
    
    /** 生成裂缝粒子效果 */
    private static void spawnCrackParticles(Level level, double x, double y, double z) {
        // 烟雾粒子
        for (int j = 0; j < 20; j++) {
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    x + (level.random.nextDouble() - 0.5) * 1.5,
                    y + level.random.nextDouble() * 1.5,
                    z + (level.random.nextDouble() - 0.5) * 1.5,
                    0, 0.05, 0
            );
        }
        
        // 末地粒子作为裂缝特效
        for (int j = 0; j < 15; j++) {
            level.addParticle(
                    ParticleTypes.PORTAL,
                    x + (level.random.nextDouble() - 0.5) * 1.5,
                    y + level.random.nextDouble() * 1.5,
                    z + (level.random.nextDouble() - 0.5) * 1.5,
                    0, 0.05, 0
            );
        }
    }
    
    /** 生成吸入粒子效果 */
    private static void spawnSuckParticles(Level level, LivingEntity entity) {
        for (int i = 0; i < 10; i++) {
            double dx = entity.getX() + (level.random.nextDouble() - 0.5) * 1.0;
            double dy = entity.getY() + 0.5 + level.random.nextDouble() * 1.0;
            double dz = entity.getZ() + (level.random.nextDouble() - 0.5) * 1.0;
            
            // 使用末地粒子作为吸入特效
            level.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    dx,
                    dy,
                    dz,
                    0.0,
                    -0.1,
                    0.0
            );
        }
    }
    
    /* ====== 工具方法 ====== */
    
    /** 检查并移除过期的踢力增强效果 */
    private static void checkAndRemoveKickEnhance(ServerPlayer sp) {
        if (sp.getPersistentData().getBoolean("DarkGaimKickEnhance")) {
            long currentTime = sp.level().getGameTime();
            long expiryTime = sp.getPersistentData().getLong("DarkGaimKickEnhanceExpiry");
            
            if (currentTime >= expiryTime) {
                sp.getPersistentData().putBoolean("DarkGaimKickEnhance", false);
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("踢力增强效果已结束"));
            }
        }
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