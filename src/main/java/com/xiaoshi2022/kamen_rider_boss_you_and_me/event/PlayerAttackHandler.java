package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
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
    private static final float BARE_HAND_BONUS = 3.0f; // 空手加成
    private static final float WEAPON_BASE_BONUS = 4.0f; // 武器基础加成

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

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player) {
            ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);

            if (chestArmor.getItem() instanceof rider_baronsItem) {
                ItemStack handItem = player.getMainHandItem();
                boolean isWeapon = !handItem.isEmpty() && isWeapon(handItem);

                float bonus = isWeapon ?
                        WEAPON_BASE_BONUS + getWeaponSpecificBonus(handItem) :
                        BARE_HAND_BONUS;

                if (bonus > 0) {
                    pendingAttacks.put(
                            event.getEntity().getUUID(),
                            new AttackData(player.getUUID(), bonus, isWeapon)
                    );
                }
            }
        }
    }

    // 判断是否是武器
    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof TridentItem;
    }

    // 武器特定加成
    private static float getWeaponSpecificBonus(ItemStack weapon) {
        if (weapon.getItem() instanceof SwordItem) {
            return 2.0f; // 剑类额外+2
        } else if (weapon.getItem() instanceof AxeItem) {
            return 3.0f; // 斧类额外+3
        }
        return 0.0f;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !pendingAttacks.isEmpty()) {
            // 创建临时副本避免并发修改
            Map<UUID, AttackData> processingMap = new HashMap<>(pendingAttacks);
            pendingAttacks.clear();

            processingMap.forEach((entityId, attackData) -> {
                // 获取目标实体
                Entity entity = event.getServer().overworld().getEntity(entityId);
                if (entity instanceof LivingEntity target && target.isAlive()) {
                    // 获取攻击者玩家
                    Entity attacker = event.getServer().overworld().getEntity(attackData.attackerId);
                    if (attacker instanceof Player player) {
                        // 应用武器加成伤害
                        DamageSource damageSource = target.damageSources().playerAttack(player);
                        target.hurt(damageSource, attackData.bonusDamage);

                        // 添加击退效果（可选）
                        target.knockback(0.5f,
                                player.getX() - target.getX(),
                                player.getZ() - target.getZ());
                    }
                }
            });
        }
    }
}