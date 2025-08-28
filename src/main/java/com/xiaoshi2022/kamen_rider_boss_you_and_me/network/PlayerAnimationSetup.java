package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerAnimationSetup {

    public static void clientInit() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"),
                1000,
                PlayerAnimationSetup::registerPlayerAnimations
        );
    }

    /**
     * 返回 ModifierLayer 实例，而不是裸接口 IAnimation
     */
    private static ModifierLayer<IAnimation> registerPlayerAnimations(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }

    @OnlyIn(Dist.CLIENT)
    public static void playAnimation(AbstractClientPlayer player, String animationName, boolean override) {
        var animationData = PlayerAnimationAccess.getPlayerAssociatedData(player)
                .get(new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"));

        // 现在可以直接强制转换，不再警告
        if (animationData instanceof ModifierLayer<?> layer) {
            @SuppressWarnings("unchecked")
            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) layer;

            if (override || !animationLayer.isActive()) {
                var animation = PlayerAnimationRegistry.getAnimation(
                        new ResourceLocation("kamen_rider_boss_you_and_me", animationName));
                if (animation != null) {
                    animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
                }
            }
        }
    }
}