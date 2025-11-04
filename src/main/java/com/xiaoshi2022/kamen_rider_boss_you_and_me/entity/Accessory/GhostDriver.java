//package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;
//
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
//import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.effect.MobEffects;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.TooltipFlag;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.client.extensions.common.IClientItemExtensions;
//import net.minecraftforge.fml.common.Mod;
//import software.bernie.geckolib.animatable.GeoItem;
//import software.bernie.geckolib.constant.DataTickets;
//import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
//import software.bernie.geckolib.core.animation.AnimatableManager;
//import software.bernie.geckolib.core.animation.AnimationController;
//import software.bernie.geckolib.core.animation.AnimationState;
//import software.bernie.geckolib.core.animation.RawAnimation;
//import software.bernie.geckolib.core.object.PlayState;
//import software.bernie.geckolib.util.GeckoLibUtil;
//import top.theillusivec4.curios.api.CuriosApi;
//import top.theillusivec4.curios.api.SlotContext;
//import top.theillusivec4.curios.api.SlotResult;
//import top.theillusivec4.curios.api.type.capability.ICurioItem;
//
//import javax.annotation.Nullable;
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Consumer;
//
//@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
//public class GhostDriver extends AbstractRiderBelt implements GeoItem, ICurioItem {
//
//    /* ----------------- 动画常量 ----------------- */
//    private static final RawAnimation IDLES  = RawAnimation.begin().thenPlayAndHold("idles");
//    private static final RawAnimation SHOW   = RawAnimation.begin().thenPlayAndHold("show");
//    private static final RawAnimation HENSHIN = RawAnimation.begin().thenPlayAndHold("henshin");
//    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlayAndHold("cancel");
//    private static final RawAnimation PULL   = RawAnimation.begin().thenPlayAndHold("pull");
//
//    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
//
//    public void setEquipped(ItemStack stack, boolean flag) {
//        stack.getOrCreateTag().putBoolean("IsEquipped", flag);
//    }
//
//    public void setHenshin(ItemStack stack, boolean flag) {
//        stack.getOrCreateTag().putBoolean("IsHenshin", flag);
//    }
//
//    public enum BeltMode {
//        DEFAULT,
//        DARK_RIDER_EYE
//    }
//
//    public GhostDriver(Properties properties) {
//        super(properties);
//    }
//
//    /* ================= GeoItem ================= */
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
//        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
//                .triggerableAnim("show", SHOW)
//                .triggerableAnim("idles", IDLES)
//                .triggerableAnim("henshin", HENSHIN)
//                .triggerableAnim("cancel", CANCEL)
//                .triggerableAnim("pull", PULL));
//    }
//
//    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
//        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
//        if (!(state.getAnimatable() instanceof GhostDriver)) return PlayState.STOP;
//
//        BeltMode mode = getMode(stack);
//        boolean show = getShowing(stack);
//        boolean release = getRelease(stack);
//        boolean hen = getHenshin(stack);
//
//        String cur = state.getController().getCurrentAnimation() == null
//                ? "" : state.getController().getCurrentAnimation().animation().name();
//
//        /* -------- 变身序列 -------- */
//        if (hen) {
//            if (!cur.equals("henshin"))
//                return state.setAndContinue(HENSHIN);
//            return PlayState.CONTINUE;
//        }
//
//        /* -------- 解除变身 -------- */
//        if (release) {
//            if (!cur.equals("cancel"))
//                return state.setAndContinue(CANCEL);
//
//            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
//                setRelease(stack, false);
//                setShowing(stack, false);
//                setMode(stack, BeltMode.DEFAULT);
//                return state.setAndContinue(IDLES);
//            }
//            return PlayState.CONTINUE;
//        }
//
//        /* -------- 展示/待机 -------- */
//        if (show) {
//            if (!cur.equals("show")) return state.setAndContinue(SHOW);
//            return PlayState.CONTINUE;
//        }
//
//        /* -------- 默认 -------- */
//        if (!cur.equals("idles")) return state.setAndContinue(IDLES);
//        return PlayState.CONTINUE;
//    }
//
//    public boolean getHenshin(ItemStack stack) {
//        return stack.getOrCreateTag().getBoolean("IsHenshin");
//    }
//
//    /* -------------- NBT 工具 -------------- */
//    public BeltMode getMode(ItemStack stack) {
//        try {
//            return BeltMode.valueOf(stack.getOrCreateTag().getString("BeltMode"));
//        } catch (IllegalArgumentException e) {
//            return BeltMode.DEFAULT;
//        }
//    }
//
//    public void setModeAndTriggerHenshin(LivingEntity entity, ItemStack stack, BeltMode mode) {
//        setMode(stack, mode);
//        triggerAnim(entity, "controller", "henshin");
//    }
//
//    public void setMode(ItemStack stack, BeltMode mode) {
//        stack.getOrCreateTag().putString("BeltMode", mode.name());
//    }
//
//    public boolean getShowing(ItemStack stack) {
//        return stack.getOrCreateTag().getBoolean("IsShowing");
//    }
//
//    public void setShowing(ItemStack stack, boolean v) {
//        stack.getOrCreateTag().putBoolean("IsShowing", v);
//    }
//
//    public boolean getRelease(ItemStack stack) {
//        return stack.getOrCreateTag().getBoolean("IsRelease");
//    }
//
//    public void setRelease(ItemStack stack, boolean v) {
//        stack.getOrCreateTag().putBoolean("IsRelease", v);
//    }
//
//    /* -------------- 业务 -------------- */
//    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
//        setRelease(stack, true);
//        setShowing(stack, false);
//
//        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp)
//            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "cancel", getMode(stack)), sp);
//
//        triggerAnim(entity, "controller", "cancel");
//    }
//
//    /* -------------- 工具 -------------- */
//    private RawAnimation getAnim(String name) {
//        return switch (name) {
//            case "show" -> SHOW;
//            case "idles" -> IDLES;
//            case "henshin" -> HENSHIN;
//            case "cancel" -> CANCEL;
//            case "pull" -> PULL;
//            default -> IDLES;
//        };
//    }
//
//    /* -------------- 物品提示 -------------- */
//    @Override
//    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
//        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
//        // 添加当前腰带形态提示
//        BeltMode mode = getMode(stack);
//        String modeText = switch (mode) {
//            case DARK_RIDER_EYE -> "暗眼魂形态";
//            default -> "普通形态（无眼魂）";
//        };
//        tooltipComponents.add(Component.translatable("tooltip.ghostdriver.mode", modeText));
//    }
//
//    /* -------------- Curio -------------- */
//    @Override
//    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
//        super.onEquip(ctx, prev, stack);
//    }
//
//    /**
//     * 实现基类的腰带装备逻辑
//     */
//    @Override
//    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
//        setHenshin(beltStack, false);
//        setRelease(beltStack, false);
//        setShowing(beltStack, true);
//
//        // 同步腰带状态到所有跟踪的玩家
//        PacketHandler.sendToAllTracking(
//                new BeltAnimationPacket(player.getId(), "show", getMode(beltStack)),
//                player);
//
//        // 更新玩家变量
//        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
//            variables.isGhostDriverEquipped = true;
//            variables.syncPlayerVariables(player);
//        });
//
//        // 给玩家添加夜视效果
//        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
//
//        // 触发动画
//        triggerAnim(player, "controller", "show");
//    }
//
//    @Override
//    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
//        if (!(ctx.entity() instanceof LivingEntity)) return;
//        LivingEntity le = (LivingEntity) ctx.entity();
//        setShowing(stack, false);
//        setRelease(stack, false);
//
//        if (!le.level().isClientSide() && le instanceof ServerPlayer sp) {
//            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "idles", getMode(stack)), sp);
//            // 移除玩家的夜视效果
//            sp.removeEffect(MobEffects.NIGHT_VISION);
//        }
//
//        triggerAnim(le, "controller", "idles");
//    }
//
//    @Override
//    public void curioTick(SlotContext ctx, ItemStack stack) {
//        if (ctx.entity().level().isClientSide()) return;
//        if (!(ctx.entity() instanceof ServerPlayer sp)) return;
//
//        // 1 秒同步一次
//        if (sp.tickCount % 20 == 0) {
//            PacketHandler.sendToClient(
//                    new BeltAnimationPacket(sp.getId(), "sync_state", getMode(stack)), sp);
//        }
//
//        // 每5秒检查一次并重新应用夜视效果，确保效果持续存在
//        if (sp.tickCount % 100 == 0) {
//            if (!sp.hasEffect(MobEffects.NIGHT_VISION)) {
//                sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
//            }
//        }
//    }
//
//    /* -------------- 同步 -------------- */
//    @Override
//    public CompoundTag getShareTag(ItemStack stack) {
//        CompoundTag tag = super.getShareTag(stack);
//        if (tag == null) tag = new CompoundTag();
//        tag.putString("BeltMode", getMode(stack).name());
//        tag.putBoolean("IsShowing", getShowing(stack));
//        tag.putBoolean("IsRelease", getRelease(stack));
//        return tag;
//    }
//
//    @Override
//    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
//        super.readShareTag(stack, nbt);
//        if (nbt == null) return;
//        if (nbt.contains("BeltMode")) setMode(stack, BeltMode.valueOf(nbt.getString("BeltMode")));
//        if (nbt.contains("IsShowing")) setShowing(stack, nbt.getBoolean("IsShowing"));
//        if (nbt.contains("IsRelease")) setRelease(stack, nbt.getBoolean("IsRelease"));
//    }
//
//    /* -------------- 客户端渲染器 -------------- */
//    @Override
//    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//        consumer.accept(new IClientItemExtensions() {
//            private GhostDriverRenderer renderer;
//
//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                if (renderer == null) renderer = new GhostDriverRenderer();
//                return renderer;
//            }
//        });
//    }
//
//    @Override
//    public AnimatableInstanceCache getAnimatableInstanceCache() {
//        return cache;
//    }
//
//    /* -------------- 触发工具 -------------- */
//    public void triggerAnim(LivingEntity entity, String ctrl, String anim) {
//        if (entity == null || entity.level() == null) return;
//        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
//            // 从Curio槽位获取腰带模式
//            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(sp).resolve()
//                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof GhostDriver));
//            GhostDriver.BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(BeltMode.DEFAULT);
//
//            PacketHandler.sendToAllTracking(
//                    new BeltAnimationPacket(entity.getId(), anim, mode), entity);
//        }
//    }
//}