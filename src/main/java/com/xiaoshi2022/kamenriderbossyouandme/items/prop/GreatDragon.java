package com.xiaoshi2022.kamenriderbossyouandme.items.prop;

import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.greatdragon.GreatDragonRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

public class GreatDragon extends Item implements GeoItem {

    private static final RawAnimation CROSS = RawAnimation.begin().thenPlay("cross");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation SHOWS = RawAnimation.begin().thenPlay("shows");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final String TAG_MODE = "Mode";

    public enum Mode {
        NORMAL, EMPTY
    }

    public GreatDragon(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData == CustomData.EMPTY) {
            return new CompoundTag();
        }
        return customData.copyTag();
    }

    private static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public Mode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Mode.EMPTY;
        }
        CompoundTag tag = getOrCreateTag(stack);
        String modeName = tag.getString(TAG_MODE);
        if (modeName.isEmpty()) {
            return Mode.EMPTY;
        }
        try {
            return Mode.valueOf(modeName);
        } catch (IllegalArgumentException ex) {
            return Mode.EMPTY;
        }
    }

    public void setMode(ItemStack stack, Mode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = getOrCreateTag(stack);
        tag.putString(TAG_MODE, mode.name());
        saveTag(stack, tag);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("cross", CROSS)
                .triggerableAnim("open", OPEN)
                .triggerableAnim("shows", SHOWS));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof GreatDragon))
            return PlayState.STOP;

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GreatDragonRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<GreatDragon> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new GreatDragonRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        this.triggerAnim(player, player.getId(), "controller", "open");

        return InteractionResultHolder.success(stack);
    }

    public void triggerCrossAnim(Player player, int entityId) {
        this.triggerAnim(player, entityId, "controller", "cross");
    }

    public void triggerShowsAnim(Player player, int entityId) {
        this.triggerAnim(player, entityId, "controller", "shows");
    }
}