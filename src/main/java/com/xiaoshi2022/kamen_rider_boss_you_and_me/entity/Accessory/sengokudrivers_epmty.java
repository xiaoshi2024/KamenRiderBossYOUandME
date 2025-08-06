package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver.sengokudrivers_epmtysRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class sengokudrivers_epmty extends Item implements GeoItem, ICurioItem {
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation BANANA_IDLE = RawAnimation.begin().thenPlayAndHold("banana_idle");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlayAndHold("release");
    private static final RawAnimation CUT = RawAnimation.begin().thenPlayAndHold("cut");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isEquipped = false;
    private BeltMode currentMode = BeltMode.DEFAULT;
    private boolean shouldPlayRelease = false;

    public void handleAnimationPacket(LivingEntity livingEntity, String animationName, BeltMode beltMode) {
        if (animationName.equals("cut")) {
            triggerAnim(livingEntity, "controller", "cut");
            this.currentMode = beltMode;
        }
    }

    public enum BeltMode {
        DEFAULT,
        BANANA;

        public static BeltMode fromString(String name) {
            return valueOf(name);
        }
    }

    public sengokudrivers_epmty(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void startReleaseAnimation(LivingEntity holder) {
        this.shouldPlayRelease = true;

        if (!holder.level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    holder.getId(),
                    "release",
                    this.currentMode
            ));
        }

        triggerAnim(holder, "controller", "release");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationController));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        if (state.getController().getAnimationState() == AnimationController.State.TRANSITIONING
                && "cut".equals(state.getController().getCurrentAnimation().animation().name())) {
            return PlayState.CONTINUE;
        }
        if (shouldPlayRelease) {
            shouldPlayRelease = false;
            return state.setAndContinue(RELEASE);
        }
        if (state.getController().getCurrentAnimation() != null &&
                state.getController().getCurrentAnimation().animation().name().equals("release")) {
            return PlayState.CONTINUE;
        }
        if (currentMode == BeltMode.BANANA) {
            return state.setAndContinue(BANANA_IDLE);
        } else {
            return state.setAndContinue(isEquipped ? SHOW : IDLES);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private sengokudrivers_epmtysRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new sengokudrivers_epmtysRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack) != null ? super.getShareTag(stack) : new CompoundTag();
        tag.putString("BeltMode", this.currentMode.name());
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null && nbt.contains("BeltMode")) {
            this.currentMode = BeltMode.valueOf(nbt.getString("BeltMode"));
        }
    }

    public void triggerAnim(LivingEntity entity, String controllerName, String animName) {
        if (entity.level().isClientSide()) {
            AnimatableManager<?> manager = this.getAnimatableInstanceCache().getManagerForId(entity.getId());
            if (manager != null) {
                AnimationController<?> controller = manager.getAnimationControllers().get(controllerName);
                if (controller != null) {
                    if (controller.getCurrentAnimation() != null &&
                            controller.getCurrentAnimation().animation().name().equals(animName)) {
                        return;
                    }
                    RawAnimation animation = switch (animName) {
                        case "show" -> SHOW;
                        case "idles" -> IDLES;
                        case "banana_idle" -> BANANA_IDLE;
                        case "release" -> RELEASE;
                        default -> IDLES;
                    };
                    controller.stop();
                    controller.setAnimation(animation);
                    controller.forceAnimationReset();
                }
            }
        } else {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(
                            entity.getId(),
                            animName,
                            this.currentMode
                    ),
                    entity
            );
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        isEquipped = true;
        this.currentMode = getMode(stack);

        if (!slotContext.entity().level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    slotContext.entity().getId(),
                    this.currentMode == BeltMode.BANANA ? "banana_idle" : "show",
                    this.currentMode
            ));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        isEquipped = false;

        if (!slotContext.entity().level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    slotContext.entity().getId(),
                    this.currentMode == BeltMode.BANANA ? "banana_idle" : "idles",
                    this.currentMode
            ));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // No additional logic needed for tick
    }

    public BeltMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BeltMode")) {
            return BeltMode.valueOf(tag.getString("BeltMode"));
        }
        return BeltMode.DEFAULT;
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack.getEntityRepresentation() != null &&
                stack.getEntityRepresentation().level().isClientSide) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("BeltMode", mode.name());
        this.currentMode = mode;
        stack.setTag(tag);

        if (stack.getEntityRepresentation() instanceof Player player) {
            player.getInventory().setChanged();
        }
    }
}