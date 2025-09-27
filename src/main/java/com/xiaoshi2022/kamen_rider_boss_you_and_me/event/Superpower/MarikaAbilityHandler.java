package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class MarikaAbilityHandler {
    private static final Random random = new Random();
    private static final int HEAL_INTERVAL = 100; // 5秒 = 100tick
    private static final int MIN_HEALTH_FOR_HEAL = 1; // 最低生命值阈值

    /**
     * 检查玩家是否装备了全套Marika装甲
     */
    private static boolean isFullMarikaArmorEquipped(ServerPlayer player) {
        return Marika.isArmorEquipped(player, ModItems.MARIKA_HELMET.get()) &&
               Marika.isArmorEquipped(player, ModItems.MARIKA_CHESTPLATE.get()) &&
               Marika.isArmorEquipped(player, ModItems.MARIKA_LEGGINGS.get());
    }

    /**
     * 每tick检查玩家状态，触发桃子治愈能力
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && event.phase == TickEvent.Phase.END) {
            // 检查是否装备全套Marika装甲且玩家存活
            if (isFullMarikaArmorEquipped(player) && player.isAlive()) {
                // 每HEAL_INTERVAL tick触发一次治愈
                if (player.tickCount % HEAL_INTERVAL == 0) {
                    // 只在生命值未满时触发
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.heal(1.0F); // 恢复1点生命值
                    }
                }
            }
        }
    }

    /**
     * 当玩家受到伤害时，有几率触发额外治愈效果
     */
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查是否装备全套Marika装甲且玩家存活
            if (isFullMarikaArmorEquipped(player) && player.isAlive()) {
                // 20%几率触发
                if (random.nextDouble() < 0.2) {
                    // 恢复3点生命值
                    player.heal(3.0F);
                    // 给予3秒的生命恢复I效果，但只有当玩家没有生命恢复效果或效果等级低于I时才添加
                    if (!player.hasEffect(MobEffects.REGENERATION) || player.getEffect(MobEffects.REGENERATION).getAmplifier() < 0) {
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));
                    }
                }
            }
        }
    }
}