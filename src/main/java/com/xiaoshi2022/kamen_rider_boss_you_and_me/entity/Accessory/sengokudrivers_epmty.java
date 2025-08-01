package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver.sengokudrivers_epmtysRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
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
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class sengokudrivers_epmty extends Item implements GeoItem, ICurioItem {
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation BANANA_IDLE = RawAnimation.begin().thenPlayAndHold("banana_idle");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlayAndHold("release");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isEquipped = false;
    private BeltMode currentMode = BeltMode.DEFAULT;
    private boolean shouldPlayRelease = false;

    public enum BeltMode {
        DEFAULT,
        BANANA
    }

    public BeltMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BeltMode")) {
            return BeltMode.valueOf(tag.getString("BeltMode"));
        }
        return BeltMode.DEFAULT;
    }

    // 确保NBT同步
    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack.getEntityRepresentation() != null &&
                stack.getEntityRepresentation().level().isClientSide) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("BeltMode", mode.name());
        this.currentMode = mode;
        stack.setTag(tag);

        // 通知物品更新
        if (stack.getEntityRepresentation() instanceof Player player) {
            player.getInventory().setChanged();
        }
    }

    public sengokudrivers_epmty(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void startReleaseAnimation(LivingEntity holder) {
        this.shouldPlayRelease = true;
        triggerAnim(holder, "controller", "release");
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationController));
    }

    // 强化动画控制
    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        // 优先处理释放动画
        if (state.getController().getCurrentAnimation() != null &&
                state.getController().getCurrentAnimation().animation().name().equals("release")) {
            return PlayState.CONTINUE;
        }

        if (currentMode == BeltMode.BANANA) {
            return state.setAndContinue(BANANA_IDLE);
        }
        return state.setAndContinue(isEquipped ? SHOW : IDLES);
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

    // 同时需要更新triggerAnim方法，确保能正确处理动画切换
    // 修改后的triggerAnim方法
    public void triggerAnim(LivingEntity entity, String controllerName, String animName) {
        if (entity.level().isClientSide()) {
            try {
                AnimatableManager<?> manager = this.getAnimatableInstanceCache().getManagerForId(entity.getId());
                if (manager != null) {
                    AnimationController<?> controller = manager.getAnimationControllers().get(controllerName);
                    if (controller != null) {
                        // 先停止当前动画
                        controller.stop();

                        // 根据动画名称选择对应的RawAnimation
                        RawAnimation animation = switch(animName) {
                            case "show" -> SHOW;
                            case "idles" -> IDLES;
                            case "banana_idle" -> BANANA_IDLE;
                            case "release" -> RELEASE;
                            default -> IDLES; // 默认回退
                        };

                        // 强制重置并播放新动画
                        controller.setAnimation(animation);
                        controller.forceAnimationReset();
                    }
                }
            } catch (Exception e) {
                System.err.println("动画触发错误: " + e.getMessage());
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        isEquipped = true;
        this.currentMode = getMode(stack);
        triggerAnim(slotContext.entity(), "controller", this.currentMode == BeltMode.BANANA ? "banana_idle" : "show");
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        isEquipped = false;
        triggerAnim(slotContext.entity(), "controller", this.currentMode == BeltMode.BANANA ? "banana_idle" : "idles");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide()) {
            this.currentMode = getMode(stack);
        }
    }
}