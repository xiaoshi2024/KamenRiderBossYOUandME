package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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

            // ② 吸血（黑暗力量） - 只在没有效果或效果等级低于我们提供的等级时添加，避免覆盖原版药水
            addEffectIfBetterOrAbsent(player, MobEffects.REGENERATION, 40, 0);

            // ③ 速度提升（黑暗速度） - 只在没有效果或效果等级低于我们提供的等级时添加，避免覆盖原版药水
            addEffectIfBetterOrAbsent(player, MobEffects.MOVEMENT_SPEED, 40, 0);
            
            // ④ 静谧性 - 降低声音和脚步声
            addEffectIfBetterOrAbsent(player, MobEffects.INVISIBILITY, 40, 0);
            
            // ⑤ 隐密行动 - 减少被生物察觉的范围
            addStealthEffect(player);
            
            // ⑥ 飞行能力 - 允许玩家飞行
            enableFlight(player);
        } else {
            // 当玩家不再穿着盔甲时，保留原版药水的效果
            // 我们不再直接移除效果，而是让它们自然到期
            disableFlightIfNotWearingArmor(player);
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
    
    // 启用飞行能力
    private static void enableFlight(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            
            // 检查是否已经有其他飞行模式在控制
            if (variables.currentFlightController == null || variables.currentFlightController.equals("EvilBats")) {
                // 设置飞行控制器为EvilBats
                variables.currentFlightController = "EvilBats";
                // 设置玩家可以飞行
                serverPlayer.getAbilities().mayfly = true;
                // 同步飞行状态到客户端
                serverPlayer.onUpdateAbilities();
                variables.syncPlayerVariables(serverPlayer);
            }
        }
    }
    
    // 当不穿盔甲时禁用飞行
    private static void disableFlightIfNotWearingArmor(Player player) {
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            
            // 只有当飞行控制器是EvilBats并且玩家不再穿着盔甲时，才禁用飞行
            if (!isWearingEvilBatsArmor(player) && variables.currentFlightController != null && variables.currentFlightController.equals("EvilBats")) {
                variables.currentFlightController = null;
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
                serverPlayer.onUpdateAbilities();
                variables.syncPlayerVariables(serverPlayer);
            }
        }
    }
    
    // 添加隐密行动效果（降低被怪物发现的几率和范围）
    private static void addStealthEffect(Player player) {
        // 在隐身效果的基础上，添加额外的隐密能力
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.isEvilBatsStealthed) {
            variables.isEvilBatsStealthed = true;
            variables.syncPlayerVariables(player);
        }
    }
    
    // 辅助方法：只在玩家没有特定效果或现有效果等级低于我们提供的等级时才添加效果
    private static void addEffectIfBetterOrAbsent(Player player, MobEffect effect, int duration, int amplifier) {
        if (!player.hasEffect(effect) || player.getEffect(effect).getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
        }
    }
}