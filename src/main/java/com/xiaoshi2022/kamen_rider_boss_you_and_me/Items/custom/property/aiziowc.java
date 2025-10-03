package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Another_zi_o_click.aiziowcRenderer;
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

import java.util.function.Consumer;

public class aiziowc extends Item implements GeoItem {
    private static final RawAnimation CLICK = RawAnimation.begin().thenPlay("click");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int MAX_DAMAGE = 64;   // 与原版工具耐久同概念

    public aiziowc(Properties props) {
        super(props.durability(MAX_DAMAGE));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getUseCount(ItemStack stack) {
        return stack.getOrCreateTag().getInt("DiskUse");
    }
    public static void incrUseCount(ItemStack stack) {
        stack.getOrCreateTag().putInt("DiskUse", getUseCount(stack) + 1);
    }

    public static void setUseCount(ItemStack disk, int i) {
        disk.getOrCreateTag().putInt("DiskUse", i);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private aiziowcRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new aiziowcRenderer();

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
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "click", "click");

                // 播放音效
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.ANOTHER_ZI_O_CLICK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

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
        controllers.add(new AnimationController<>(this, "click", 20, state -> PlayState.STOP)
                .triggerableAnim("click", CLICK)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
                    Player player = ClientUtils.getClientPlayer();

                    if (player != null)
                        player.playSound(ModBossSounds.ANOTHER_ZI_O_CLICK.get(), 1, 1);
                }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

