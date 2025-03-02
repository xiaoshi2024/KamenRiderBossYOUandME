package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.StoriousMonsterBook.StoriousMonsterBookRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import vazkii.patchouli.api.PatchouliAPI;

import java.util.function.Consumer;

public class StoriousMonsterBook extends Item implements GeoItem {
    private static final RawAnimation idle = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation ZHANKAI = RawAnimation.begin().thenPlay("zhankai");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StoriousMonsterBook(Item.Properties p_41383_) {
        super(p_41383_);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }


    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private StoriousMonsterBookRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new StoriousMonsterBookRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);


//        // 确保物品有 NBT 标签
//        if (!stack.hasTag()) {
//            stack.setTag(new CompoundTag());
//        }
//        // 标记物品已打开
//        stack.getTag().putBoolean("opened", true);


        // 如果是服务器世界实例，触发动画
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "zhankai", "zhankai");
        }


        // 检查是否为服务器端，因为一些 GUI 操作需要在服务器端触发
        if (!level.isClientSide()) {
            PatchouliAPI.get().openBookGUI((ServerPlayer) player, new ResourceLocation("kamen_rider_boss_you_and_me:storiousmonsterbook"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "zhankai", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("zhankai", ZHANKAI)
                // 标记动画可由服务器触发
                .setSoundKeyframeHandler(state -> {
                }));
    }

    private PlayState idleAnimController(AnimationState<StoriousMonsterBook> StoriousMonsterBookAnimationState) {
        StoriousMonsterBookAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

