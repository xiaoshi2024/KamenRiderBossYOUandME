package com.xiaoshi2022.kamenriderbossyouandme.Accessory;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.builddriver.BuildDriverRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.BloodHandler;
import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionTagManager;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionTeleportManager;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.SoundStopPacket;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModEntitys;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver.BeltMode.DEFAULT;

public class BuildDriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    // 添加一个 Map 来跟踪每个腰带的循环任务
    private static final Map<ItemStack, Runnable> rtSoundTasks = new HashMap<>();

    // ==================== 动画常量 ====================
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlay("idles");
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlay("show");
    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlay("cancel");
    private static final RawAnimation CANCEL_S = RawAnimation.begin().thenPlay("cancel_s");
    private static final RawAnimation TURN = RawAnimation.begin().thenPlay("turn");
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation MOULD = RawAnimation.begin().thenPlay("mould");
    private static final RawAnimation MOULD_B = RawAnimation.begin().thenPlay("mould_b");

    // ==================== NBT 标签 ====================
    private static final String TAG_BELT_MODE = "BeltMode";
    private static final String TAG_IS_SHOWING = "IsShowing";
    private static final String TAG_IS_ACTIVE = "IsActive";
    private static final String TAG_IS_RELEASE = "IsRelease";
    private static final String TAG_IS_TURNING = "IsTurning";
    private static final String TAG_IS_TRANSFORMING = "IsTransforming";
    private static final String TAG_IS_SHAKING = "IsShaking";
    private static final String TAG_HAS_GREAT_DRAGON = "HasGreatDragon";
    private static final String TAG_FUSION_EFFECT_ID = "FusionEffectId";

    // ==================== 枚举 ====================
    public enum BeltMode {
        DEFAULT, RT, R, T,
        HAZARD_EMPTY, HAZARD_RT, HAZARD_R, HAZARD_T, HAZARD_K, HAZARD_KR, HAZARD_RESSYA, HAZARD_RT_MOULD,
        HAZARD_GD
    }

    // ==================== 缺失的方法 ====================

    /**
     * 获取是否正在变身中
     */
    public boolean getIsTransforming(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getOrCreateTag(stack).getBoolean(TAG_IS_TRANSFORMING);
    }

    /**
     * 设置是否正在变身中
     */
    public void setIsTransforming(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_TRANSFORMING, flag));
    }

    /**
     * 开始解除动画
     */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || stack.isEmpty() || entity.level() == null) return;

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

    // ==================== 构造 ====================
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BuildDriver(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // ==================== NBT 工具 ====================
    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData == CustomData.EMPTY ? new CompoundTag() : customData.copyTag();
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

    // ==================== GeoItem ====================
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
                );
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty() || !(state.getAnimatable() instanceof BuildDriver))
            return PlayState.STOP;

        boolean isTurning = getIsTurning(stack);
        boolean showing = getShowing(stack);
        boolean rel = getRelease(stack);
        String current = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        if (isTurning) {
            if (current.equals("turn")) return PlayState.CONTINUE;
            return state.setAndContinue(TURN);
        }

        if (rel) {
            if (!current.equals("cancel")) return state.setAndContinue(CANCEL);
            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, DEFAULT);
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        if (showing) {
            if (!"show".equals(current)) return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        if (!"idles".equals(current)) return state.setAndContinue(IDLES);
        return PlayState.CONTINUE;
    }

    // ==================== 数据访问器 ====================
    public BeltMode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return DEFAULT;
        String modeName = getOrCreateTag(stack).getString(TAG_BELT_MODE);
        try { return modeName.isEmpty() ? DEFAULT : BeltMode.valueOf(modeName); }
        catch (IllegalArgumentException e) { return DEFAULT; }
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        modifyTag(stack, tag -> tag.putString(TAG_BELT_MODE, mode.name()));
    }

    public boolean getShowing(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_IS_SHOWING);
    }

    public void setShowing(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_SHOWING, flag));
    }

    public boolean getActive(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_IS_ACTIVE);
    }

    public void setActive(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_ACTIVE, flag));
    }

    public boolean getRelease(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_IS_RELEASE);
    }

    public void setRelease(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_RELEASE, flag));
    }

    public boolean getIsTurning(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_IS_TURNING);
    }

    public void setIsTurning(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_TURNING, flag));
    }

    public boolean getIsShaking(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_IS_SHAKING);
    }

    public void setIsShaking(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_SHAKING, flag));
    }

    public boolean getHasGreatDragon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_HAS_GREAT_DRAGON);
    }

    public void setHasGreatDragon(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_HAS_GREAT_DRAGON, flag));
    }

    public int getFusionEffectId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return getOrCreateTag(stack).getInt(TAG_FUSION_EFFECT_ID);
    }

    public void setFusionEffectId(ItemStack stack, int id) {
        modifyTag(stack, tag -> tag.putInt(TAG_FUSION_EFFECT_ID, id));
    }

    public void removeFusionEffectId(ItemStack stack) {
        modifyTag(stack, tag -> tag.remove(TAG_FUSION_EFFECT_ID));
    }

    // ==================== 腰带业务逻辑 ====================

    /**
     * 激活危险模式
     */
    public void activateHazardMode(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null || beltStack.isEmpty()) return;

        BeltMode currentMode = getMode(beltStack);
        BeltMode newMode;
        switch (currentMode) {
            case DEFAULT: newMode = BeltMode.HAZARD_EMPTY; break;
            case RT: newMode = BeltMode.HAZARD_RT; break;
            case R: newMode = BeltMode.HAZARD_R; break;
            case T: newMode = BeltMode.HAZARD_T; break;
            default: return;
        }

        setMode(beltStack, newMode);
        setShowing(beltStack, false);
        setActive(beltStack, true);

        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToTrackingAndSelf(sp,
                    new BeltAnimationPacket(sp.getId(), "idle", "builddriver", newMode.name()));
        }
    }

    /**
     * 插入伟大龙
     */
    public boolean insertGreatDragon(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null || beltStack.isEmpty()) return false;
        if (!(entity instanceof Player player)) return false;

        if (getMode(beltStack) != BeltMode.HAZARD_EMPTY) {
            if (!entity.level().isClientSide()) {
                entity.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§c需要腰带处于 §6HAZARD_EMPTY §c模式！")
                );
            }
            return false;
        }

        if (getHasGreatDragon(beltStack)) {
            if (!entity.level().isClientSide()) {
                entity.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c腰带已装载伟大龙！"));
            }
            return false;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon) {
                var greatDragon = (com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon) stack.getItem();
                if (greatDragon.getMode(stack) == com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon.Mode.NORMAL) {
                    stack.shrink(1);
                    setHasGreatDragon(beltStack, true);
                    setMode(beltStack, BeltMode.HAZARD_GD);
                    setShowing(beltStack, false);
                    setActive(beltStack, true);

                    if (entity instanceof Player p) {
                        p.playSound(ModBossSounds.SUPER_BEST_MATCH.get(), 1.0F, 1.0F);
                    }

                    if (!entity.level().isClientSide()) {
                        entity.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("§a✅ 伟大龙已装载！长按变身键开始融合！")
                        );
                    }
                    return true;
                }
            }
        }

        if (!entity.level().isClientSide()) {
            entity.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§c背包中没有伟大龙满瓶！")
            );
        }
        return false;
    }


    // 在 BuildDriver 中添加
    private static final String TAG_SHAKING_ACTIVE = "ShakingActive";

    public void setShakingActive(ItemStack stack, boolean flag) {
        modifyTag(stack, tag -> tag.putBoolean(TAG_SHAKING_ACTIVE, flag));
    }

    public boolean getShakingActive(ItemStack stack) {
        return stack != null && !stack.isEmpty() && getOrCreateTag(stack).getBoolean(TAG_SHAKING_ACTIVE);
    }

    /**
     * 开始摇动 - 播放 turn 动画，创建融合特效
     */
    public void startShaking(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null || beltStack.isEmpty()) return;
        if (getMode(beltStack) != BeltMode.HAZARD_GD) {
            if (!entity.level().isClientSide()) {
                entity.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c当前不是伟大龙危险模式！"));
            }
            return;
        }

        if (getIsShaking(beltStack)) {
            KamenRiderBossYOUandME.LOGGER.debug("已经开始摇动，跳过重复调用");
            return;
        }

        setIsShaking(beltStack, true);
        setIsTurning(beltStack, true);

        KamenRiderBossYOUandME.LOGGER.info("🔴 startShaking 被调用！");

        // 停止音效
        stopSuperBestMatchSound(entity);

        // 玩家摇动动画
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            triggerPlayerAnim(sp, "turn");
        }

        // 腰带 turn 动画
        triggerAnim(entity, "controller", "turn");

        // 创建融合特效
        createFusionEffect(entity, beltStack);

        // ✅ 播放音效
        if (entity instanceof Player player) {
            // GREAT_DRAGON 播放一次
            BloodHandler.playSound(player, ModBossSounds.GREAT_DRAGON.get());

            // ✅ 启动 RT_BY 循环播放（每 10 ticks 播放一次）
            startRTSoundLoop(player, beltStack);
        }
    }

    private void startRTSoundLoop(Player player, ItemStack beltStack) {
        KamenRiderBossYOUandME.LOGGER.info("🎵 startRTSoundLoop 被调用！");
        stopRTSoundLoop(beltStack);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                KamenRiderBossYOUandME.LOGGER.info("🔄 RT_BY 循环执行中...");

                if (!getIsShaking(beltStack) || !player.isAlive()) {
                    rtSoundTasks.remove(beltStack);
                    KamenRiderBossYOUandME.LOGGER.info("🛑 RT_BY 循环停止");
                    return;
                }

                if (player instanceof ServerPlayer sp) {
                    KamenRiderBossYOUandME.LOGGER.info("🔊 播放 RT_BY 音效");
                    // 使用 World 的 playSound
                    sp.level().playSound(
                            null,
                            sp.getX(), sp.getY(), sp.getZ(),
                            ModBossSounds.RT_BY.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }

                // 继续循环
                RideBattleAPI.scheduleTicks(5, this);
            }
        };

        rtSoundTasks.put(beltStack, task);
        // 第一次延迟 5 ticks 执行
        RideBattleAPI.scheduleTicks(5, task);
    }

    /**
     * 停止 RT_BY 循环播放
     */
    private void stopRTSoundLoop(ItemStack beltStack) {
        Runnable task = rtSoundTasks.remove(beltStack);
        // 任务会在下次执行时自行停止
    }

    public void stopShaking(LivingEntity entity, ItemStack beltStack) {
        if (entity == null || beltStack == null || beltStack.isEmpty()) {
            KamenRiderBossYOUandME.LOGGER.warn("⚠️ stopShaking: entity 或 beltStack 为空");
            return;
        }

        KamenRiderBossYOUandME.LOGGER.info("🔴 stopShaking 被调用, isShaking={}", getIsShaking(beltStack));

        // ✅ 停止 RT_BY 循环
        stopRTSoundLoop(beltStack);

        // 重置腰带状态（先清理）
        setIsShaking(beltStack, false);
        setIsTurning(beltStack, false);
        setShowing(beltStack, true);
        setActive(beltStack, false);

        if (!(entity instanceof Player player) || entity.level().isClientSide()) {
            return;
        }

        // ✅ 1. 播放 GD_HENSHIN 音效
        BloodHandler.playSound(player, ModBossSounds.GD_HENSHIN.get());
        KamenRiderBossYOUandME.LOGGER.info("🎵 播放 GD_HENSHIN 音效");

        //20tick=1秒

        // ✅ 2. 延迟 70tick后播放特效完成动画
        RideBattleAPI.scheduleTicks(70, () -> {
            if (!player.isAlive()) return;

            KamenRiderBossYOUandME.LOGGER.info("🎬 音效播放完毕，触发特效完成动画");

            // 获取融合特效实体
            int effectId = getFusionEffectId(beltStack);
            FusionEffectEntity effect = null;
            if (effectId != 0) {
                var e = player.level().getEntity(effectId);
                if (e instanceof FusionEffectEntity) {
                    effect = (FusionEffectEntity) e;
                }
            }

            // 播放特效完成动画
            if (effect != null) {
                effect.triggerFinish();
                KamenRiderBossYOUandME.LOGGER.info("🎬 触发特效 finish 动画");
            } else {
                KamenRiderBossYOUandME.LOGGER.warn("⚠️ 找不到融合特效实体");
            }

            // 停止玩家的摇动动画
            triggerPlayerAnim(player, "idle");
            KamenRiderBossYOUandME.LOGGER.info("🎬 停止玩家摇动动画 -> idle");

            // 清除特效 ID
            removeFusionEffectId(beltStack);

            // ✅ 3. 再延迟 75tick 后执行变身（穿戴盔甲）
            RideBattleAPI.scheduleTicks(75, () -> {
                if (player.isAlive() && !RideBattleAPI.isTransformed(player)) {
                    KamenRiderBossYOUandME.LOGGER.info("🎬 特效动画完成，穿戴盔甲!");
                    BloodHandler.executeHenshin(player, beltStack);
                }
            });
        });
    }

    /**
     * 重置腰带（解除变身后）
     */
    public void resetBelt(ItemStack beltStack) {
        if (beltStack == null || beltStack.isEmpty()) return;
        setMode(beltStack, DEFAULT);
        setActive(beltStack, false);
        setShowing(beltStack, false);
        setRelease(beltStack, true);
        setHasGreatDragon(beltStack, false);
        setIsTurning(beltStack, false);
        setIsShaking(beltStack, false);
        removeFusionEffectId(beltStack);
    }

    // ==================== 私有辅助方法 ====================

    private void stopSuperBestMatchSound(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath(
                com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID,
                "super_best_match"
        );
        PacketHandler.sendToClient(player, new SoundStopPacket(player.getUUID(), soundId));
        PacketHandler.sendToAllTracking(player, new SoundStopPacket(player.getUUID(), soundId));
    }

    private void createFusionEffect(LivingEntity entity, ItemStack beltStack) {
        if (!(entity instanceof ServerPlayer player)) return;

        // 检查是否已存在
        int existingId = getFusionEffectId(beltStack);
        if (existingId != 0) {
            var existing = player.level().getEntity(existingId);
            if (existing instanceof FusionEffectEntity) return;
            removeFusionEffectId(beltStack);
        }

        // 获取融合者
        List<Player> targets = FusionTagManager.getNearbyFusionTargets(player, 10.0);
        String name1 = targets.size() >= 1 ? targets.get(0).getName().getString() : "Steve";
        String name2 = targets.size() >= 2 ? targets.get(1).getName().getString() : "Alex";
        String name3 = targets.size() >= 3 ? targets.get(2).getName().getString() : "Steve";

        if (targets.size() < 3) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§e⚠ 融合者不足 (当前: " + targets.size() + "/3)，使用默认史蒂夫！"),
                    true
            );
        } else {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6⚡ 融合开始！3名融合者已被传送！"),
                    true
            );
        }

        // 创建特效
        FusionEffectEntity effect = new FusionEffectEntity(ModEntitys.FUSION_EFFECT.get(), player.level());
        effect.setPos(player.getX(), player.getY(), player.getZ());
        effect.setPlayerNames(name1, name2, name3);
        player.level().addFreshEntity(effect);

        setFusionEffectId(beltStack, effect.getId());

        // 传送融合者
        for (int i = 0; i < 3 && i < targets.size(); i++) {
            FusionTeleportManager.teleportToHell(targets.get(i), player);
        }

        KamenRiderBossYOUandME.LOGGER.info("融合特效已创建: 玩家={}, 融合者={}, {}, {}",
                player.getName().getString(), name1, name2, name3);
    }

    // ==================== 动画工具 ====================

    public void triggerPlayerAnim(LivingEntity entity, String anim) {
        if (!(entity instanceof ServerPlayer sp)) return;
        PacketHandler.sendToClient(sp, new BYAnimationPacket(sp.getUUID(), anim, 5));
        PacketHandler.sendToAllTracking(sp, new BYAnimationPacket(sp.getUUID(), anim, 5));
    }

    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (!(entity instanceof ServerPlayer sp)) return;
        Optional<SlotResult> beltOptional = CurioUtils.findFirstCurio(sp, item -> item.getItem() instanceof BuildDriver);
        BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(DEFAULT);
        PacketHandler.sendToTrackingAndSelf(sp,
                new BeltAnimationPacket(sp.getId(), anim, "builddriver", mode.name()));
    }

    // ==================== Curios ====================

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
        if (player == null || beltStack == null || beltStack.isEmpty()) return;

        setShowing(beltStack, true);
        setActive(beltStack, false);
        setRelease(beltStack, false);
        setIsShaking(beltStack, false);
        setIsTurning(beltStack, false);

        PacketHandler.sendToTrackingAndSelf(player,
                new BeltAnimationPacket(player.getId(), "show", "builddriver", DEFAULT.name()));
        triggerAnim(player, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) return;
        if (!(ctx.entity() instanceof LivingEntity le)) return;

        setShowing(stack, false);
        setActive(stack, false);
        setIsShaking(stack, false);
        setIsTurning(stack, false);

        if (!le.level().isClientSide && le instanceof ServerPlayer sp) {
            PacketHandler.sendToTrackingAndSelf(sp,
                    new BeltAnimationPacket(sp.getId(), "idles", "builddriver", getMode(stack).name()));
        }
        triggerAnim(le, "controller", "idles");
    }

    // ==================== 工具方法 ====================

    public static ItemStack getBeltStack(ServerPlayer player) {
        if (player == null) return ItemStack.EMPTY;
        return CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BuildDriver)
                .map(SlotResult::stack).orElse(ItemStack.EMPTY);
    }

    public boolean hasUsedHazardTrigger(ItemStack beltStack) {
        if (beltStack == null || beltStack.isEmpty()) return false;
        BeltMode mode = getMode(beltStack);
        return mode == BeltMode.HAZARD_GD || mode == BeltMode.HAZARD_EMPTY;
    }

    public ItemStack getGreatDragonItem(ItemStack beltStack) {
        if (!getHasGreatDragon(beltStack)) return ItemStack.EMPTY;
        return new ItemStack(ModItems.GREAT_DRAGON.get(), 1);
    }
}