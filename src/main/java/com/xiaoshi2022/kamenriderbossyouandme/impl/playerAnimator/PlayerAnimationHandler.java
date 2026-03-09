package com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator;

import net.minecraft.client.player.AbstractClientPlayer;

public class PlayerAnimationHandler {
    public static void handleAnimation(AbstractClientPlayer player, String animationId, int fadeDuration) {
        PlayerAnimationTrigger.playAnimation(player, animationId, fadeDuration);
    }
}
