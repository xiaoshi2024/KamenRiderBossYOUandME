package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.KivatBatTwoNdm.KivatBatTwoItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class KivatBatTwoNdItem extends Item implements GeoItem {

    /* ---------- 动画定义 ---------- */
    private static final RawAnimation SAY   = RawAnimation.begin().thenPlay("say");
    private static final RawAnimation FLY   = RawAnimation.begin().thenPlay("fly");
    private static final RawAnimation SLEEP = RawAnimation.begin().thenPlay("sleep");
    private static final RawAnimation HOVER = RawAnimation.begin().thenPlay("hover");
    private static final RawAnimation SNAP  = RawAnimation.begin().thenPlay("snap");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KivatBatTwoNdItem(Properties properties) {
        super(properties);
        /* 注册同步，让动画能在客户端之间同步播放 */
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private KivatBatTwoItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new KivatBatTwoItemRenderer();

                return this.renderer;
            }
        });
    }
    
    /* ---------- 右键切换动画 ---------- */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int state = stack.getOrCreateTag().getInt("animationState");
            state = (state + 1) % 5;          // 0-4 循环
            stack.getOrCreateTag().putInt("animationState", state);
        }
        return InteractionResultHolder.success(stack);
    }

    /* ---------- 动画控制器 ---------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<KivatBatTwoNdItem> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null) return PlayState.STOP;

        int mode = stack.getOrCreateTag().getInt("animationState");
        switch (mode) {
            case 0 -> state.setAndContinue(SAY);
            case 1 -> state.setAndContinue(FLY);
            case 2 -> state.setAndContinue(SLEEP);
            case 3 -> state.setAndContinue(HOVER);
            case 4 -> state.setAndContinue(SNAP);
            default -> { return PlayState.STOP; }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}