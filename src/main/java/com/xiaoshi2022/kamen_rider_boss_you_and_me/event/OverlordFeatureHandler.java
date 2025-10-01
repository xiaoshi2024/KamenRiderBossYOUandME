package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class OverlordFeatureHandler {
    // 基础生命值（普通人类）
    private static final double BASE_HEALTH = 20.0D;
    // Overlord额外生命值
    private static final double OVERLORD_EXTRA_HEALTH = 100.0D;
    // Overlord攻击力倍数
    private static final int ATTACK_DAMAGE_MULTIPLIER = 12;
    // 赫尔海姆藤蔓生成范围
    private static final int VINE_SPAWN_RANGE = 3;
    // 藤蔓方块存在时间（毫秒）
    private static final long VINE_LIFETIME = 10000; // 10秒
    // 藤蔓技能冷却时间（tick）- 可配置的冷却时间
    private static final int VINE_SKILL_COOLDOWN = 200; // 约10秒
    private static final int VINE_SKILL_COOLDOWN_HUMAN = 400; // 人类形态下40秒冷却
    // 存储玩家的藤蔓技能冷却时间
    private static final Map<UUID, Integer> vineCooldowns = new HashMap<>();

    // 每秒检查玩家状态并应用Overlord特性
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level();
        
        // 只在服务器端和玩家活着的时候执行
        if (level.isClientSide() || player.isDeadOrDying()) {
            return;
        }
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
        
        // 检查玩家是否为Overlord且未变身
        boolean isOverlord = variables.isOverlord;
        boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);
        
        // 根据状态调整生命值上限
        if (isOverlord && !isTransformed) {
            // 应用夜视效果
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0, true, false));
            
            // 调整生命值上限（只在需要时调整）
            if (player.getMaxHealth() <= BASE_HEALTH) {
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(BASE_HEALTH + OVERLORD_EXTRA_HEALTH);
                // 如果当前生命值低于新的最大值，恢复一些生命值
                if (player.getHealth() < player.getMaxHealth()) {
                    player.setHealth(Math.min(player.getHealth() + 20.0F, player.getMaxHealth()));
                }
            }
            
            // 更新冷却时间
            updateCooldowns();
        } else {
            // 如果不是Overlord或已变身，恢复默认生命值上限
            if (player.getMaxHealth() > BASE_HEALTH && variables.baseMaxHealth <= BASE_HEALTH) {
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(BASE_HEALTH);
                // 确保生命值不会超过新的最大值
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth((float) player.getMaxHealth());
                }
            }
        }
    }

    // 更新技能冷却时间
    private static void updateCooldowns() {
        List<UUID> toRemove = new java.util.ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : vineCooldowns.entrySet()) {
            int remainingCooldown = entry.getValue() - 1;
            if (remainingCooldown <= 0) {
                toRemove.add(entry.getKey());
            } else {
                entry.setValue(remainingCooldown);
            }
        }
        for (UUID uuid : toRemove) {
            vineCooldowns.remove(uuid);
        }
    }
    
    // 检查玩家是否可以使用藤蔓技能
    private static boolean canUseVineSkill(Player player) {
        return !vineCooldowns.containsKey(player.getUUID());
    }
    
    // 激活藤蔓技能并设置冷却时间
    public static void activateVineSkill(Player player) {
        if (!player.level().isClientSide() && canUseVineSkill(player)) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean isOverlord = variables.isOverlord;
            boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);
            boolean isBaron = false;
            boolean isBaronLemon = false;
            
            // 检查是否是巴隆形态
            if (player instanceof ServerPlayer serverPlayer) {
                isBaron = isBaronArmorEquipped(serverPlayer);
                isBaronLemon = isBaronLemonArmorEquipped(serverPlayer);
            }
            
            if (isOverlord) {  // 只要是Overlord就可以使用，无论是否变身
                // 根据形态设置不同的冷却时间
                int cooldown = VINE_SKILL_COOLDOWN; // 默认冷却时间
                if (!isTransformed && !isBaron && !isBaronLemon) {
                    cooldown = VINE_SKILL_COOLDOWN_HUMAN; // 人类形态下冷却更长
                }
                
                // 生成藤蔓
                spawnHelheimVines(player);
                // 设置冷却时间
                vineCooldowns.put(player.getUUID(), cooldown);
                // 通知玩家技能已激活
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("赫尔海姆藤蔓已激活！"), true);
            } else {
                // 通知玩家无法使用此技能
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("只有Overlord可以使用此技能！"), true);
            }
        } else if (!canUseVineSkill(player)) {
            // 通知玩家技能冷却中
            int cooldownSeconds = vineCooldowns.get(player.getUUID()) / 20;
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("技能冷却中，剩余 " + cooldownSeconds + " 秒！"), true);
        }
    }
    
    // 定期清理过期的藤蔓方块
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // 只在结束阶段执行
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        // 每100个tick清理一次（约5秒）
        if (event.getServer().overworld().getGameTime() % 100 != 0) {
            return;
        }
        
        // 遍历所有玩家
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            
            // 清理所有Overlord玩家（无论是否变身）的藤蔓
            if (variables.isOverlord) {
                cleanupOldVines(player);
            }
        }
    }

    // 增强Overlord的攻击力
    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event) {
        Entity source = event.getSource().getEntity();
        // 检查攻击者是否为玩家且是Overlord且未变身
        if (source instanceof Player player) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean isOverlord = variables.isOverlord;
            boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);
            
            if (isOverlord && !isTransformed) {
                // 记录原始伤害值，我们将在HurtEvent中应用倍数
                event.getEntity().getPersistentData().putDouble("OriginalDamage", event.getAmount());
            }
        }
    }

    // 处理伤害计算（增强攻击力和减免伤害）
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        
        // 处理Overlord的攻击增强
        if (source.getEntity() instanceof Player attacker) {
            KRBVariables.PlayerVariables attackerVars = attacker.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean attackerIsOverlord = attackerVars.isOverlord;
            boolean attackerIsTransformed = PlayerDeathHandler.hasTransformationArmor(attacker);
            
            if (attackerIsOverlord && !attackerIsTransformed) {
                // 应用12倍攻击力
                event.setAmount(event.getAmount() * ATTACK_DAMAGE_MULTIPLIER);
            }
        }
        
        // 处理Overlord的伤害减免
        if (target instanceof Player player) {
            KRBVariables.PlayerVariables playerVars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean isOverlord = playerVars.isOverlord;
            boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);
            
            if (isOverlord && !isTransformed) {
                // 物理伤害减免（减少50%物理伤害）
                if (source.getMsgId().equals("player") || source.getMsgId().equals("mob")) {
                    event.setAmount(event.getAmount() * 0.5F);
                }
                
                // 硬抗一次音速弓伤害（这里简化处理为对远程伤害额外减免）
                if (source.isIndirect()) {
                    event.setAmount(event.getAmount() * 0.3F); // 远程伤害减免70%
                }
            }
        }
    }

    // 生成赫尔海姆藤蔓（使用植物方块代替实体）
    private static void spawnHelheimVines(Player player) {
        Level level = player.level();
        BlockPos targetPos = null;
        
        // 尝试找到玩家视线内最近的敌方生物
        LivingEntity targetEntity = findNearestHostileEntity(player);
        
        // 如果找到目标实体，使用目标实体的位置；否则使用玩家位置
        if (targetEntity != null) {
            targetPos = targetEntity.blockPosition();
        } else {
            targetPos = player.blockPosition();
        }
        
        // 在目标周围生成一圈藤蔓（环形）
        generateVineCircle(level, targetPos, player);
    }
    
    // 检测玩家是否穿着基础巴隆盔甲
    private static boolean isBaronArmorEquipped(ServerPlayer player) {
        // 检查胸甲是否为基础巴隆盔甲
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
    }
    
    // 检测玩家是否穿着巴隆柠檬形态盔甲
    private static boolean isBaronLemonArmorEquipped(ServerPlayer player) {
        // 检查是否穿着巴隆柠檬形态的全套盔甲
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
    }
    
    // 找到玩家视线内最近的敌方生物
    private static LivingEntity findNearestHostileEntity(Player player) {
        Level level = player.level();
        AABB searchArea = new AABB(player.blockPosition()).inflate(10.0D); // 搜索玩家周围10格内的实体
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
        
        LivingEntity nearestHostile = null;
        double nearestDistance = Double.MAX_VALUE;
        
        // 首先尝试找到玩家正在注视的实体
        LivingEntity lookingAtEntity = getEntityPlayerIsLookingAt(player);
        if (lookingAtEntity != null && isHostileEntity(lookingAtEntity, player)) {
            return lookingAtEntity;
        }
        
        // 如果玩家没有注视任何实体或注视的不是敌对实体，则查找最近的敌对实体
        for (LivingEntity entity : entities) {
            if (isHostileEntity(entity, player)) {
                double distance = player.distanceToSqr(entity);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestHostile = entity;
                }
            }
        }
        
        return nearestHostile;
    }
    
    // 检查实体是否是敌对实体（排除玩家自己、其他玩家和Overlord生物）
    private static boolean isHostileEntity(LivingEntity entity, Player player) {
        if (entity == player || entity instanceof Player || entity.getPersistentData().getBoolean("IsOverlord")) {
            return false;
        }
        
        // 检查是否为敌对生物
        return !entity.getType().getCategory().isFriendly() && entity.canChangeDimensions() && player.canAttack(entity);
    }
    
    // 获取玩家正在注视的实体
    private static LivingEntity getEntityPlayerIsLookingAt(Player player) {
        double reach = 10.0D;
        HitResult result = player.pick(reach, 0.0F, false);
        
        if (result.getType() == HitResult.Type.ENTITY) {
            // 在较新版本的Minecraft中，使用这种方式获取实体
            if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof LivingEntity) {
                    return (LivingEntity) entity;
                }
            }
        }
        
        return null;
    }
    
    // 在目标位置周围生成一圈藤蔓（荆棘墙效果）
    private static void generateVineCircle(Level level, BlockPos centerPos, Player player) {
        // 荆棘墙生成参数
        int minRadius = 2; // 最小半径
        int maxRadius = 3; // 最大半径
        int pointsCount = 12; // 环形上的点数量，越多越密集
        boolean generateMultiLayer = true; // 是否生成多层藤蔓
        
        // 主藤蔓墙（外环）
        for (int r = minRadius; r <= maxRadius; r += generateMultiLayer ? 1 : (maxRadius - minRadius)) {
            // 在环形上生成藤蔓点
            for (int i = 0; i < pointsCount; i++) {
                // 计算环形上的点坐标
                double angle = 2 * Math.PI * i / pointsCount;
                int x = centerPos.getX() + (int) Math.round(r * Math.cos(angle));
                int z = centerPos.getZ() + (int) Math.round(r * Math.sin(angle));
                
                // 寻找合适的放置位置（从地面开始向上搜索）
                for (int yOffset = -2; yOffset <= 1; yOffset++) {
                    int y = centerPos.getY() + yOffset;
                    BlockPos spawnPos = new BlockPos(x, y, z);
                    
                    // 检查位置是否可生成效果
                    if (level.getBlockState(spawnPos).isSolidRender(level, spawnPos) && 
                        level.isEmptyBlock(spawnPos.above())) {
                        
                        // 放置藤蔓方块
                        BlockPos vinePos = spawnPos.above();
                        if (level.isEmptyBlock(vinePos)) {
                            try {
                                BlockState vineBlock = ModBlocks.HELHEIMVINE.get().defaultBlockState();
                                level.setBlock(vinePos, vineBlock, 3);
                                
                                // 获取方块实体并设置标记数据
                                BlockEntity blockEntity = level.getBlockEntity(vinePos);
                                if (blockEntity != null) {
                                    CompoundTag tag = blockEntity.getPersistentData();
                                    tag.putString("OwnerUUID", player.getUUID().toString());
                                    tag.putLong("SpawnTime", System.currentTimeMillis());
                                }
                            } catch (Exception e) {
                                // 如果无法放置藤蔓方块，忽略错误
                            }
                        }
                        
                        // 对附近的敌人施加中毒效果
                        applyPoisonToNearbyEnemies(level, vinePos);
                        
                        // 每个角度只在一个合适的高度放置藤蔓
                        break;
                    }
                }
            }
        }
        
        // 在目标位置中心上方也放置一个藤蔓作为标记
        BlockPos centerVinePos = centerPos.above(1);
        if (level.isEmptyBlock(centerVinePos)) {
            try {
                BlockState vineBlock = ModBlocks.HELHEIMVINE.get().defaultBlockState();
                level.setBlock(centerVinePos, vineBlock, 3);
                
                // 获取方块实体并设置标记数据
                BlockEntity blockEntity = level.getBlockEntity(centerVinePos);
                if (blockEntity != null) {
                    CompoundTag tag = blockEntity.getPersistentData();
                    tag.putString("OwnerUUID", player.getUUID().toString());
                    tag.putLong("SpawnTime", System.currentTimeMillis());
                }
            } catch (Exception e) {
                // 如果无法放置藤蔓方块，忽略错误
            }
        }
    }

    // 清理过期的藤蔓方块
    private static void cleanupOldVines(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        long currentTime = System.currentTimeMillis();
        
        // 搜索玩家周围30格范围内的方块
        AABB searchArea = new AABB(playerPos).inflate(30);
        
        // 遍历搜索区域内的所有方块（简化版，实际应用中可能需要更高效的方法）
        BlockPos.betweenClosedStream(searchArea).forEach(blockPos -> {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                CompoundTag tag = blockEntity.getPersistentData();
                if (tag.contains("OwnerUUID") && tag.getString("OwnerUUID").equals(player.getUUID().toString()) && 
                    tag.contains("SpawnTime")) {
                    
                    long spawnTime = tag.getLong("SpawnTime");
                    if (currentTime - spawnTime > VINE_LIFETIME) {
                        // 方块已过期，移除它
                        level.removeBlock(blockPos, false);
                    }
                }
            }
        });
    }

    // 对藤蔓周围的敌人施加中毒效果
    private static void applyPoisonToNearbyEnemies(Level level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(2.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        
        for (LivingEntity entity : entities) {
            // 不影响玩家和已经是Overlord的生物
            if (!(entity instanceof Player) && !entity.getPersistentData().getBoolean("IsOverlord")) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            }
        }
    }
}