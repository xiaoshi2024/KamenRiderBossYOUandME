package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DukeShieldDamageHandler {

    // 处理实体伤害事件，应用Duke能量护盾减免
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只处理玩家伤害
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 检查是否装备Duke胸甲
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof Duke duke)) {
            return;
        }
        
        // 检查能量护盾是否激活
        if (duke.isEnergyShieldActive()) {
            // 应用50%伤害减免
            float reduction = duke.getShieldDamageReduction();
            float newDamage = event.getAmount() * (1.0F - reduction);
            event.setAmount(newDamage);
            
            // 播放护盾受到攻击的音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,
                    player.getSoundSource(), 0.5F, 1.0F);
        }
    }
}