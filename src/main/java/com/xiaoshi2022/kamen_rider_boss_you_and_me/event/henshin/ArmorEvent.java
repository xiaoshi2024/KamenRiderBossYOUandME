//package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;
//
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import top.theillusivec4.curios.api.CuriosApi;
//import top.theillusivec4.curios.api.SlotResult;
//
//import java.util.Optional;
//
//@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
//public class ArmorEvent {
//    @SubscribeEvent
//    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
//        Player player = event.getEntity();
//        ItemStack stack = event.getOriginal();
//
//        // 检查玩家是否处于变身状态
//        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
//                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));
//
//        if (beltOptional.isPresent()) {
//            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltOptional.get().stack().getItem();
//            if (belt.isEquipped) {
//                // 如果玩家处于变身状态，阻止销毁变身盔甲
//                if (isTransformationArmor(stack)) {
//                    event.setCanceled(true);
//                }
//            }
//        }
//    }
//
//    private static boolean isTransformationArmor(ItemStack stack) {
//        // 检查是否是变身用的盔甲
//        return stack.getItem() == ModItems.RIDER_BARONS_HELMET.get() ||
//                stack.getItem() == ModItems.RIDER_BARONS_CHESTPLATE.get() ||
//                stack.getItem() == ModItems.RIDER_BARONS_LEGGINGS.get();
//    }
//}