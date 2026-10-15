package com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PlayerAnimationTrigger {

    /**
     * 用淡入的方式播放动画
     * @param player       目标玩家
     * @param animationId  动画资源路径（如 "waving"）
     * @param fadeDuration 淡入时间（tick）
     */
    public static void playAnimation(
            @NotNull AbstractClientPlayer player,
            String animationId,
            int fadeDuration) {

        ResourceLocation animationRL =
                ResourceLocation.fromNamespaceAndPath(
                        com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID,
                        animationId
                );

        PlayerAnimationController controller =
                (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                        player, PlayerAnimatorSetup.ANIMATION_LAYER_ID);

        if (controller == null) {
            return;
        }

        controller.replaceAnimationWithFade(
                AbstractFadeModifier.standardFadeIn(fadeDuration, EasingType.EASE_IN_OUT_SINE),
                animationRL);
    }

    /**
     * 无淡入的直接播放
     */
    public static void playAnimation(AbstractClientPlayer player, String animationId) {
        PlayerAnimationController controller =
                (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                        player, PlayerAnimatorSetup.ANIMATION_LAYER_ID);

        if (controller == null) {
            return;
        }

        controller.triggerAnimation(
                ResourceLocation.fromNamespaceAndPath(
                        com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID,
                        animationId
                ));
    }
}