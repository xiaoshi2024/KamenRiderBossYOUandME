package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.BlackBuild;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BerserkModeManager {
    
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
        
        BerserkState state = BERSERK_STATES.get(uuid);
        
        if (state == null) {
            return;
        }
        
        if (!BlackBuild.isFullArmorEquipped(player)) {
            stopBerserkMode(player);
            return;
        }
        
        if (!state.isBerserk) {
            handleBerserkDelay(player, state);
        } else {
            handleBerserkMode(player, state);
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
        }
    }
    
    private static void handleBerserkMode(ServerPlayer player, BerserkState state) {
        if (!player.isAlive()) {
            stopBerserkMode(player);
            return;
        }
        
        disablePlayerControl(player);
        updateBerserkAI(player, state);
    }
    
    private static void updateBerserkAI(ServerPlayer player, BerserkState state) {
        LivingEntity target = state.currentTarget;
        
        if (target == null || !target.isAlive() || player.distanceToSqr(target) > SEARCH_RANGE * SEARCH_RANGE) {
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
               !entity.isSpectator();
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
    
    private static void disablePlayerControl(ServerPlayer player) {
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.getAbilities().invulnerable = false;
        }
    }
}
