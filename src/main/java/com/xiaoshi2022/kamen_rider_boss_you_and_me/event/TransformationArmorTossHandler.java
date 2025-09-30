package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class TransformationArmorTossHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    
    @SubscribeEvent
    public static void onTransformationArmorToss(ItemTossEvent event) {
        // 只在服务端执行
        if (event.getEntity().level().isClientSide) {
            return;
        }
        
        ItemEntity itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItem();
        Player player = event.getPlayer();
        
        // 检查是否是变身盔甲
        if (ClearArmorWhenRemovedHandler.isTransformationArmor(stack)) {
            LOGGER.info("检测到变身盔甲被丢弃，将其移除");
            // 取消原掉落事件
            event.setCanceled(true);
            // 移除物品实体
            itemEntity.discard();
            
            // 向玩家显示消息
            if (player != null) {
                player.displayClientMessage(Component.literal("变身盔甲无法丢弃！"), true);
            }
        }
    }
}