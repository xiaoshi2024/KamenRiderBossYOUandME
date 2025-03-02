//package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;
//
//import com.zigythebird.playeranimatorapi.data.PlayerAnimationData;
//import com.zigythebird.playeranimatorapi.playeranims.ConditionalAnimations;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//
//import static com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup.STORIOUS_VFX_HEIXIN;
//
//
//@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
//public class ClientEvents {
//
//    @SubscribeEvent
//    public static void onClientSetup(FMLClientSetupEvent event) {
//        // 注册条件动画
//        clientInit();
//    }
//
//    public static void clientInit() {
//        // 注册条件动画
//        ConditionalAnimations.addModConditions("kamen_rider_boss_you_and_me", ClientEvents::getAnimationForCurrentConditions);
//    }
//
//    public static ResourceLocation getAnimationForCurrentConditions(PlayerAnimationData data) {
//        AbstractClientPlayer player = (AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(data.playerUUID());
//        if (player == null) {
//            return data.animationID(); // 返回默认动画
//        }
//        // TODO: 添加条件判断
//        return STORIOUS_VFX_HEIXIN; // 返回条件动画
//    }
//}