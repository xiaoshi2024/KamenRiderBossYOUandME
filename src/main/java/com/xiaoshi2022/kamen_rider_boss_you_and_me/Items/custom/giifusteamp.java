package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.giifusteamp.giifusteampRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;


public class giifusteamp extends Item implements GeoItem {
    private static final RawAnimation idle = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public giifusteamp(Properties p_41383_) {
        super(p_41383_);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }


    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private giifusteampRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new giifusteampRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final int INTERVAL = 12 * 20; // 12秒间隔，1秒 = 20 tick
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
        long currentTime = level.getGameTime();

        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "extruding", "extruding");

                // 播放音效
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                // 更新玩家最后一次播放音效的时间
                player.getPersistentData().putLong("lastPlayedSound", currentTime);
            }
        } else {
            // 提示玩家冷却时间未结束
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }

        return super.use(level, player, hand);
    }


    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, this::idleAnimController));
    }

    private PlayState idleAnimController(AnimationState<giifusteamp> giifusteampAnimationState) {
        giifusteampAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
