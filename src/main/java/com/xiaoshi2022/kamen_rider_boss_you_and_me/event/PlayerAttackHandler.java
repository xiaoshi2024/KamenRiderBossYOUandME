package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
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
        if (event.phase == TickEvent.Phase.END && !pendingAttacks.isEmpty()) {
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
    }
}