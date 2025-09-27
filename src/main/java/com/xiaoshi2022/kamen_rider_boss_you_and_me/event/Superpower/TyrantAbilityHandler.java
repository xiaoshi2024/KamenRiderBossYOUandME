package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TyrantAbilityHandler {

    /* ---------------- 常驻被动 ---------------- */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player) && player.isAlive()) {
            int duration = 100; // 5秒

            // 只在没有效果时添加生命提升
            if (!player.hasEffect(MobEffects.HEALTH_BOOST)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.HEALTH_BOOST,
                        duration, 0, false, false));

                // 立即恢复生命值，但不超过最大生命值
                player.heal(8.0f);
            }

            // 其他效果，只有当没有效果或效果等级低于我们提供的等级时才添加
            addEffectIfBetterOrAbsent(player, MobEffects.DAMAGE_RESISTANCE, duration, 0);
            addEffectIfBetterOrAbsent(player, MobEffects.FIRE_RESISTANCE, duration, 0);
            addEffectIfBetterOrAbsent(player, MobEffects.REGENERATION, duration, 0);
        } else {
            // 当玩家不再穿着暴君盔甲时，移除生命提升效果以确保生命值恢复正常
            if (player.hasEffect(MobEffects.HEALTH_BOOST) && player.isAlive()) {
                player.removeEffect(MobEffects.HEALTH_BOOST);
                // 确保生命值不会超过新的最大生命值
                player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
            }
            // 保留其他通过原版药水获得的效果，不再直接移除
        }
    }

    // 辅助方法：只在玩家没有特定效果或现有效果等级低于我们提供的等级时才添加效果
    private static void addEffectIfBetterOrAbsent(Player player, MobEffect effect, int duration, int amplifier) {
        if (!player.hasEffect(effect) || player.getEffect(effect).getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
        }
    }

    /* ---------------- 伤害反弹 ---------------- */

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player)) {
            // 反弹10%伤害给攻击者
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                attacker.hurt(player.damageSources().generic(), event.getAmount() * 0.1f);
            }
        }
    }

    /* ---------------- 生命偷取 ---------------- */

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player) && event.getEntity() instanceof LivingEntity && player.isAlive()) {
            LivingEntity target = (LivingEntity) event.getEntity();
            float healAmount = target.getMaxHealth() * 0.01f;
            player.heal(healAmount);
        }
    }

    /* ---------------- 工具方法 ---------------- */

    private static boolean isWearingTyrant(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.TYRANT_HELMET.get() ||
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.TYRANT_CHESTPLATE.get() ||
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.TYRANT_LEGGINGS.get();
    }

}