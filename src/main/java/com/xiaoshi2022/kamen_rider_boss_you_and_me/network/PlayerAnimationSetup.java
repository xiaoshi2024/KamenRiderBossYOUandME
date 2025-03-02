package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.zigythebird.playeranimatorapi.API.PlayerAnimAPI;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerAnimationSetup {
    public static final ResourceLocation STORIOUS_VFX_HEIXIN = new ResourceLocation("kamen_rider_boss_you_and_me", "storiousvfxheixin");

    @OnlyIn(Dist.CLIENT)
    public static void clientInit() {
        // 注册动画工厂
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                new ResourceLocation("kamen_rider_boss_you_and_me", "player_animation"),
                1000,
                PlayerAnimationSetup::registerPlayerAnimations
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static IAnimation registerPlayerAnimations(AbstractClientPlayer player) {
        // 返回一个动画层
        return new ModifierLayer<>();
    }

    public static void playAnimation(ServerLevel level, Player player, ResourceLocation animation) {
        // 调用 PlayerAnimatorAPI 的 playPlayerAnim 方法
        PlayerAnimAPI.playPlayerAnim(level, player, animation);
    }
}
