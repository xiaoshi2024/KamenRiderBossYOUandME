package com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID, value = Dist.CLIENT)
public class PlayerAnimatorSetup {

    public static final ResourceLocation ANIMATION_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animation");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    ANIMATION_LAYER_ID,
                    1000,
                    player -> new PlayerAnimationController(player,
                            (controller, state, animSetter) -> PlayState.STOP
                    )
            );
        });
    }
}