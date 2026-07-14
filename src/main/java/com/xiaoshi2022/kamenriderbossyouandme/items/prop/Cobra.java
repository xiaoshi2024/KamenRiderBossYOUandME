package com.xiaoshi2022.kamenriderbossyouandme.items.prop;

import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.cobra.CobraRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class Cobra extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Cobra(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("idle", IDLE)
                .triggerableAnim("open", OPEN));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof Cobra))
            return PlayState.STOP;

        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private CobraRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<Cobra> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new CobraRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        InteractionHand offHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack offHandStack = player.getItemInHand(offHand);

        if (offHandStack.getItem() instanceof GreatDragon greatDragon) {
            if (greatDragon.getMode(offHandStack) == GreatDragon.Mode.EMPTY) {
                greatDragon.setMode(offHandStack, GreatDragon.Mode.NORMAL);
                greatDragon.triggerShowsAnim(player, player.getId());

                player.setItemInHand(hand, ItemStack.EMPTY);

                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        }

        this.triggerAnim(player, player.getId(), "controller", "open");

        return InteractionResultHolder.success(stack);
    }
}