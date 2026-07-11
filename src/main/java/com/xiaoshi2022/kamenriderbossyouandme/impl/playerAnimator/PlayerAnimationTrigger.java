package com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


@OnlyIn(Dist.CLIENT)
public class PlayerAnimationTrigger {
    /**
     * 在任何需要的地方触发动画
     *
     * @param player       目标玩家实体
     * @param animationId  动画资源ID (如 "waving")
     * @param fadeDuration 淡入淡出时间（tick）
     * @param easeType     缓动函数类型（如 Ease.LINEAR, Ease.QUAD_IN_OUT）
     */
    public static void playAnimation(
            @NotNull AbstractClientPlayer player,
            String animationId,
            int fadeDuration,
            Ease easeType) {

        ResourceLocation layerId =
                ResourceLocation.fromNamespaceAndPath(
                        KamenRiderBossYOUandME.MODID,
                        "animation"
                );

        var animationData = PlayerAnimationAccess.getPlayerAssociatedData(player);

        IAnimation rawLayer = animationData.get(layerId);
        if (!(rawLayer instanceof ModifierLayer<?> modifierLayer)) {
            return;
        }

        @SuppressWarnings("unchecked")
        ModifierLayer<IAnimation> animationLayer =
                (ModifierLayer<IAnimation>) modifierLayer;

        // 3. 获取动画资源（你原来的逻辑，不动）
        var animEntry = PlayerAnimationRegistry.getAnimation(
                ResourceLocation.fromNamespaceAndPath(
                        KamenRiderBossYOUandME.MODID,
                        animationId
                )
        );
        if (animEntry == null) {
            return;
        }

        IAnimation animationResource = animEntry.playAnimation();

        // 4. 创建淡入修饰器
        AbstractFadeModifier fadeModifier =
                AbstractFadeModifier.standardFadeIn(fadeDuration, easeType);

        // 5. 应用动画
        animationLayer.replaceAnimationWithFade(fadeModifier, animationResource);
    }

    public static void playAnimation(AbstractClientPlayer player, String animationId, int fadeDuration) {
        playAnimation(player, animationId, fadeDuration, Ease.LINEAR);
    }
}
