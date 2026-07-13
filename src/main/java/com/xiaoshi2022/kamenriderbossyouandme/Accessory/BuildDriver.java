package com.xiaoshi2022.kamenriderbossyouandme.Accessory;

import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.builddriver.BuildDriverRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import com.xiaoshi2022.kamenriderbossyouandme.util.ServerScheduleUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver.BeltMode.DEFAULT;

public class BuildDriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    private static final RawAnimation IDLES   = RawAnimation.begin().thenPlay("idles");
    private static final RawAnimation SHOW    = RawAnimation.begin().thenPlay("show");
    private static final RawAnimation CANCEL  = RawAnimation.begin().thenPlay("cancel");
    private static final RawAnimation CANCEL_S = RawAnimation.begin().thenPlay("cancel_s");
    private static final RawAnimation TURN    = RawAnimation.begin().thenPlay("turn");
    private static final RawAnimation IDLE    = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation MOULD   = RawAnimation.begin().thenPlay("mould");
    private static final RawAnimation MOULD_B = RawAnimation.begin().thenPlay("mould_b");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final String TAG_BELT_MODE = "BeltMode";
    private static final String TAG_IS_SHOWING = "IsShowing";
    private static final String TAG_IS_ACTIVE = "IsActive";
    private static final String TAG_IS_RELEASE = "IsRelease";
    private static final String TAG_IS_TURNING = "IsTurning";
    private static final String TAG_IS_TRANSFORMING = "IsTransforming";
    private static final String TAG_IS_PLAYING_MOULD = "IsPlayingMould";
    private static final String TAG_IS_PLAYING_MOULD_B = "IsPlayingMouldB";

    public enum BeltMode {
        DEFAULT, RT, R, T,
        HAZARD_EMPTY, HAZARD_RT, HAZARD_R, HAZARD_T, HAZARD_K, HAZARD_KR, HAZARD_RESSYA, HAZARD_RT_MOULD
    }

    public BuildDriver(Properties properties) {
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

    private static void modifyTag(ItemStack stack, Consumer<CompoundTag> action) {
        if (stack == null || stack.isEmpty()) return;
        CompoundTag tag = getOrCreateTag(stack);
        action.accept(tag);
        saveTag(stack, tag);
    }

    /* ========================= GeoItem ========================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("cancel", CANCEL)
                .triggerableAnim("cancel_s", CANCEL_S)
                .triggerableAnim("turn", TURN)
                .triggerableAnim("idle", IDLE)
                .triggerableAnim("mould", MOULD)
                .triggerableAnim("mould_b", MOULD_B)
                .setSoundKeyframeHandler(event -> {
                    // 修复：SoundKeyframeEvent 使用 getAnimatable()
                    GeoAnimatable animatable = event.getAnimatable();
                    if (animatable instanceof Player player) {
                        player.playSound(ModBossSounds.RT_BY.get(), 1.0F, 1.0F);
                    }
                }));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty() || !(state.getAnimatable() instanceof BuildDriver))
            return PlayState.STOP;

        BeltMode mode = getMode(stack);
        boolean showing = getShowing(stack);
        boolean active = getActive(stack);
        boolean rel = getRelease(stack);
        boolean isTurning = getIsTurning(stack);
        boolean isTransforming = getIsTransforming(stack);
        boolean isPlayingMould = getIsPlayingMould(stack);
        boolean isPlayingMouldB = getIsPlayingMouldB(stack);

        String current = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        if (rel) {
            if (!current.equals("cancel"))
                return state.setAndContinue(CANCEL);

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, DEFAULT);
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        if (isTurning) {
            if (!current.equals("turn")) {
                return state.setAndContinue(TURN);
            }
            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setIsTurning(stack, false);
                return PlayState.CONTINUE;
            }
            return PlayState.CONTINUE;
        }

        if (mode == BeltMode.HAZARD_RT_MOULD) {
            if (isPlayingMould) {
                if (!current.equals("mould")) {
                    return state.setAndContinue(MOULD);
                }
                if (current.equals("mould") && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    setIsPlayingMould(stack, false);
                    return PlayState.CONTINUE;
                }
                return PlayState.CONTINUE;
            }

            if (isPlayingMouldB) {
                if (!current.equals("mould_b")) {
                    return state.setAndContinue(MOULD_B);
                }
                if (current.equals("mould_b") && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    setIsPlayingMouldB(stack, false);
                    return PlayState.CONTINUE;
                }
                return PlayState.CONTINUE;
            }

            return PlayState.CONTINUE;
        }

        if (showing) {
            if (!"show".equals(current))
                return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        if (!"idles".equals(current))
            return state.setAndContinue(IDLES);

        return PlayState.CONTINUE;
    }

    /* =========================================================== */
    /* -------------------- 数据读/写 Helper -------------------- */
    public BeltMode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return DEFAULT;
        }
        CompoundTag tag = getOrCreateTag(stack);
        String modeName = tag.getString(TAG_BELT_MODE);
        if (modeName.isEmpty()) {
            return DEFAULT;
        }
        try {
            return BeltMode.valueOf(modeName);
        } catch (IllegalArgumentException ex) {
            return DEFAULT;
        }
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putString(TAG_BELT_MODE, mode.name()));
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_SHOWING);
    }

    public void setShowing(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_SHOWING, flag));
    }

    public boolean getActive(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_ACTIVE);
    }

    public void setActive(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_ACTIVE, flag));
    }

    public boolean getRelease(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_RELEASE);
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_RELEASE, flag));
    }

    public boolean getIsTurning(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_TURNING);
    }

    public void setIsTurning(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_TURNING, flag));
    }

    public boolean getIsTransforming(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_TRANSFORMING);
    }

    public void setIsTransforming(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_TRANSFORMING, flag));
    }

    public boolean getIsPlayingMould(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_PLAYING_MOULD);
    }

    public void setIsPlayingMould(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_PLAYING_MOULD, flag));
    }

    public boolean getIsPlayingMouldB(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_PLAYING_MOULD_B);
    }

    public void setIsPlayingMouldB(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_PLAYING_MOULD_B, flag));
    }

    /* ----------------------------------------------------------- */

    /* ===================== 业务方法 ==================== */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || stack.isEmpty() || entity.level() == null) {
            return;
        }

        setRelease(stack, true);

        BeltMode mode = getMode(stack);
        String anim1 = "cancel";
        String anim2 = "cancel_s";

        if (entity instanceof ServerPlayer sp) {
            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), anim1, "builddriver", mode.name())
            );
            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), anim2, "builddriver", mode.name())
            );
        }
    }

    public void activateHazardMode(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null || beltStack.isEmpty()) return;

        BeltMode currentMode = getMode(beltStack);
        BeltMode newMode;

        switch (currentMode) {
            case DEFAULT:
                newMode = BeltMode.HAZARD_EMPTY;
                break;
            case RT:
                newMode = BeltMode.HAZARD_RT;
                UUID playerUUID = entity.getUUID();
                ServerScheduleUtils.scheduleSeconds(2.0f, () -> {
                    ServerPlayer player = entity.getServer().getPlayerList().getPlayer(playerUUID);
                    if (player != null) {
                        player.playSound(ModBossSounds.SUPER_BEST_MATCH.get(), 1.0F, 1.0F);
                    }
                });
                break;
            case R:
                newMode = BeltMode.HAZARD_R;
                break;
            case T:
                newMode = BeltMode.HAZARD_T;
                break;
            default:
                newMode = BeltMode.HAZARD_EMPTY;
                break;
        }

        setMode(beltStack, newMode);

        if (entity instanceof ServerPlayer sp) {
            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), "idle", "builddriver", newMode.name())
            );
        }
    }

    public void deactivateHazardMode(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null || beltStack.isEmpty()) return;

        BeltMode currentMode = getMode(beltStack);
        BeltMode newMode;

        switch (currentMode) {
            case HAZARD_RT:
                newMode = BeltMode.RT;
                break;
            case HAZARD_R:
                newMode = BeltMode.R;
                break;
            case HAZARD_T:
                newMode = BeltMode.T;
                break;
            default:
                newMode = BeltMode.DEFAULT;
                break;
        }

        setMode(beltStack, newMode);

        if (entity instanceof ServerPlayer sp) {
            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), "idle", "builddriver", newMode.name())
            );
        }
    }

    /* =========================================================== */

    /* -------------------- 其它必要实现 -------------------- */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BuildDriverRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<BuildDriver> getGeoItemRenderer() {
                if (renderer == null) renderer = new BuildDriverRenderer();
                return renderer;
            }
        });
    }

    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        super.onEquip(ctx, prev, stack);
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltEquipped(player, stack);
        }
    }

    @Override
    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) {
            return;
        }

        setShowing(beltStack, true);
        setActive(beltStack, false);
        setRelease(beltStack, false);

        PacketHandler.sendToTrackingAndSelf(
                player,
                new BeltAnimationPacket(player.getId(), "show", "builddriver", DEFAULT.name())
        );

        triggerAnim(player, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
            return;
        }

        if (!(ctx.entity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setActive(stack, false);

        if (le.level() != null && !le.level().isClientSide && le instanceof ServerPlayer sp) {
            PacketHandler.sendToTrackingAndSelf(sp, new BeltAnimationPacket(sp.getId(), "idles", "builddriver", getMode(stack).name()));
        }

        triggerAnim(le, "controller", "idles");
    }

//    @Override
//    public void curioTick(SlotContext ctx, ItemStack stack) {
//        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
//            return;
//        }
//        if (ctx.entity().level() == null || ctx.entity().level().isClientSide()) {
//            return;
//        }
//        if (!(ctx.entity() instanceof ServerPlayer sp)) {
//            return;
//        }
//
//    }

    public boolean onRightClick(SlotContext ctx, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
            return false;
        }

        if (!(ctx.entity() instanceof ServerPlayer sp)) {
            return false;
        }

        ItemStack heldItem = sp.getMainHandItem();

        if (heldItem.isEmpty()) {
            BeltMode currentMode = getMode(stack);
            boolean isHazardMode = currentMode.toString().startsWith("HAZARD_");

            switch (currentMode) {
                case RT:
                case HAZARD_RT:
                    setMode(stack, isHazardMode ? BeltMode.HAZARD_R : BeltMode.R);
                    break;
                case R:
                case HAZARD_R:
                    setMode(stack, isHazardMode ? BeltMode.HAZARD_EMPTY : BeltMode.DEFAULT);
                    break;
                case T:
                case HAZARD_T:
                    setMode(stack, isHazardMode ? BeltMode.HAZARD_EMPTY : BeltMode.DEFAULT);
                    break;
                case HAZARD_KR:
                    setMode(stack, BeltMode.HAZARD_K);
                    break;
                case HAZARD_K:
                    setMode(stack, BeltMode.HAZARD_EMPTY);
                    break;
                case HAZARD_RESSYA:
                    setMode(stack, BeltMode.HAZARD_EMPTY);
                    break;
                case HAZARD_EMPTY:
                    setMode(stack, BeltMode.DEFAULT);
                    break;
                default:
                    return false;
            }

            triggerAnim(sp, "controller", "cancel");

            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), "cancel", "builddriver", getMode(stack).name())
            );

            return true;
        }

        return false;
    }

    /* -------------------- NBT 同步 -------------------- */
