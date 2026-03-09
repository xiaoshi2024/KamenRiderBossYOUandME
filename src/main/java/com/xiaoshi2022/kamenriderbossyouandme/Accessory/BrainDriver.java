package com.xiaoshi2022.kamenriderbossyouandme.Accessory;

import com.jpigeon.ridebattlelib.api.RiderManager;
import com.jpigeon.ridebattlelib.core.system.network.packet.UnhenshinPacket;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.braindriver.BrainDriverRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
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
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public class BrainDriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ----------------- 动画常量 ----------------- */
    private static final RawAnimation IDLES = RawAnimation.begin().thenLoop("idles");
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation HENSHIN = RawAnimation.begin().thenPlayAndHold("henshin");
    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlayAndHold("cancel");

    /* ------------------------- 标签常量 ------------------------- */
    private static final String TAG_BELT_MODE = "BeltMode";
    private static final String TAG_IS_SHOWING = "IsShowing";
    private static final String TAG_IS_ACTIVE = "IsActive";
    private static final String TAG_IS_HENSHIN = "IsHenshin";
    private static final String TAG_IS_RELEASE = "IsRelease";
    private static final String TAG_IS_EQUIPPED = "IsEquipped";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public enum BeltMode {
        DEFAULT,
        BRAIN
    }

    public BrainDriver(Properties properties) {
        super(properties);

        // 关键修复：注册为同步动画对象
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    /* -------------------- 数据读/写 Helper -------------------- */
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

    public void setEquipped(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_EQUIPPED, flag));
    }

    public BeltMode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return BeltMode.DEFAULT;
        CompoundTag tag = getOrCreateTag(stack);
        String modeName = tag.getString(TAG_BELT_MODE);
        if (!modeName.isEmpty()) {
            try {
                return BeltMode.valueOf(modeName);
            } catch (IllegalArgumentException e) {
                return BeltMode.DEFAULT;
            }
        }
        return BeltMode.DEFAULT;
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_SHOWING);
    }

    public void setShowing(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_SHOWING, flag));
    }

    public boolean getActive(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_ACTIVE);
    }

    public void setActive(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_ACTIVE, flag));
    }

    public boolean getHenshin(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_HENSHIN);
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_HENSHIN, flag));
    }

    public boolean getRelease(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_RELEASE);
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_RELEASE, flag));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主控制器
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationController)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("henshin", HENSHIN)
                .triggerableAnim("cancel", CANCEL));
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putString(TAG_BELT_MODE, mode.name()));

        String modeText = switch (mode) {
            case BRAIN -> "Brain驱动器 - Brain骑士形态";
            default -> "Brain驱动器 - 普通形态";
        };
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(modeText));
    }

    /* -------------- 动画触发方法 -------------- */
