package com.xiaoshi2022.kamenriderbossyouandme.Accessory;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.braindriver.BrainDriverRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
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
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlayAndHold("idles");
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
    }

    /* -------------------- 数据读/写 Helper -------------------- */
    // 获取或创建 CompoundTag
    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData == CustomData.EMPTY) {
            return new CompoundTag();
        }
        return customData.copyTag();
    }

    // 保存 CompoundTag
    private static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // 修改 CompoundTag 并保存
    private static void modifyTag(ItemStack stack, Consumer<CompoundTag> action) {
        CompoundTag tag = getOrCreateTag(stack);
        action.accept(tag);
        saveTag(stack, tag);
    }

    public void setEquipped(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_EQUIPPED, flag));
    }

    /**
     * 获取腰带的模式
     */
    public BeltMode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return BeltMode.DEFAULT;
        }
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

    public boolean getHenshin(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_HENSHIN);
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_HENSHIN, flag));
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
    /* ----------------------------------------------------------- */
    
    @Override
    public boolean isFoil(ItemStack stack) {
        // 移除原版附魔发光效果
        return false;
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("henshin", HENSHIN)
                .triggerableAnim("cancel", CANCEL));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        // 添加null检查
        if (stack == null || !(state.getAnimatable() instanceof BrainDriver)) return PlayState.STOP;

        BeltMode mode = getMode(stack);
        boolean show = getShowing(stack);
        boolean release = getRelease(stack);
        boolean hen = getHenshin(stack);
        boolean active = getActive(stack);

        String cur = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        /* -------- 变身序列 -------- */
        if (hen) {
            if (!cur.equals("henshin"))
                return state.setAndContinue(HENSHIN);
            return PlayState.CONTINUE;
        }

        /* -------- 解除变身 -------- */
        if (release) {
            if (!cur.equals("cancel"))
                return state.setAndContinue(CANCEL);

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, BeltMode.DEFAULT);
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        /* -------- 展示/待机 -------- */
        if (show) {
            if (!cur.equals("show")) return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        /* -------- 默认 -------- */
        if (!cur.equals("idles")) return state.setAndContinue(IDLES);
        return PlayState.CONTINUE;
    }

    /* -------------- NBT 工具 -------------- */
    public void setModeAndTriggerHenshin(LivingEntity entity, ItemStack stack, BeltMode mode) {
        setMode(stack, mode);
        triggerAnim(entity, "controller", "henshin");
    }

    /**
     * 设置腰带的模式
     */
    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putString(TAG_BELT_MODE, mode.name()));

        // 更新物品显示名称，使每条腰带在物品栏中显示其模式
        String modeText = switch (mode) {
            case BRAIN -> "Brain驱动器 - Brain骑士形态";
            default -> "Brain驱动器 - 普通形态";
        };

        // 正确的方式：使用 set 方法设置 CUSTOM_NAME 组件
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(modeText));
    }

    /* -------------- 业务 -------------- */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setShowing(stack, false);
        setHenshin(stack, false);

        triggerAnim(entity, "controller", "cancel");
    }

    /* -------------- 工具 -------------- */
    private RawAnimation getAnim(String name) {
        return switch (name) {
            case "show" -> SHOW;
            case "idles" -> IDLES;
            case "henshin" -> HENSHIN;
            case "cancel" -> CANCEL;
            default -> IDLES;
        };
    }

    /* -------------- 物品提示 -------------- */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        // 添加当前腰带形态提示
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
        // 确保实体是ServerPlayer类型
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltEquipped(player, stack);
        }
    }

    /**
     * 实现基类的腰带装备逻辑
     */
    @Override
    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) {
            return;
        }
        
        setHenshin(beltStack, false);
        setRelease(beltStack, false);
        setShowing(beltStack, true);
        setActive(beltStack, false);

        // 触发动画
        triggerAnim(player, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
            return;
        }
        super.onUnequip(ctx, newStack, stack);
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltUnequipped(player, stack);
        }
    }

    /**
     * 实现基类的腰带卸下逻辑
     */
    protected void onBeltUnequipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) {
            return;
        }
        
        setShowing(beltStack, false);
        setRelease(beltStack, false);
        
        // 触发动画
        triggerAnim(player, "controller", "idles");
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
            return;
        }
        if (ctx.entity().level() == null || ctx.entity().level().isClientSide()) {
            return;
        }
        if (!(ctx.entity() instanceof ServerPlayer sp)) {
            return;
        }
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

    /* -------------- 触发工具 -------------- */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;
        // 简化实现，仅在客户端触发本地动画
        if (entity.level().isClientSide()) {
            // 这里可以添加客户端动画触发逻辑
            System.out.println("触发动画: " + anim + " 对于实体: " + entity.getName().getString());
        }
    }

    // 获取当前物品的模式（用于模型渲染）
    public BeltMode getCurrentMode(ItemStack stack) {
        return getMode(stack);
    }

    // 重载方法，用于模型渲染时的默认调用
    public BeltMode getCurrentMode() {
        return BeltMode.DEFAULT;
    }
}
