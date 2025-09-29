package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.DarkKivaSequence;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkKivaSealBarrierEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class PlayerAttackHandler {
    private static final Map<UUID, AttackData> pendingAttacks = new HashMap<>();
    private static final Random RANDOM = new Random();
    
    // 残血触发概率 (50%)
    private static final float LOW_HEALTH_MESSAGE_CHANCE = 0.5f;
    // 残血判定阈值 (3点生命值)
    private static final float LOW_HEALTH_THRESHOLD = 3.0f;

    /* ======== 香蕉加成 ======== */
    private static final float BANANA_JUMP_LEVEL   = 3.0f;   // 跳跃Ⅲ
    private static final float BANANA_FALL_IMMUNE  = 0.0f;   // 免疫摔伤
    private static final float BANANA_LANDING_AOE  = 3.0f;   // 落地冲击波半径
    private static final float BANANA_LANDING_DMG  = 4.0f;   // 冲击波伤害

    // 基础加成配置
    private static final float BARE_HAND_BONUS = 3.0f;          // 基础空手加成
    private static final float WEAPON_BASE_BONUS = 4.0f;        // 武器基础加成
    private static final float LEMON_EXTRA_BONUS = 2.0f;        // 柠檬额外加成（从1.0提高到2.0）
    private static final float LEMON_BOW_BONUS = 3.0f;          // 柠檬弓箭加成
    private static final float LEMON_SPEED_LEVEL = 2.0f;        // 速度效果等级
    private static final float DARK_KIVA_BONUS = 5.0f;          // 黑暗月骑额外加成
    private static final float DUKE_BONUS = 3.5f;               // 公爵额外加成
    private static final float DARK_GAIM_BONUS = 4.0f;          // 黑暗铠武额外加成

    private static class AttackData {
        public final UUID attackerId;
        public final float bonusDamage;
        public final boolean isWeaponAttack;

        public AttackData(UUID attackerId, float bonusDamage, boolean isWeaponAttack) {
            this.attackerId = attackerId;
            this.bonusDamage = bonusDamage;
            this.isWeaponAttack = isWeaponAttack;
        }
    }

    // 速度强化 - 使用原版速度效果
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);

            // 蜜瓜夜视：只有当玩家没有原版夜视效果，或原版效果即将结束时才添加
            if (chestArmor.getItem() instanceof ZangetsuShinItem) {
                if (!player.hasEffect(MobEffects.NIGHT_VISION) || 
                    (player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 260 && 
                    player.getEffect(MobEffects.NIGHT_VISION).getDuration() != Integer.MAX_VALUE)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            320,           // 16 秒，延长持续时间避免闪烁
                            0,             // 夜视 I
                            false,         // 不显示图标
                            false,         // 不显示粒子
                            true));        // 图标/粒子可选
                }
            }
            // 香蕉高跳 + 免摔
            if (chestArmor.getItem() instanceof rider_baronsItem) {
                String tag = chestArmor.getOrCreateTag().getString("BaronMode");
                if ("BANANA".equals(tag)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.JUMP,
                            40, (int) BANANA_JUMP_LEVEL - 1, false, false));

                    // 免摔（用抗性提升模拟，也可直接取消摔伤）
                    if (player.fallDistance > 3.0f) {
                        player.fallDistance = 0.0f;       // 完全免摔
                        // 落地 AoE 在 onLivingTick 做
                    }
                }
            }
            if (chestArmor.getItem() instanceof baron_lemonItem) {
                // 给予速度II效果（40%移动速度加成）
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        40,  // 持续2秒（持续刷新）
                        (int)LEMON_SPEED_LEVEL - 1,  // 速度II（等级1表示II级效果）
                        false,
                        false,
                        true)); // 显示粒子效果
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        
        // 检查黑暗Kiva攻击残血玩家的情况
        if (attacker instanceof Player darkKivaPlayer && target instanceof Player victimPlayer) {
            checkAndSendDarkKivaMessage(darkKivaPlayer, victimPlayer, event.getAmount());
        }
        
        // 检查黑暗Kiva玩家攻击被封印结界控制的实体
        if (attacker instanceof ServerPlayer serverPlayer && target instanceof LivingEntity) {
            if (DarkKivaItem.isFullArmorEquipped(serverPlayer)) {
                // 查找世界中所有由当前玩家创建的活跃封印结界实体
                for (DarkKivaSealBarrierEntity barrier : serverPlayer.level().getEntitiesOfClass(
                        DarkKivaSealBarrierEntity.class, 
                        new AABB(
                            serverPlayer.level().getWorldBorder().getMinX(),
                            -64, // 下界高度
                            serverPlayer.level().getWorldBorder().getMinZ(),
                            serverPlayer.level().getWorldBorder().getMaxX(),
                            320, // 世界最大高度
                            serverPlayer.level().getWorldBorder().getMaxZ()
                        ), // 搜索整个世界范围
                        barrierEntity -> barrierEntity.getOwner() == serverPlayer && barrierEntity.isActive())) {
                    
                    // 检查被攻击的实体是否被该封印结界控制
                    if (barrier.isEntityTrapped(target)) {
                        // 直接将实体传送回封印结界位置
                        Vec3 sealPos = barrier.position();
                        target.teleportTo(sealPos.x, sealPos.y + 0.5, sealPos.z);
                        
                        // 添加短暂的效果提示
                        serverPlayer.displayClientMessage(Component.literal("将敌人拉回封印位置！"), true);
                        break;
                    }
                }
            }
        }
        
        if (attacker instanceof Player player) {
            ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
            boolean isBaronLemon = chestArmor.getItem() instanceof baron_lemonItem;

            if (chestArmor.getItem() instanceof rider_baronsItem || isBaronLemon) {
                ItemStack handItem = player.getMainHandItem();
                boolean isWeapon = !handItem.isEmpty() && isWeapon(handItem);
                boolean isBow = handItem.getItem() instanceof BowItem;

                float bonus = 0f;

                if (isBow && isBaronLemon) {
                    bonus = LEMON_BOW_BONUS;
                } else if (isWeapon) {
                    bonus = WEAPON_BASE_BONUS + getWeaponSpecificBonus(handItem);
                    if (isBaronLemon) {
                        bonus += LEMON_EXTRA_BONUS;
                    }
                } else {
                    bonus = BARE_HAND_BONUS;
                    if (isBaronLemon) {
                        bonus += LEMON_EXTRA_BONUS;
                    }
                }

                if (bonus > 0) {
                    pendingAttacks.put(
                            event.getEntity().getUUID(),
                            new AttackData(player.getUUID(), bonus, isWeapon || isBow)
                    );
                }
                // 柠檬吸血
                if (isBaronLemon) {
                    float heal = bonus * 0.3f;  // 30% 伤害值转生命
                    player.heal(heal);
                }
            }
        }
    }

    /**
     * 检查并发送黑暗Kiva攻击残血玩家的自定义消息，同时处理解除变身和转移蝙蝠主人
     */
    private static void checkAndSendDarkKivaMessage(Player attacker, Player victim, float damageAmount) {
        // 检查攻击者是否穿着全套黑暗Kiva盔甲
        boolean isDarkKiva = false;
        if (attacker instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            isDarkKiva = DarkKivaItem.isFullArmorEquipped(serverPlayer);
        }
        
        // 检查受害者是否处于残血状态（伤害后血量可能低于阈值）
        float remainingHealth = victim.getHealth() - damageAmount;
        boolean isLowHealth = remainingHealth <= LOW_HEALTH_THRESHOLD && remainingHealth > 0;
        
        // 触发条件：攻击者是黑暗Kiva，受害者是残血玩家，且随机概率通过
        if (isDarkKiva && isLowHealth && RANDOM.nextFloat() < LOW_HEALTH_MESSAGE_CHANCE) {
            // 发送自定义消息给攻击者
            attacker.sendSystemMessage(Component.literal("小丑蝙蝠：你在欺负弱小的人类吗？真是邪恶的骑士啊！"));
            
            // 也发送消息给受害者
            victim.sendSystemMessage(Component.literal("小丑蝙蝠：这个骑士太邪恶了，让我来帮助你！"));
            
            // 如果是服务端，处理解除变身和转移蝙蝠主人
            if (attacker instanceof ServerPlayer serverAttacker && victim instanceof ServerPlayer serverVictim) {
                // 1. 解除攻击者的变身
                DarkKivaSequence.startDisassembly(serverAttacker);
                
                // 2. 延迟处理蝙蝠转移，确保解除变身完成
                serverAttacker.getServer().tell(new net.minecraft.server.TickTask(
                        serverAttacker.getServer().getTickCount() + 20, // 1秒延迟
                        () -> transferKivatToVictim(serverAttacker, serverVictim)
                ));
            }
        }
    }

    /**
     * 将黑暗Kiva的蝙蝠从攻击者转移给受害者，并强制受害者变身
     */
    private static void transferKivatToVictim(ServerPlayer attacker, ServerPlayer victim) {
        // 在攻击者周围查找Kivat蝙蝠
        ServerLevel level = attacker.serverLevel();
        List<KivatBatTwoNd> bats = level.getEntitiesOfClass(KivatBatTwoNd.class,
                new AABB(attacker.blockPosition()).inflate(10),
                bat -> bat.isTame() && bat.isOwnedBy(attacker));
        
        if (!bats.isEmpty()) {
            KivatBatTwoNd bat = bats.get(0); // 获取第一只找到的蝙蝠
            
            // 1. 改变蝙蝠的主人
            bat.tame(victim);
            
            // 2. 设置蝙蝠飞向受害者并触发变身
            bat.temptToPlayer(victim);
            bat.pendingTransformPlayer = victim.getUUID();
            
            // 3. 给受害者一些提示消息
            victim.sendSystemMessage(Component.literal("小丑蝙蝠：现在你获得了黑暗的力量！"));
            
            // 4. 生成一些视觉效果
            spawnEffectParticles(level, victim);
        }
    }

    /**
     * 生成特效粒子
     */
    private static void spawnEffectParticles(ServerLevel level, Player player) {
        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleTypes.FLASH,
                    player.getX() + (level.random.nextDouble() - 0.5) * 2,
                    player.getY() + 1 + level.random.nextDouble(),
                    player.getZ() + (level.random.nextDouble() - 0.5) * 2,
                    1, 0, 0.1, 0, 0.1
            );
        }
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof TridentItem;
    }

    private static float getWeaponSpecificBonus(ItemStack weapon) {
        if (weapon.getItem() instanceof SwordItem) {
            return 2.0f;
        } else if (weapon.getItem() instanceof AxeItem) {
            return 3.0f;
        }
        return 0.0f;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 1. 处理 pendingAttacks（保持原逻辑）
        if (!pendingAttacks.isEmpty()) {
            Map<UUID, AttackData> processingMap = new HashMap<>(pendingAttacks);
            pendingAttacks.clear();
            processingMap.forEach((entityId, attackData) -> {
                Entity entity = event.getServer().overworld().getEntity(entityId);
                if (entity instanceof LivingEntity target && target.isAlive()) {
                    Entity attacker = event.getServer().overworld().getEntity(attackData.attackerId);
                    if (attacker instanceof Player player) {
                        DamageSource damageSource = target.damageSources().playerAttack(player);
                        target.hurt(damageSource, attackData.bonusDamage);
                        target.knockback(0.5f,
                                player.getX() - target.getX(),
                                player.getZ() - target.getZ());
                    }
                }
            });
        }

        // 2. 香蕉落地 AoE：遍历所有玩家
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level().isClientSide()) continue;
            if (!player.onGround() || player.fallDistance <= 3.0f) continue;

            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(chest.getItem() instanceof rider_baronsItem)) continue;
            if (!"BANANA".equals(chest.getOrCreateTag().getString("BaronMode"))) continue;

            // 造成伤害 & 击退
            player.level().getEntities(
                    player,
                    player.getBoundingBox().inflate(BANANA_LANDING_AOE)
            ).forEach(e -> {
                if (e instanceof LivingEntity target && target != player) {
                    target.hurt(target.damageSources().playerAttack(player), BANANA_LANDING_DMG);
                    target.knockback(0.8f,
                            player.getX() - target.getX(),
                            player.getZ() - target.getZ());
                }
            });
            player.fallDistance = 0;   // 防止重复触发
        }
    }
}