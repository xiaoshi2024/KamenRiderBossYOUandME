package com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerAnimationHandler {
    public static void handleAnimation(Player player, String animationId, int fadeDuration) {
        if (player instanceof AbstractClientPlayer clientPlayer) PlayerAnimationTrigger.playAnimation(clientPlayer, animationId, fadeDuration);
    }
}
