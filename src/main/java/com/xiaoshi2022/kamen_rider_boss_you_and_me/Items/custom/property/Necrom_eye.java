package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Necrom_eye.Necrom_eyeRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
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
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;
import java.util.function.Consumer;


public class Necrom_eye extends Item implements GeoItem {

    public static final int SOUND_COOLDOWN = 5 * 20; // 5秒冷却

    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public Necrom_eye(Properties properties) {
        super(properties);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private Necrom_eyeRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new Necrom_eyeRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final int INTERVAL = 5 * 20; // 5秒间隔，1秒 = 20 tick
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
        long currentTime = level.getGameTime();

        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "start", "start");

                // 随机播放音效
                Random random = new Random();
                int randomIndex = random.nextInt(3); // 生成0到2的随机数
                switch (randomIndex) {
                    case 0:
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.STAND_BY_NECROM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        break;
                    case 1:
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.LOADING_EYE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        break;
                    case 2:
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.DESTROY_EYE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        break;
                }

                // 更新玩家最后一次播放音效的时间
                player.getPersistentData().putLong("lastPlayedSound", currentTime);
            }
        } else {
            // 提示玩家冷却时间未结束
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "start", 20, state -> PlayState.STOP)
                .triggerableAnim("start", START)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
                    Player player = ClientUtils.getClientPlayer();

                    if (player != null)
                        player.playSound(ModBossSounds.STAND_BY_NECROM.get(), 1, 1);
                }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}