// 移除 @Override 注解，或者改为：
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_BELT_MODE, getMode(stack).name());
        tag.putBoolean(TAG_IS_SHOWING, getShowing(stack));
        tag.putBoolean(TAG_IS_ACTIVE, getActive(stack));
        tag.putBoolean(TAG_IS_RELEASE, getRelease(stack));
        tag.putBoolean(TAG_IS_TURNING, getIsTurning(stack));
        tag.putBoolean(TAG_IS_TRANSFORMING, getIsTransforming(stack));
        tag.putBoolean(TAG_IS_PLAYING_MOULD, getIsPlayingMould(stack));
        tag.putBoolean(TAG_IS_PLAYING_MOULD_B, getIsPlayingMouldB(stack));
        return tag;
    }

    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt.contains(TAG_BELT_MODE)) setMode(stack, BeltMode.valueOf(nbt.getString(TAG_BELT_MODE)));
        if (nbt.contains(TAG_IS_SHOWING)) setShowing(stack, nbt.getBoolean(TAG_IS_SHOWING));
        if (nbt.contains(TAG_IS_ACTIVE)) setActive(stack, nbt.getBoolean(TAG_IS_ACTIVE));
        if (nbt.contains(TAG_IS_RELEASE)) setRelease(stack, nbt.getBoolean(TAG_IS_RELEASE));
        if (nbt.contains(TAG_IS_TURNING)) setIsTurning(stack, nbt.getBoolean(TAG_IS_TURNING));
        if (nbt.contains(TAG_IS_TRANSFORMING)) setIsTransforming(stack, nbt.getBoolean(TAG_IS_TRANSFORMING));
        if (nbt.contains(TAG_IS_PLAYING_MOULD)) setIsPlayingMould(stack, nbt.getBoolean(TAG_IS_PLAYING_MOULD));
        if (nbt.contains(TAG_IS_PLAYING_MOULD_B)) setIsPlayingMouldB(stack, nbt.getBoolean(TAG_IS_PLAYING_MOULD_B));
    }

    /* -------------------- 动画触发工具 -------------------- */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;

        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            Optional<SlotResult> beltOptional = CurioUtils.findFirstCurio(sp, item -> item.getItem() instanceof BuildDriver);
            BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(DEFAULT);

            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), anim, "builddriver", mode.name())
            );
        }
    }

    public void triggerPlayerAnim(@Nullable LivingEntity entity, String anim) {
        if (entity == null || entity.level() == null) return;

        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            BYAnimationPacket packet = new BYAnimationPacket(sp.getUUID(), anim, 5);

            // 发送给玩家自己
            PacketHandler.sendToClient(sp, packet);

            // 发送给所有追踪的玩家（其他人也能看到）
            PacketHandler.sendToAllTracking(sp, packet);
        }
    }
}