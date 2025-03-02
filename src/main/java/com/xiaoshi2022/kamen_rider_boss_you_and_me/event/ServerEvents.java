//package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;
//
//import com.zigythebird.playeranimatorapi.API.PlayerAnimAPI;
//import net.minecraft.client.Minecraft;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.player.Player;
//import net.minecraftforge.client.event.InputEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//import static com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup.STORIOUS_VFX_HEIXIN;
//import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.CHANGE_KEY;
//
//@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
//public class ServerEvents {
//
//    @SubscribeEvent
//    public static void onKeyInput(InputEvent.Key event) {
//        if (Minecraft.getInstance().screen == null) {
//            if (CHANGE_KEY.isDown()) {
//                // 获取当前玩家
//                Player player = Minecraft.getInstance().player;
//                if (player != null && player.level() instanceof ServerLevel serverLevel) {
//                    // 直接在服务器端触发动画播放
//                    playAnimation(serverLevel, player, STORIOUS_VFX_HEIXIN);
//                }
//            }
//        }
//    }
//
//    public static void playAnimation(ServerLevel level, Player player, ResourceLocation animation) {
//        // 调用 PlayerAnimatorAPI 的 playPlayerAnim 方法
//        PlayerAnimAPI.playPlayerAnim(level, player, animation);
//    }
//}