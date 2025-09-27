package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ZangetsuShinAbilityHandler {

    /* ---------------- 常驻被动 ---------------- */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingZangetsuShin(player)) {
            // ① 速度 II（音速加速）：只有当玩家没有速度效果或效果等级低于II时才添加
            MobEffectInstance speedEffect = new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    40, 1, false, false);
            if (!player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() < 1) {
                player.addEffect(speedEffect);
            }

            // ② 力量 I（能量斩击）：只有当玩家没有力量效果或效果等级低于I时才添加
            MobEffectInstance strengthEffect = new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    40, 0, false, false);
            if (!player.hasEffect(MobEffects.DAMAGE_BOOST) || player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() < 0) {
                player.addEffect(strengthEffect);
            }

            // ③ 跳跃提升 II（高机动）：只有当玩家没有跳跃提升效果或效果等级低于II时才添加
            MobEffectInstance jumpEffect = new MobEffectInstance(
                    MobEffects.JUMP,
                    40, 1, false, false);
            if (!player.hasEffect(MobEffects.JUMP) || player.getEffect(MobEffects.JUMP).getAmplifier() < 1) {
                player.addEffect(jumpEffect);
            }
        }
    }

    /* ---------------- 投射物格挡 ---------------- */

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingZangetsuShin(player)) {
            DamageSource src = event.getSource();
            if (src.isIndirect()) {          // 覆盖所有投射物
                event.setAmount(event.getAmount() * 0.5f);
                ((ServerPlayer) player).playNotifySound(
                        SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS, 1F, 1.5F);
            }
        }
    }

    /* ---------------- 工具方法 ---------------- */
    private static boolean isWearingZangetsuShin(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ZangetsuShinItem ||
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ZangetsuShinItem ||
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ZangetsuShinItem ||
                player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ZangetsuShinItem;
    }
}