package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.property.RabbitItemRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class RabbitItem extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    private static final RawAnimation WRING = RawAnimation.begin().thenPlay("wring");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RabbitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("idle", IDLE)
                .triggerableAnim("wring", WRING));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof RabbitItem))
            return PlayState.STOP;

        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<RabbitItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new RabbitItemRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查玩家是否装备了BuildDriver腰带
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player, item -> item != null && item.getItem() instanceof BuildDriver);
        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

            // 检查当前模式是否可以插入兔子瓶
            // 只有在空模式或坦克模式时才能插入兔子瓶
            if (currentMode == BuildDriver.BeltMode.DEFAULT || currentMode == BuildDriver.BeltMode.T ||
                    currentMode == BuildDriver.BeltMode.HAZARD_EMPTY || currentMode == BuildDriver.BeltMode.HAZARD_T) {
                // 根据当前模式切换到对应的兔子模式
                BuildDriver.BeltMode newMode;
                if (currentMode == BuildDriver.BeltMode.DEFAULT) {
                    newMode = BuildDriver.BeltMode.R;
                } else if (currentMode == BuildDriver.BeltMode.T) {
                    newMode = BuildDriver.BeltMode.RT;
                } else if (currentMode == BuildDriver.BeltMode.HAZARD_EMPTY) {
                    newMode = BuildDriver.BeltMode.HAZARD_R;
                } else {
                    newMode = BuildDriver.BeltMode.HAZARD_RT;
                }

                belt.setMode(beltStack, newMode);

                // 播放兔子瓶音效
                player.playSound(ModBossSounds.RABBIT.get(), 1.0F, 1.0F);

                // 插入腰带后移除玩家手中的兔子瓶
                player.setItemInHand(hand, ItemStack.EMPTY);

                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.kamen_rider_boss_you_and_me.rabbit");
    }
}
