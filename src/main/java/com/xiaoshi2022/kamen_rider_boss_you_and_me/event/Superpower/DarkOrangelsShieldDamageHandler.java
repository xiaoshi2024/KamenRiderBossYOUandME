package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DarkOrangelsShieldDamageHandler {

    // 处理实体伤害事件，应用DarkOrangels黑暗护盾减免
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只处理玩家伤害
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 检查是否装备DarkOrangels胸甲
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof Dark_orangels darkOrangels)) {
            return;
        }
        
        // 检查黑暗护盾是否激活
        if (darkOrangels.isDarkShieldActive()) {
            // 应用伤害减免
            float reduction = darkOrangels.getShieldDamageReduction();
            float newDamage = event.getAmount() * (1.0F - reduction);
            event.setAmount(newDamage);
            
            // 播放护盾受到攻击的音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,
                    player.getSoundSource(), 0.5F, 0.8F);
        }
    }
}