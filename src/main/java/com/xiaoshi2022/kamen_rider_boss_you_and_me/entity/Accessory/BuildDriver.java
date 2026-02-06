package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.builddriver.BuildDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.BuildDriverKeyHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.RabbitItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.TankItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.HazardTrigger;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver.BeltMode.DEFAULT;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class BuildDriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ------------------------- 动画常量 ------------------------- */
    private static final RawAnimation IDLES   = RawAnimation.begin().thenPlay("idles");
    private static final RawAnimation SHOW    = RawAnimation.begin().thenPlay("show");
    private static final RawAnimation CANCEL  = RawAnimation.begin().thenPlay("cancel");
    private static final RawAnimation CANCEL_S = RawAnimation.begin().thenPlay("cancel_s");
    private static final RawAnimation TURN    = RawAnimation.begin().thenPlay("turn");
    private static final RawAnimation IDLE    = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation MOULD   = RawAnimation.begin().thenPlay("mould");
    private static final RawAnimation MOULD_B = RawAnimation.begin().thenPlay("mould_b");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public enum BeltMode {
        DEFAULT, RT, R, T, HAZARD_EMPTY, HAZARD_RT, HAZARD_R, HAZARD_T, HAZARD_RT_MOULD
    }

    public BuildDriver(Properties properties) {
        super(properties);
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
                .triggerableAnim("mould_b", MOULD_B) .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null) {
                        player.playSound(ModBossSounds.RT_BY.get(), 1.0F, 1.0F);
                    }
                }));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof BuildDriver))
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

        /* -------- 解除变身 -------- */
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

        /* -------- 转动动画 -------- */
        if (isTurning) {
            // 无论当前动画是什么，都播放turn动画，确保持续播放
            return state.setAndContinue(TURN);
        }

        /* -------- HAZARD_RT_MOULD模式动画 -------- */
        if (mode == BeltMode.HAZARD_RT_MOULD) {
            // 处理mould动画（按下X键）
            if (isPlayingMould) {
                if (!current.equals("mould")) {
                    return state.setAndContinue(MOULD);
                }
                // 当mould动画播放完成后，重置状态
                if (current.equals("mould") && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    setIsPlayingMould(stack, false);
                    return PlayState.CONTINUE;
                }
                return PlayState.CONTINUE;
            }
            
            // 处理mould_b动画（松开X键）
            if (isPlayingMouldB) {
                if (!current.equals("mould_b")) {
                    return state.setAndContinue(MOULD_B);
                }
                // 当mould_b动画播放完成后，重置状态
                if (current.equals("mould_b") && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    setIsPlayingMouldB(stack, false);
                    return PlayState.CONTINUE;
                }
                return PlayState.CONTINUE;
            }
            
            // 默认返回CONTINUE
            return PlayState.CONTINUE;
        }

        /* -------- 展示 -------- */
        if (showing) {
            if (!"show".equals(current))
                return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        /* -------- 空闲 -------- */
        if (!"idles".equals(current))
            return state.setAndContinue(IDLES);

        return PlayState.CONTINUE;
    }



    /* =========================================================== */
    /* -------------------- 数据读/写 Helper -------------------- */
    public BeltMode getMode(ItemStack stack) {
        if (stack == null) {
            return DEFAULT;
        }
        CompoundTag tag = stack.getOrCreateTag();
        String modeName = tag.getString("BeltMode");
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
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putString("BeltMode", mode.name());
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsShowing") ? tag.getBoolean("IsShowing") : false;
    }

    public void setShowing(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsShowing", flag);
    }

    public boolean getActive(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsActive") ? tag.getBoolean("IsActive") : false;
    }

    public void setActive(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsActive", flag);
    }



    public boolean getRelease(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsRelease") ? tag.getBoolean("IsRelease") : false;
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsRelease", flag);
    }

    public boolean getIsTurning(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsTurning") ? tag.getBoolean("IsTurning") : false;
    }

    public void setIsTurning(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsTurning", flag);
    }

    public boolean getIsTransforming(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsTransforming") ? tag.getBoolean("IsTransforming") : false;
    }

    public void setIsTransforming(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsTransforming", flag);
    }

    public boolean getIsPlayingMould(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsPlayingMould") ? tag.getBoolean("IsPlayingMould") : false;
    }

    public void setIsPlayingMould(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsPlayingMould", flag);
    }

    public boolean getIsPlayingMouldB(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsPlayingMouldB") ? tag.getBoolean("IsPlayingMouldB") : false;
    }

    public void setIsPlayingMouldB(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsPlayingMouldB", flag);
    }

    /* ----------------------------------------------------------- */

    /* ===================== 业务方法 ==================== */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || entity.level() == null) {
            return;
        }

        setRelease(stack, true);

        BeltMode mode = getMode(stack);
        String anim1 = "cancel";
        String anim2 = "cancel_s";

        System.out.println(">>> Server send packet: " + anim1 + " and " + anim2);

        // 1. 服务端：把腰带动画名同步给所有追踪者
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            // 发送 cancel 动画
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), anim1, mode), sp);
            // 发送 cancel_s 动画
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), anim2, mode), sp);
        }

        // 2. 客户端：本地线程直接播，不再发包
        if (entity.level().isClientSide) {
            triggerAnim(entity, "controller", anim1);
            triggerAnim(entity, "controller", anim2);
        }
    }

    /**
     * 检查玩家是否持有危险扳机
     */
    public boolean hasHazardTrigger(LivingEntity entity) {
        if (entity == null || !(entity instanceof net.minecraft.world.entity.player.Player)) return false;

        net.minecraft.world.entity.player.Player player = (net.minecraft.world.entity.player.Player) entity;
        // 检查玩家物品栏
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.HazardTrigger) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查玩家是否持有兔子瓶
     */
    public boolean hasRabbitItem(LivingEntity entity) {
        if (entity == null || !(entity instanceof net.minecraft.world.entity.player.Player)) return false;

        net.minecraft.world.entity.player.Player player = (net.minecraft.world.entity.player.Player) entity;
        // 检查玩家物品栏
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.RabbitItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查玩家是否持有坦克瓶
     */
    public boolean hasTankItem(LivingEntity entity) {
        if (entity == null || !(entity instanceof net.minecraft.world.entity.player.Player)) return false;

        net.minecraft.world.entity.player.Player player = (net.minecraft.world.entity.player.Player) entity;
        // 检查玩家物品栏
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.TankItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * 激活危险模式
     */
    public void activateHazardMode(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null) return;

        BeltMode currentMode = getMode(beltStack);
        BeltMode newMode;

        // 根据当前模式切换到对应的危险模式
        switch (currentMode) {
            case DEFAULT:
                newMode = BeltMode.HAZARD_EMPTY;
                break;
            case RT:
                newMode = BeltMode.HAZARD_RT;
                // 延迟2秒播放Super Best Match音效
                // 无论客户端还是服务器端都播放音效
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 2秒延迟
                        // 直接播放音效
                        entity.playSound(ModBossSounds.SUPER_BEST_MATCH.get(), 1.0F, 1.0F);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
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

        // 同步模式变更
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), "idle", newMode), sp);
        }
    }

    /**
     * 停用危险模式
     */
    public void deactivateHazardMode(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null) return;

        BeltMode currentMode = getMode(beltStack);
        BeltMode newMode;

        // 根据当前危险模式切换回对应的普通模式
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

        // 同步模式变更
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), "idle", newMode), sp);
        }
    }

    /* =========================================================== */

    /* -------------------- 其它必要实现 -------------------- */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BuildDriverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
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

    /**
     * 实现基类的腰带装备逻辑
     */
    @Override
    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null) {
            return;
        }

        setShowing(beltStack, true);
        setActive(beltStack, false);
        setRelease(beltStack, false);

        // 同步腰带状态到所有跟踪的玩家
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "show", DEFAULT),
                player);

        // 更新玩家变量
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isBuildDriverEquipped = true;
            variables.syncPlayerVariables(player);
        });

        // 触发动画
        triggerAnim(player, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return;
        }

        if (!(ctx.entity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setActive(stack, false);

        if (le.level() != null && !le.level().isClientSide && le instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "idles", getMode(stack)), sp);
        }

        triggerAnim(le, "controller", "idles");

        // 更新玩家变量
        if (le instanceof ServerPlayer player) {
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.isBuildDriverEquipped = false;
                variables.syncPlayerVariables(player);
            });
        }
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return;
        }
        if (ctx.entity().level() == null || ctx.entity().level().isClientSide()) {
            return;
        }
        if (!(ctx.entity() instanceof ServerPlayer sp)) {
            return;
        }

        // 每 5 秒同步一次，避免频繁刷新
        if (sp.tickCount % 100 == 0) {
            PacketHandler.sendToClient(
                    new BeltAnimationPacket(sp.getId(), "sync_state", getMode(stack)), sp);
        }
    }

    /**
     * 处理玩家右键点击腰带的逻辑
     * 用于移除物品
     */
    public boolean onRightClick(SlotContext ctx, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return false;
        }

        if (!(ctx.entity() instanceof ServerPlayer sp)) {
            return false;
        }

        ItemStack heldItem = sp.getMainHandItem();

        // 检查是否持有满瓶或危险扳机
        boolean isHoldingBottleOrTrigger = heldItem.getItem() instanceof RabbitItem ||
                                          heldItem.getItem() instanceof TankItem ||
                                          heldItem.getItem() instanceof HazardTrigger;

        // 空手持或持有满瓶/危险扳机时处理移除逻辑
        if (heldItem.isEmpty() || isHoldingBottleOrTrigger) {
            BeltMode currentMode = getMode(stack);
            boolean isHazardMode = currentMode.toString().startsWith("HAZARD_");

            // 如果持有物品，移除玩家手中的道具
            if (isHoldingBottleOrTrigger) {
                sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            // 根据当前模式移除对应物品
            switch (currentMode) {
                case RT:
                case HAZARD_RT:
                    // 移除坦克瓶，切换到R模式
                    setMode(stack, isHazardMode ? BeltMode.HAZARD_R : BeltMode.R);
                    break;
                case R:
                case HAZARD_R:
                    // 移除兔子瓶，切换到默认模式
                    setMode(stack, isHazardMode ? BeltMode.HAZARD_EMPTY : BeltMode.DEFAULT);
                    break;
                case T:
                case HAZARD_T:
                    // 移除坦克瓶，切换到默认模式
                    setMode(stack, isHazardMode ? BeltMode.HAZARD_EMPTY : BeltMode.DEFAULT);
                    break;
                case HAZARD_EMPTY:
                    // 移除危险扳机，切换到默认模式
                    setMode(stack, BeltMode.DEFAULT);
                    break;
                default:
                    return false;
            }

            // 触发动画
            triggerAnim(sp, "controller", "cancel");

            // 同步腰带状态
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), "cancel", getMode(stack)), sp);

            return true;
        }

        return false;
    }

    /* -------------------- NBT 同步 -------------------- */
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        tag.putString("BeltMode", getMode(stack).name());
        tag.putBoolean("IsShowing", getShowing(stack));
        tag.putBoolean("IsActive", getActive(stack));
        tag.putBoolean("IsRelease", getRelease(stack));
        tag.putBoolean("IsTurning", getIsTurning(stack));
        tag.putBoolean("IsTransforming", getIsTransforming(stack));
        tag.putBoolean("IsPlayingMould", getIsPlayingMould(stack));
        tag.putBoolean("IsPlayingMouldB", getIsPlayingMouldB(stack));
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt == null) return;
        if (nbt.contains("BeltMode")) setMode(stack, BeltMode.valueOf(nbt.getString("BeltMode")));
        if (nbt.contains("IsShowing")) setShowing(stack, nbt.getBoolean("IsShowing"));
        if (nbt.contains("IsActive")) setActive(stack, nbt.getBoolean("IsActive"));
        if (nbt.contains("IsRelease")) setRelease(stack, nbt.getBoolean("IsRelease"));
        if (nbt.contains("IsTurning")) setIsTurning(stack, nbt.getBoolean("IsTurning"));
        if (nbt.contains("IsTransforming")) setIsTransforming(stack, nbt.getBoolean("IsTransforming"));
        if (nbt.contains("IsPlayingMould")) setIsPlayingMould(stack, nbt.getBoolean("IsPlayingMould"));
        if (nbt.contains("IsPlayingMouldB")) setIsPlayingMouldB(stack, nbt.getBoolean("IsPlayingMouldB"));
    }

    /* -------------------- 动画触发工具 -------------------- */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;

        // 服务端处理：发送动画包到所有跟踪者
        if (!entity.level().isClientSide) {
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(entity).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof BuildDriver));
            BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(DEFAULT);

            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, mode), entity);
        }
    }



    /**
     * 触发玩家作动动画
     */
    public void triggerPlayerAnim(@Nullable LivingEntity entity, String anim) {
        if (entity == null || entity.level() == null) return;

        if (entity.level().isClientSide) {
            // 客户端处理：发送数据包到服务器
            PacketHandler.sendToServer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket(anim, entity.getId(), true, 5, 2000));
        } else {
            // 服务端处理：发送玩家动画包到所有跟踪者和自己
            PacketHandler.sendAnimationToAllTrackingAndSelf(anim, entity.getId(), true, entity, 5, 2000);
        }
    }

    /**
     * 取消玩家作动动画
     */
    public void cancelPlayerAnim(@Nullable LivingEntity entity) {
        if (entity == null || entity.level() == null) return;

        if (entity.level().isClientSide) {
            // 客户端处理：发送取消动画数据包到服务器
            PacketHandler.sendToServer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket(entity.getId()));
        } else {
            // 服务端处理：发送取消动画包到所有跟踪者和自己
            if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
                net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) entity.level();
                PacketHandler.cancelAnimation(entity.getId(), level);
            }
        }
    }
}
