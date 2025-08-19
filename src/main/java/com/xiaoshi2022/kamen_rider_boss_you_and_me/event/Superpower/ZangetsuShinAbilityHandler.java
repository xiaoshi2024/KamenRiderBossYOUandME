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
            // ① 速度 II（音速加速）
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    40, 1, false, false));

            // ② 力量 I（能量斩击）
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    40, 0, false, false));

            // ③ 跳跃提升 II（高机动）
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP,
                    40, 1, false, false));
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