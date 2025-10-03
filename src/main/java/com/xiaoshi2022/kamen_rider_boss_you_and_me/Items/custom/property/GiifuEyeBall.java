package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.GiifuEyeBall.GiifuEyeBallRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;


public class GiifuEyeBall extends Item implements GeoItem {

    public static final int SOUND_COOLDOWN = 5 * 20; // 5秒冷却

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public GiifuEyeBall(Properties properties) {
        super(properties);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GiifuEyeBallRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GiifuEyeBallRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            // 触发idle动画
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "idle", "idle");
        }

        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 20, state -> {
            state.setAnimation(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}