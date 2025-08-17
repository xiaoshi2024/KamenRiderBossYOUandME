package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
public class Genesis_driver extends Item implements GeoItem, ICurioItem {
    // 基础动画
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlayAndHold("show");


    // 柠檬形态特有动画
    private static final RawAnimation LEMON_TICK = RawAnimation.begin().thenPlayAndHold("lemon_tick");
    private static final RawAnimation START = RawAnimation.begin().thenPlayAndHold("start");
    private static final RawAnimation SCATTER = RawAnimation.begin().thenPlayAndHold("scatter");
    private static final RawAnimation MOVE = RawAnimation.begin().thenPlayAndHold("move");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean isActive = false;
    public boolean isShowing = false;
    public boolean isEquipped  = false;

    // 新增字段
    public boolean isReleasing = false;
    public boolean isHenshining = false;

    public enum BeltMode {
        DEFAULT,
        LEMON
    }

    public BeltMode currentMode = BeltMode.DEFAULT;

    public Genesis_driver(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("lemon_tick", LEMON_TICK)
                .triggerableAnim("move", MOVE)
                .triggerableAnim("scatter", SCATTER)
                .triggerableAnim("start", START)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        AnimationController<?> controller = state.getController();
        String currentAnim = controller.getCurrentAnimation() != null ?
                controller.getCurrentAnimation().animation().name() : "";

        // 优先处理解除状态
        if (isReleasing) {
            if (!"start".equals(currentAnim)) {
                return state.setAndContinue(START);
            }
            // 检查动画是否完成
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                isReleasing = false;
                isShowing = false;
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        // 处理变身序列
        if (isHenshining) {
            // 第一阶段：播放move动画
            if (!"move".equals(currentAnim) && !"scatter".equals(currentAnim)) {
                return state.setAndContinue(MOVE);
            }

            // 第二阶段：move完成后播放scatter（仅柠檬形态）
            if ("move".equals(currentAnim) &&
                    controller.getAnimationState() == AnimationController.State.STOPPED) {

                if (currentMode == BeltMode.LEMON) {
                    return state.setAndContinue(SCATTER);
                } else {
                    isHenshining = false;
                    isShowing = true;
                    return state.setAndContinue(SHOW);
                }
            }

            // 第三阶段：scatter完成后保持展示状态
            if ("scatter".equals(currentAnim) &&
                    controller.getAnimationState() == AnimationController.State.STOPPED) {

                isHenshining = false;
                isShowing = true;
                return state.setAndContinue(SHOW);
            }

            return PlayState.CONTINUE;
        }

        // 展示状态保持
        if (isShowing) {
            if (!"show".equals(currentAnim)) {
                return state.setAndContinue(SHOW);
            }
            return PlayState.CONTINUE;
        }

        // 默认空闲状态
        if (!"idles".equals(currentAnim)) {
            return state.setAndContinue(IDLES);
        }
        return PlayState.CONTINUE;
    }

    // 变身方法
    public void startHenshinAnimation(LivingEntity entity) {
        this.isHenshining = true;
        this.isActive = false;
        this.isShowing = false;
        this.isReleasing = false;

        // 只在服务端发送数据包
        if (!entity.level().isClientSide()) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(
                            entity.getId(), // 使用实体 ID
                            "move",
                            this.currentMode
                    ),
                    entity
            );
        }
        // 无论客户端还是服务端都触发动画
        triggerAnim(entity, "controller", "move");
    }

    // 解除变身方法
    public void startReleaseAnimation(LivingEntity entity) {
        this.isReleasing = true;
        this.isActive = false;
        this.isShowing = false;
        this.isHenshining = false;

        if (entity.level().isClientSide()) {
            this.triggerAnim(entity, "controller", "start");
        } else {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(
                            entity.getId(), // 使用 UUID
                            "start",
                            this.currentMode
                    ),
                    entity
            );
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GenesisDriverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GenesisDriverRenderer();
                return this.renderer;
            }
        });
    }

    public void startShowAnimation(LivingEntity holder) {
        if (!holder.level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    holder.getId(), // 使用 UUID
                    "show",
                    this.currentMode
            ));
        }
        triggerAnim(holder, "controller", "show");
    }

    public void startActionAnimation(LivingEntity holder) {
        String animationName = (currentMode == BeltMode.LEMON) ? "start" : "show";

        if (!holder.level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    holder.getId(), // 使用 UUID
                    animationName,
                    this.currentMode
            ));
        }
        triggerAnim(holder, "controller", animationName);
    }



    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack) != null ? super.getShareTag(stack) : new CompoundTag();
        tag.putString("BeltMode", this.currentMode.name());
        tag.putBoolean("IsShowing", this.isShowing);
        tag.putBoolean("IsActive", this.isActive);
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null) {
            if (nbt.contains("BeltMode")) {
                this.currentMode = BeltMode.valueOf(nbt.getString("BeltMode"));
            }
            if (nbt.contains("IsShowing")) {
                this.isShowing = nbt.getBoolean("IsShowing");
            }
            if (nbt.contains("IsActive")) {
                this.isActive = nbt.getBoolean("IsActive");
            }
        }
    }

    public void triggerAnim(@Nullable LivingEntity entity, String controllerName, String animName) {
        if (entity == null || entity.level() == null) {
            return;
        }

        // 客户端处理动画
        if (entity.level().isClientSide()) {
            AnimatableInstanceCache cache = this.getAnimatableInstanceCache();
            if (cache == null) return;

            AnimatableManager<?> manager = cache.getManagerForId(entity.getId());
            if (manager == null) return;

            AnimationController<?> controller = manager.getAnimationControllers().get(controllerName);
            if (controller == null) return;

            RawAnimation animation = getAnimationByName(animName);
            controller.stop();
            controller.setAnimation(animation);
            controller.forceAnimationReset();
        }
        // 服务端同步数据
        else {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), animName, this.currentMode),
                    entity
            );
        }
    }

    private RawAnimation getAnimationByName(String animName) {
        return switch (animName) {
            case "idles" -> IDLES;
            case "show" -> SHOW;
            case "start" -> START;
            case "scatter" -> SCATTER;
            case "move" -> MOVE;
            default -> IDLES;
        };
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // 确保实体存在且是LivingEntity
        if (slotContext.entity() == null || !(slotContext.entity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity living = (LivingEntity) slotContext.entity();
        this.isShowing = true;
        this.isActive = false;

        // 更新NBT数据
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("IsShowing", true);
        tag.putBoolean("IsActive", false);
        stack.setTag(tag);

        // 只在服务端发送数据包，客户端会通过数据包触发动画
        if (!living.level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    living.getId(),
                    "show",
                    this.currentMode
            ));
        }

        // 无论客户端还是服务端都尝试触发动画
        triggerAnim(living, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // 确保实体存在且是LivingEntity
        if (slotContext.entity() == null || !(slotContext.entity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity living = (LivingEntity) slotContext.entity();
        this.isShowing = false;
        this.isActive = false;

        // 更新NBT数据
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("IsShowing", false);
        tag.putBoolean("IsActive", false);
        stack.setTag(tag);

        // 只在服务端发送数据包
        if (!living.level().isClientSide()) {
            PacketHandler.sendToAll(new BeltAnimationPacket(
                    living.getId(),
                    "idles",
                    this.currentMode
            ));
        }

        // 无论客户端还是服务端都尝试触发动画
        triggerAnim(living, "controller", "idles");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide() &&
                slotContext.entity() instanceof ServerPlayer player) {

            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("BeltMode", currentMode.name());
            tag.putBoolean("IsShowing", isShowing);
            tag.putBoolean("IsActive", isActive);
            stack.setTag(tag);

            if (player.tickCount % 20 == 0) {
                PacketHandler.sendToClient(
                        new BeltAnimationPacket(
                                player.getId(),
                                "sync_state",
                                this.currentMode
                        ),
                        player
                );
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