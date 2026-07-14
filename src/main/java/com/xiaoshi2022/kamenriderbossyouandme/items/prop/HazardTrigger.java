package com.xiaoshi2022.kamenriderbossyouandme.items.prop;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.hazardtrigger.HazardTriggerRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.server.level.ServerPlayer;
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
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class HazardTrigger extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HazardTrigger(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("idle", IDLE)
                .triggerableAnim("start", START));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof HazardTrigger))
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
            private HazardTriggerRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<HazardTrigger> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new HazardTriggerRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player, item -> item != null && item.getItem() instanceof BuildDriver);
        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

            if (currentMode == BuildDriver.BeltMode.DEFAULT || currentMode == BuildDriver.BeltMode.RT) {
                this.triggerAnim(player, player.getId(), "controller", "start");

                belt.activateHazardMode(player, beltStack);

                player.playSound(ModBossSounds.BUILD_HAZARD.get(), 1.0F, 1.0F);

                if (!level.isClientSide && player instanceof ServerPlayer sp) {
                }

                player.setItemInHand(hand, ItemStack.EMPTY);

                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        }

        return InteractionResultHolder.pass(stack);
    }
}