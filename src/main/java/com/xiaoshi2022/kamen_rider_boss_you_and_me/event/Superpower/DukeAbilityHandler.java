package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DukeAbilityHandler {

    // 处理玩家tick事件，更新Duke能力状态
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        
        // 只在服务器端处理
        if (player.level().isClientSide) {
            return;
        }
        
        // 检查是否装备全套Duke盔甲
        if (Duke.isFullArmorEquipped((ServerPlayer) player)) {
            // 获取胸甲实例（假设胸甲是GeoItem并实现了能力）
            ItemStack chestplate = player.getInventory().armor.get(2);
            if (chestplate.getItem() instanceof Duke duke) {
                // 更新能力状态
                duke.tick(player);
                
                // 检测右键点击以激活能量剑
                if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                    duke.energySwordAttack(player);
                }
                
                // 检测Shift+右键以激活能量护盾
                if (player.isShiftKeyDown() && player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                    duke.activateEnergyShield(player);
                }
            }
        }
    }


}