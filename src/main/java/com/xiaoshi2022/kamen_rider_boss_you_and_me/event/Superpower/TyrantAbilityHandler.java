package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
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

        if (isWearingTyrant(player)) {
            // 1. 增加最大生命值
            player.addEffect(new MobEffectInstance(
                    MobEffects.HEALTH_BOOST,
                    40, 3, false, false));

            // 2. 提升防御力
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    40, 2, false, false));

            // 3. 火焰抗性
            player.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE,
                    40, 0, false, false));
        }
    }

    /* ---------------- 伤害反弹 ---------------- */

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player)) {
            // 反弹20%伤害给攻击者
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                attacker.hurt(player.damageSources().generic(), event.getAmount() * 0.2f);
            }
        }
    }

    /* ---------------- 生命偷取 ---------------- */

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player) && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            float healAmount = target.getMaxHealth() * 0.03f;
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