package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
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
public class EvilBatsAbilityHandler {

    /* ---------------- 常驻被动 ---------------- */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingEvilBatsArmor(player)) {
            // ① 夜视（黑暗视觉）
            if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                    (player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 260 &&
                            player.getEffect(MobEffects.NIGHT_VISION).getDuration() != Integer.MAX_VALUE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        220, 0, false, false)); // 增加夜视效果的持续时间到 220 ticks (约 11 秒)
            }

            // ② 吸血（黑暗力量）
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                    40, 0, false, false));

            // ③ 速度提升（黑暗速度）
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    40, 0, false, false));
        } else {
            // 当玩家不再穿着盔甲时，移除盔甲提供的夜视效果
            if (player.getEffect(MobEffects.NIGHT_VISION) != null &&
                    player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 260 &&
                    player.getEffect(MobEffects.NIGHT_VISION).getDuration() != Integer.MAX_VALUE) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    /* ---------------- 受伤时触发的能力 ---------------- */

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingEvilBatsArmor(player)) {
            DamageSource src = event.getSource();
            if (src.isIndirect()) {          // 覆盖所有投射物
                event.setAmount(event.getAmount() * 0.5f); // 减少投射物伤害
                ((ServerPlayer) player).playNotifySound(
                        SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS, 1F, 1.5F);
            }
        }
    }

    /* ---------------- 工具方法 ---------------- */
    public static boolean isWearingEvilBatsArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof EvilBatsArmor ||
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof EvilBatsArmor ||
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof EvilBatsArmor ||
                player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof EvilBatsArmor;
    }
}