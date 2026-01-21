package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.RiderInvisibilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KamenBossArmorTickHandler {
    
    /**
     * 在玩家tick事件中调用所有KamenBossArmor物品的tick方法
     * 仅在服务器端执行，避免客户端与服务器端类转换问题
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 只在结束阶段执行
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        Player player = event.player;
        
        // 仅在服务器端执行tick方法，避免客户端-服务器端类转换问题
        if (player instanceof ServerPlayer) {
            // 检查并处理玩家装备的所有盔甲槽位
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack armorStack = player.getItemBySlot(slot);
                    if (!armorStack.isEmpty() && armorStack.getItem() instanceof KamenBossArmor) {
                        // 调用该盔甲的tick方法
                        ((KamenBossArmor) armorStack.getItem()).tick(player);
                    }
                }
            }
            
            // 在服务器端更新隐身效果，确保所有玩家看到一致的效果
            RiderInvisibilityManager.updateInvisibility(player);
        }
    }
}