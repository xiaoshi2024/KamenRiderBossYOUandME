package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.BlackBuild;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BerserkModeManager {
    
    // 静态初始化块，用于验证事件订阅是否成功
    static {
        System.out.println("BerserkModeManager initialized and event subscribed!");
    }
    
    private static final int BERSERK_DELAY_TICKS = 600; // 30秒 = 600 ticks
    private static final double ATTACK_RANGE = 3.5; // 攻击范围（玩家正常攻击范围）
    private static final int SEARCH_RANGE = 32; // 搜索范围
    
    private static final Map<UUID, BerserkState> BERSERK_STATES = new ConcurrentHashMap<>();
    
    public static class BerserkState {
        public int ticksUntilBerserk;
        public boolean isBerserk;
        public LivingEntity currentTarget;
        public Vec3 targetPosition;
        public int stuckTicks;
        
        public BerserkState(int delayTicks) {
            this.ticksUntilBerserk = delayTicks;
            this.isBerserk = false;
            this.currentTarget = null;
            this.targetPosition = null;
            this.stuckTicks = 0;
        }
    }
    
    public static void startBerserkMode(ServerPlayer player) {
        BERSERK_STATES.put(player.getUUID(), new BerserkState(BERSERK_DELAY_TICKS));
    }
    
    public static void startBerserkModeImmediately(ServerPlayer player) {
        BerserkState state = new BerserkState(0);
        state.isBerserk = true;
        BERSERK_STATES.put(player.getUUID(), state);
        
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("§c§l暴走模式已立即激活！"),
            true
        );
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL,
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
        
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkSyncPacket(true),
            player
        );
    }
    
    public static void stopBerserkMode(ServerPlayer player) {
        BERSERK_STATES.remove(player.getUUID());
        
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkSyncPacket(false),
            player
        );
    }
    
    public static boolean isBerserk(ServerPlayer player) {
        BerserkState state = BERSERK_STATES.get(player.getUUID());
        return state != null && state.isBerserk;
    }
    
    public static BerserkState getBerserkState(ServerPlayer player) {
        return BERSERK_STATES.get(player.getUUID());
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        
        ServerPlayer player = (ServerPlayer) event.player;
        UUID uuid = player.getUUID();
        
        // 检查玩家是否装备了全套BlackBuild盔甲
        boolean hasFullArmor = BlackBuild.isFullArmorEquipped(player);
        
        if (hasFullArmor) {
                BerserkState state = BERSERK_STATES.get(uuid);
                if (state == null) {
                    state = new BerserkState(BERSERK_DELAY_TICKS);
                    BERSERK_STATES.put(uuid, state);
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e§l开始30秒倒计时，准备进入暴走模式！"),
                        true
                    );
                }
                
                if (!state.isBerserk) {
                    // 每5秒显示一次倒计时，避免刷屏
                    if (state.ticksUntilBerserk % 100 == 0) {
                        player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§e§l倒计时中... " + state.ticksUntilBerserk / 20 + "秒"),
                            true
                        );
                    }
                    handleBerserkDelay(player, state);
                } else {
                    handleBerserkMode(player, state);
                }
            } else {
                stopBerserkMode(player);
            }
    }
    
    private static void handleBerserkDelay(ServerPlayer player, BerserkState state) {
        state.ticksUntilBerserk--;
        
        if (state.ticksUntilBerserk <= 0) {
            state.isBerserk = true;
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§c§l暴走模式已激活！"),
                true
            );
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
            
            // 发送暴走状态同步包到客户端
            com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
                new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkSyncPacket(true),
                player
            );
            
            // 禁用玩家控制
            disablePlayerControl(player);
        }
        
        // 将更新后的state对象保存回BERSERK_STATES中
        BERSERK_STATES.put(player.getUUID(), state);
    }
    
    private static void handleBerserkMode(ServerPlayer player, BerserkState state) {
        if (!player.isAlive()) {
            stopBerserkMode(player);
            return;
        }
        
        // 如果玩家在水中，解除暴走
        if (player.isInWater()) {
            stopBerserkMode(player);
            return;
        }
        
        disablePlayerControl(player);
        updateBerserkAI(player, state);
    }
    
    private static void updateBerserkAI(ServerPlayer player, BerserkState state) {
        LivingEntity target = state.currentTarget;
        
        // 检查目标是否有效
        boolean isTargetValid = target != null && target.isAlive() && player.distanceToSqr(target) <= SEARCH_RANGE * SEARCH_RANGE;
        
        // 如果目标无效，或者目标在视线外且距离较远，重新寻找目标
        if (!isTargetValid || (player.distanceTo(target) > ATTACK_RANGE * 2 && !player.hasLineOfSight(target))) {
            state.currentTarget = findNearestTarget(player);
            target = state.currentTarget;
            state.stuckTicks = 0;
        }
        
        if (target != null) {
            double distance = player.distanceTo(target);
            
            if (distance <= ATTACK_RANGE && player.hasLineOfSight(target)) {
                attackTarget(player, target);
            } else {
                moveToTarget(player, target, state);
            }
        } else {
            exploreRandomly(player, state);
        }
    }
    
    private static boolean isValidTarget(LivingEntity entity, ServerPlayer player) {
        return entity != null && 
               entity.isAlive() && 
               entity != player && 
               !entity.isSpectator() && 
               Math.abs(entity.getY() - player.getY()) < 15.0 && 
               (entity.level().getBlockState(entity.blockPosition()).canOcclude() || entity.onGround());
    }
    
    private static LivingEntity findNearestTarget(ServerPlayer player) {
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(SEARCH_RANGE),
            entity -> isValidTarget(entity, player)
        );
        
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : nearbyEntities) {
            double distance = player.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = entity;
            }
        }
        
        return nearestTarget;
    }
    
    private static void attackTarget(ServerPlayer player, LivingEntity target) {
        Vec3 lookVec = player.getEyePosition(1.0F).vectorTo(target.getEyePosition(1.0F)).normalize();
        
        float yaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float pitch = (float) Math.toDegrees(-Math.asin(lookVec.y));
        
        player.setYRot(yaw);
        player.setXRot(pitch);
        
        if (player.hasLineOfSight(target)) {
            // 检查玩家是否持有允许的武器
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();
            
            if (isAllowedItem(mainHandItem)) {
                useAllowedItem(player, mainHandItem, target);
            } else if (isAllowedItem(offHandItem)) {
                useAllowedItem(player, offHandItem, target);
            } else {
                // 没有允许的武器，使用默认攻击
                player.attack(target);
                
                player.resetAttackStrengthTicker();
                
                Vec3 knockback = lookVec.scale(0.5);
                target.push(knockback.x, 0.2, knockback.z);
                
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_KNOCKBACK, 
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
                
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
                    new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkAttackPacket(),
                    player
                );
            }
        }
    }
    
    private static void useAllowedItem(ServerPlayer player, ItemStack stack, LivingEntity target) {
        Item item = stack.getItem();
        
        if (item instanceof BowItem) {
            // 检查弓的耐久度
            if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                // 弓的耐久度不足，使用默认攻击
                useDefaultAttack(player, target);
                return;
            }
            
            // 检查玩家是否有箭
            boolean hasArrows = false;
            for (ItemStack invStack : player.getInventory().items) {
                if (invStack.getItem() instanceof net.minecraft.world.item.ArrowItem) {
                    hasArrows = true;
                    break;
                }
            }
            
            if (!hasArrows) {
                // 没有箭，使用默认攻击
                useDefaultAttack(player, target);
                return;
            }
            
            // 模拟使用弓
            player.startUsingItem(player.getUsedItemHand());
            // 直接射箭，不需要等待拉弓
            player.stopUsingItem();
            
            // 创建箭
            net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(player.level(), player);
            arrow.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            
            // 设置箭的方向
            Vec3 lookVec = player.getLookAngle();
            arrow.setDeltaMovement(lookVec.x * 3.0, lookVec.y * 3.0, lookVec.z * 3.0);
            arrow.setCritArrow(true);
            
            // 发射箭
            player.level().addFreshEntity(arrow);
            
            // 播放射箭声音
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                net.minecraft.sounds.SoundEvents.ARROW_SHOOT, 
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F / (player.level().getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
            
            // 消耗弓的耐久
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            
            // 消耗玩家的箭
            for (ItemStack invStack : player.getInventory().items) {
                if (invStack.getItem() instanceof net.minecraft.world.item.ArrowItem) {
                    invStack.shrink(1);
                    break;
                }
            }
        } else if (item == Items.ENDER_PEARL) {
            // 模拟使用末影珍珠
            net.minecraft.world.entity.projectile.ThrownEnderpearl enderpearl = new net.minecraft.world.entity.projectile.ThrownEnderpearl(player.level(), player);
            enderpearl.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            
            // 设置末影珍珠的方向
            Vec3 lookVec = player.getLookAngle();
            enderpearl.setDeltaMovement(lookVec.x * 1.5, lookVec.y * 1.5, lookVec.z * 1.5);
            
            // 发射末影珍珠
            player.level().addFreshEntity(enderpearl);
            
            // 播放末影珍珠声音
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW, 
                net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 0.4F / (player.level().getRandom().nextFloat() * 0.4F + 0.8F));
            
            // 消耗末影珍珠
            stack.shrink(1);
        } else {
            // 其他武器，使用默认攻击
            useDefaultAttack(player, target);
        }
    }
    
    private static void useDefaultAttack(ServerPlayer player, LivingEntity target) {
        player.attack(target);
        
        player.resetAttackStrengthTicker();
        
        Vec3 lookVec = player.getEyePosition(1.0F).vectorTo(target.getEyePosition(1.0F)).normalize();
        Vec3 knockback = lookVec.scale(0.5);
        target.push(knockback.x, 0.2, knockback.z);
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_KNOCKBACK, 
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
            
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkAttackPacket(),
            player
        );
    }
    
    private static Vec3 findNearestLand(ServerPlayer player) {
        int searchRadius = 16;
        Vec3 playerPos = player.position();
        Vec3 nearestLand = null;
        double nearestDistance = Double.MAX_VALUE;
        
        // 在玩家周围搜索陆地
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    double posX = playerPos.x + x;
                    double posY = playerPos.y + y;
                    double posZ = playerPos.z + z;
                    net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos((int) posX, (int) posY, (int) posZ);
                    
                    // 检查方块是否是固体且可以站立
                    if (player.level().getBlockState(blockPos).canOcclude()) {
                        // 检查上方是否有足够的空间
                        net.minecraft.core.BlockPos abovePos = blockPos.above();
                        if (player.level().isEmptyBlock(abovePos) && player.level().isEmptyBlock(abovePos.above())) {
                            double distance = playerPos.distanceToSqr(posX, posY, posZ);
                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearestLand = new Vec3(posX + 0.5, posY + 1, posZ + 0.5); // 站在方块上方
                            }
                        }
                    }
                }
            }
        }
        
        return nearestLand;
    }
    
    private static void moveToLand(ServerPlayer player, Vec3 landPosition, BerserkState state) {
        // 计算移动方向
        Vec3 direction = landPosition.subtract(player.position()).normalize();
        
        // 设置玩家朝向
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(-Math.asin(direction.y));
        player.setYRot(yaw);
        player.setXRot(pitch);
        
        // 检查是否接近陆地表面
        boolean shouldJump = false;
        double distanceToLand = player.position().distanceTo(landPosition);
        if (distanceToLand < 3.0 && player.isInWater()) {
            // 接近陆地，准备跳跃
            shouldJump = true;
        }
        
        // 在水中移动
        double speed = 0.2;
        double yVelocity = direction.y * speed + 0.05; // 稍微上浮
        
        if (shouldJump) {
            // 触发跳跃
            yVelocity = 0.5; // 跳跃力度
        }
        
        player.setDeltaMovement(
            direction.x * speed,
            yVelocity,
            direction.z * speed
        );
        
        // 发送移动数据包
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkMovementPacket(
                1.0F, 0.0F, shouldJump, yaw, pitch
            ),
            player
        );
        
        // 检查是否到达陆地
        if (player.position().distanceToSqr(landPosition) < 4.0) {
            state.stuckTicks = 0;
        } else {
            state.stuckTicks++;
        }
        
        // 更新目标位置
        state.targetPosition = landPosition;
    }
    
    private static void moveToTarget(ServerPlayer player, LivingEntity target, BerserkState state) {
        Vec3 targetPos = target.position();
        
        boolean jumping = false;
        
        Vec3 lookVec = player.getEyePosition(1.0F).vectorTo(target.getEyePosition(1.0F)).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float pitch = (float) Math.toDegrees(-Math.asin(lookVec.y));
        
        player.setYRot(yaw);
        player.setXRot(pitch);
        
        Vec3 direction = targetPos.subtract(player.position()).normalize();
        double speed = 0.15;
        
        player.setDeltaMovement(
            direction.x * speed,
            player.getDeltaMovement().y,
            direction.z * speed
        );
        
        if (state.stuckTicks > 60) {
            jumping = true;
            state.stuckTicks = 0;
        }
        
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkMovementPacket(
                1.0F, 0.0F, jumping, yaw, pitch
            ),
            player
        );
        
        if (state.targetPosition != null) {
            double distToTarget = player.position().distanceToSqr(state.targetPosition);
            if (distToTarget < 4.0) {
                state.stuckTicks++;
            } else {
                state.stuckTicks = 0;
            }
        }
        
        state.targetPosition = targetPos;
    }
    
    private static void exploreRandomly(ServerPlayer player, BerserkState state) {
        if (state.targetPosition == null || player.position().distanceToSqr(state.targetPosition) < 9.0) {
            Random random = new Random();
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 10 + random.nextDouble() * 20;
            
            state.targetPosition = new Vec3(
                player.getX() + Math.cos(angle) * distance,
                player.getY(),
                player.getZ() + Math.sin(angle) * distance
            );
        }
        
        moveToPosition(player, state.targetPosition, state);
    }
    
    private static void moveToPosition(ServerPlayer player, Vec3 targetPos, BerserkState state) {
        boolean jumping = false;
        
        Vec3 lookVec = player.getEyePosition(1.0F).vectorTo(targetPos);
        float yaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float pitch = (float) Math.toDegrees(-Math.asin(lookVec.y / lookVec.length()));
        
        player.setYRot(yaw);
        player.setXRot(pitch);
        
        Vec3 direction = targetPos.subtract(player.position()).normalize();
        double speed = 0.12;
        
        player.setDeltaMovement(
            direction.x * speed,
            player.getDeltaMovement().y,
            direction.z * speed
        );
        
        if (state.stuckTicks > 60) {
            jumping = true;
            state.stuckTicks = 0;
        }
        
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToClient(
            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkMovementPacket(
                1.0F, 0.0F, jumping, yaw, pitch
            ),
            player
        );
        
        double distToTarget = player.position().distanceToSqr(targetPos);
        if (distToTarget < 4.0) {
            state.stuckTicks++;
        } else {
            state.stuckTicks = 0;
        }
    }
    
    private static void performJump(ServerPlayer player, BerserkState state) {
        Vec3 currentPos = player.position();
        Vec3 jumpDirection = state.targetPosition.subtract(currentPos).normalize();
        
        player.setDeltaMovement(
            jumpDirection.x * 0.3,
            0.5,
            jumpDirection.z * 0.3
        );
    }
    
    private static boolean isAllowedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        Item item = stack.getItem();
        
        // 允许原版弓
        if (item instanceof BowItem) return true;
        
        // 允许末影珍珠
        if (item == Items.ENDER_PEARL) return true;
        
        // 允许武器mod的音速弓和平成剑
        String itemName = BuiltInRegistries.ITEM.getKey(item).toString();
        return itemName.contains("sonic_bow") || itemName.contains("heisei_sword");
    }
    
    private static boolean isPlayerUsingAllowedItem(ServerPlayer player) {
        return isAllowedItem(player.getMainHandItem()) || isAllowedItem(player.getOffhandItem());
    }
    
    private static void disablePlayerControl(ServerPlayer player) {
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.getAbilities().invulnerable = false;
        }
    }
}
