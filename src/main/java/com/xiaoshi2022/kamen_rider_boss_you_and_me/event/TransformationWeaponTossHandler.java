//package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;
//
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
//import net.minecraft.world.entity.item.ItemEntity;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.event.entity.item.ItemTossEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
//public class TransformationWeaponTossHandler {
//    private static final Logger LOGGER = LogManager.getLogger();
//
//    @SubscribeEvent
//    public static void onTransformationWeaponToss(ItemTossEvent event) {
//        // 只在服务端执行
//        if (event.getEntity().level().isClientSide) {
//            return;
//        }
//
//        ItemEntity itemEntity = event.getEntity();
//        ItemStack stack = itemEntity.getItem();
//
//        // 添加调试日志
//        LOGGER.info("检测到物品丢弃事件: 物品ID={}, 有标签={}",
//                stack.getItem().getDescriptionId(),
//                stack.hasTag() ? "是" : "否");
//
//        // 检查是否是变身武器
//        if (TransformationWeaponManager.isTransformationWeapon(stack)) {
//            LOGGER.info("检测到变身武器被丢弃，将其移除");
//            // 取消原掉落事件
//            event.setCanceled(true);
//            // 移除物品实体
//            itemEntity.discard();
//        } else if (stack.hasTag()) {
//            LOGGER.info("物品有标签但不是变身武器，标签内容: {}", stack.getTag());
//        }
//    }
//}