package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CherrySigurdAbilityHandler {

    /* ---------------- 常驻被动 ---------------- */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingCherrySigurd(player)) {
            // ① 速度 III（高速移动，比ZangetsuShin更快）
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    40, 2, false, false));

            // ② 跳跃提升 II
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP,
                    40, 1, false, false));

            // ③ 急迫 III（快速攻击）
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SPEED,
                    40, 2, false, false));
        }
    }

    /* ---------------- 伤害减免 ---------------- */

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingCherrySigurd(player)) {
            // 受到的伤害降低30%
            event.setAmount(event.getAmount() * 0.7f);
        }
    }

    /* ---------------- 工具方法 ---------------- */

    private static boolean isWearingCherrySigurd(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.SIGURD_HELMET.get() ||
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.SIGURD_CHESTPLATE.get() ||
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.SIGURD_LEGGINGS.get();
        }

}