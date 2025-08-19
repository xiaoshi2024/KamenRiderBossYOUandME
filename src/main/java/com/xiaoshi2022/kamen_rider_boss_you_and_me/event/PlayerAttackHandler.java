package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PlayerAttackHandler {
    private static final Map<UUID, AttackData> pendingAttacks = new HashMap<>();

    /* ======== 香蕉加成 ======== */
    private static final float BANANA_JUMP_LEVEL   = 3.0f;   // 跳跃Ⅲ
    private static final float BANANA_FALL_IMMUNE  = 0.0f;   // 免疫摔伤
    private static final float BANANA_LANDING_AOE  = 3.0f;   // 落地冲击波半径
    private static final float BANANA_LANDING_DMG  = 4.0f;   // 冲击波伤害

    // 基础加成配置
    private static final float BARE_HAND_BONUS = 3.0f;
    private static final float WEAPON_BASE_BONUS = 4.0f;
    private static final float LEMON_EXTRA_BONUS = 1.0f;
    private static final float LEMON_BOW_BONUS = 3.0f;
    private static final float LEMON_SPEED_LEVEL = 2.0f; // 速度效果等级

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

            // 蜜瓜夜视
            if (chestArmor.getItem() instanceof ZangetsuShinItem) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        220,           // 11 秒，持续刷新就不会闪
                        0,             // 夜视 I
                        false,         // 不显示图标
                        false,         // 不显示粒子
                        true));        // 图标/粒子可选
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