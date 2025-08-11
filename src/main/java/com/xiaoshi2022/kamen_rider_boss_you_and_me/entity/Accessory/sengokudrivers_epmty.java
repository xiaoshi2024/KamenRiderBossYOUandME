package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver.sengokudrivers_epmtysRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
    private static final RawAnimation CUT = RawAnimation.begin().thenPlayAndHold("cut").thenLoop("cut_hold");;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean isEquipped = false;
    public BeltMode currentMode = BeltMode.DEFAULT;
    private boolean shouldPlayRelease = false;
    public boolean isHenshining = false;


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

    // 在sengokudrivers_epmty类中添加
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack) != null ? super.getShareTag(stack) : new CompoundTag();
        tag.putString("BeltMode", this.currentMode.name());
        tag.putBoolean("IsEquipped", this.isEquipped);
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null) {
            if (nbt.contains("BeltMode")) {
                this.currentMode = BeltMode.valueOf(nbt.getString("BeltMode"));
            }
            if (nbt.contains("IsEquipped")) {
                this.isEquipped = nbt.getBoolean("IsEquipped");
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationController));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        // 优先处理释放动画
        if (shouldPlayRelease) {
            shouldPlayRelease = false;
            return state.setAndContinue(RELEASE);
        }

        // 如果处于变身状态，保持CUT动画
        if (isEquipped && currentMode == BeltMode.BANANA) {
            return state.setAndContinue(CUT);
        }

        // 默认状态处理
        if (!isEquipped) {
            return state.setAndContinue(IDLES);
        } else {
            return state.setAndContinue(currentMode == BeltMode.BANANA ? BANANA_IDLE : SHOW);
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


    public void triggerAnim(LivingEntity entity, String controllerName, String animName) {
        if (entity.level().isClientSide()) {
            AnimatableManager<?> manager = this.getAnimatableInstanceCache().getManagerForId(entity.getId());
            if (manager != null) {
                AnimationController<?> controller = manager.getAnimationControllers().get(controllerName);
                if (controller != null) {
                    RawAnimation animation = switch (animName) {
                        case "show" -> SHOW;
                        case "idles" -> IDLES;
                        case "banana_idle" -> BANANA_IDLE;
                        case "release" -> RELEASE;
                        case "cut" -> CUT;
                        default -> IDLES;
                    };

                    // 如果当前不是相同的动画才触发
                    if (controller.getCurrentAnimation() == null ||
                            !controller.getCurrentAnimation().animation().name().equals(animName)) {
                        controller.stop();
                        controller.setAnimation(animation);
                    }
                }
            }
        } else {
            // 服务器端同步给所有客户端
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), animName, this.currentMode),
                    entity
            );
        }
    }


    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        isEquipped = true;
        this.currentMode = getMode(stack);

        // 根据当前形态播放相应的动画
        String animationName = this.currentMode == BeltMode.BANANA ? "banana_idle" : "show";

        if (!slotContext.entity().level().isClientSide()) {
            // 发送数据包到客户端，触发动画
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    slotContext.entity().getId(),
                    animationName, // 根据形态选择动画
                    this.currentMode
            ));
        } else {
            // 如果已经在客户端，直接触发动画
            triggerAnim(slotContext.entity(), "controller", animationName);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        isEquipped = false;

        // 当玩家卸下腰带时，播放 IDLES 动画
        if (!slotContext.entity().level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    slotContext.entity().getId(),
                    "idles",
                    this.currentMode
            ));
        } else {
            // 如果已经在客户端，直接触发动画
            triggerAnim(slotContext.entity(), "controller", "idles");
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // 确保服务端定期同步状态到客户端
        if (!slotContext.entity().level().isClientSide() &&
                slotContext.entity() instanceof ServerPlayer player) {

            // 更新NBT数据
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("BeltMode", currentMode.name());
            tag.putBoolean("IsEquipped", isEquipped);
            stack.setTag(tag);

            // 每20 ticks同步一次(1秒)
            if (player.tickCount % 20 == 0) {
                PacketHandler.sendToClient(
                        new BeltAnimationPacket(
                                player.getId(),
                                "sync_state", // 使用特定动作标识同步
                                this.currentMode
                        ),
                        player
                );

                // 强制同步物品堆栈
                player.getInventory().setChanged();
            }
        }
    }


    public BeltMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BeltMode")) {
            return BeltMode.valueOf(tag.getString("BeltMode"));
        }
        return BeltMode.DEFAULT;
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack.getEntityRepresentation() != null && stack.getEntityRepresentation().level().isClientSide) {
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