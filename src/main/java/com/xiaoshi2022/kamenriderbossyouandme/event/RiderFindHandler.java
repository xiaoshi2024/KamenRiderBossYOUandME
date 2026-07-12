//package com.xiaoshi2022.kamenriderbossyouandme.event;
//
//import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
//import com.jpigeon.ridebattlelib.common.config.RiderConfig;
//import com.jpigeon.ridebattlelib.common.event.FindRiderConfigEvent;
//import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
//import com.xiaoshi2022.kamenriderbossyouandme.riders.driver.BrainConfig;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.player.Player;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import top.theillusivec4.curios.api.CuriosApi;
//
//@EventBusSubscriber(modid = "kamenriderbossyouandme")
//public class RiderFindHandler {
//    private static final Logger LOGGER = LoggerFactory.getLogger(RiderFindHandler.class);
//
//    @SubscribeEvent
//    public static void onFindRiderConfig(FindRiderConfigEvent event) {
//        // 如果已经有配置了，跳过
//        if (event.getConfig() != null) {
//            return;
//        }
//
//        Player player = event.getPlayer();
//        if (player == null) {
//            return;
//        }
//
//        // 检查 Curios 槽位
//        var curiosInventory = CuriosApi.getCuriosInventory(player);
//        if (curiosInventory.isPresent()) {
//            var result = curiosInventory.get().findFirstCurio(ModItems.BRAIN_DRIVER.get());
//            if (result.isPresent()) {
//                // ✅ 直接设置配置，这样 findActiveDriverConfig 就会立即返回
//                event.setConfig(BrainConfig.BRAIN);
//                return;
//            }
//        }
//
//        // 检查原版槽位（备用）
//        var driverStack = player.getItemBySlot(EquipmentSlot.LEGS);
//        if (driverStack.is(ModItems.BRAIN_DRIVER.get())) {
//            event.setConfig(BrainConfig.BRAIN);
//        }
//    }
//}