// 服务端触发变身动画
    // 服务端触发变身动画
    public void triggerHenshinAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || stack.isEmpty()) return;

        // 先重置其他动画状态
        setRelease(stack, false);
        setShowing(stack, false);
        // 设置变身状态
        setHenshin(stack, true);

        if (entity.level() instanceof ServerLevel serverLevel) {
            long id = GeoItem.getOrAssignId(stack, serverLevel);
            // 触发动画
            this.triggerAnim(entity, id, "controller", "henshin");



            // 添加动画完成后的延迟重置
            serverLevel.getServer().execute(() -> {
                try {
                    // 等待动画播放完成 (1.0833秒 ≈ 1083ms)
                    Thread.sleep(1200);
                    serverLevel.getServer().execute(() -> {
                        // 动画完成后重置状态，进入待机显示
                        setHenshin(stack, false);
                        setShowing(stack, true);


                        // 重新触发待机显示动画
                        if (entity instanceof ServerPlayer serverPlayer) {
                            this.triggerShowAnimation(serverPlayer, stack);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // 服务端触发解除变身动画
    public void triggerCancelAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || stack.isEmpty()) return;

        // 先重置其他动画状态
        setHenshin(stack, false);
        setShowing(stack, false);
        // 最后设置解除状态
        setRelease(stack, true);

        if (entity.level() instanceof ServerLevel serverLevel) {
            long id = GeoItem.getOrAssignId(stack, serverLevel);
            this.triggerAnim(entity, id, "controller", "cancel");


        }
    }

    // 修改动画控制器，添加调试信息
    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) return PlayState.STOP;

        boolean hen = getHenshin(stack);
        boolean release = getRelease(stack);
        boolean show = getShowing(stack);

        AnimationController<?> controller = state.getController();

        // 获取当前动画名称
        String currentAnim = controller.getCurrentAnimation() != null ?
                controller.getCurrentAnimation().animation().name() : "none";

        // 打印调试信息


        // 变身动画 - 最高优先级
        if (hen) {
            // 如果当前不是变身动画，或者变身动画已结束，才重新触发
            if (!"henshin".equals(currentAnim) || controller.hasAnimationFinished()) {

                return state.setAndContinue(HENSHIN);
            }
            return PlayState.CONTINUE;
        }

        // 解除动画
        if (release) {
            if (!"cancel".equals(currentAnim) || controller.hasAnimationFinished()) {

                return state.setAndContinue(CANCEL);
            }
            return PlayState.CONTINUE;
        }

        // 展示动画
        if (show) {
            if (!"show".equals(currentAnim) || controller.hasAnimationFinished()) {

                return state.setAndContinue(SHOW);
            }
            return PlayState.CONTINUE;
        }

        // 待机动画
        if (!"idles".equals(currentAnim)) {

            return state.setAndContinue(IDLES);
        }

        return PlayState.CONTINUE;
    }

    // 服务端触发展示动画
    public void triggerShowAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || stack.isEmpty()) return;

        setShowing(stack, true);

        if (entity.level() instanceof ServerLevel serverLevel) {
            long id = GeoItem.getOrAssignId(stack, serverLevel);
            this.triggerAnim(entity, id, "controller", "show");
        }
    }

    // 客户端触发动画的方法
    public void triggerClientAnim(LivingEntity entity, String animName) {
        if (entity == null) return;

        // 只在客户端触发
        if (entity.level().isClientSide()) {
            // 动画控制器会根据状态自动切换到正确的动画
            // 这里不需要额外触发，只需要确保状态正确即可

        }
    }

    /* -------------- 业务 -------------- */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        triggerCancelAnimation(entity, stack);
    }

    /* -------------- 物品提示 -------------- */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        BeltMode mode = getMode(stack);
        String modeText = switch (mode) {
            case BRAIN -> Component.translatable("tooltip.braindriver.mode.brain").getString();
            default -> Component.translatable("tooltip.braindriver.mode.normal").getString();
        };
        tooltipComponents.add(Component.translatable("tooltip.braindriver.mode", modeText));
    }

    /* -------------- Curio -------------- */
    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        super.onEquip(ctx, prev, stack);
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltEquipped(player, stack);
        }
    }

    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) return;

        setHenshin(beltStack, false);
        setRelease(beltStack, false);
        setShowing(beltStack, true);
        setActive(beltStack, false);
        setEquipped(beltStack, true);

        // 使用新的触发方法
        triggerShowAnimation(player, beltStack);
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) return;
        super.onUnequip(ctx, newStack, stack);
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltUnequipped(player, stack);
        }
    }

    protected void onBeltUnequipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) return;

        setShowing(beltStack, false);
        setRelease(beltStack, false);
        setEquipped(beltStack, false);
        //这里调用解除变身的功能
        PacketDistributor.sendToServer(new UnhenshinPacket(player.getUUID()));
    }

    /* -------------- 客户端渲染器 -------------- */
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BrainDriverRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<BrainDriver> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new BrainDriverRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // 获取当前物品的模式（用于模型渲染）
    public BeltMode getCurrentMode(ItemStack stack) {
        return getMode(stack);
    }
}