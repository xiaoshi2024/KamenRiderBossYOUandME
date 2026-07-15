package com.xiaoshi2022.kamenriderbossyouandme.items.prop;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.greatdragon.GreatDragonRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class GreatDragon extends Item implements GeoItem {

    private static final RawAnimation CROSS = RawAnimation.begin().thenPlay("cross");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation SHOWS = RawAnimation.begin().thenPlay("shows");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final String TAG_MODE = "Mode";

    public enum Mode {
        NORMAL, EMPTY
    }

    public GreatDragon(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // ==================== NBT 工具 ====================
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

    public Mode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Mode.EMPTY;
        }
        CompoundTag tag = getOrCreateTag(stack);
        String modeName = tag.getString(TAG_MODE);
        if (modeName.isEmpty()) {
            return Mode.EMPTY;
        }
        try {
            return Mode.valueOf(modeName);
        } catch (IllegalArgumentException ex) {
            return Mode.EMPTY;
        }
    }

    public void setMode(ItemStack stack, Mode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = getOrCreateTag(stack);
        tag.putString(TAG_MODE, mode.name());
        saveTag(stack, tag);
    }

    // ==================== GeoItem ====================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("cross", CROSS)
                .triggerableAnim("open", OPEN)
                .triggerableAnim("shows", SHOWS));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof GreatDragon))
            return PlayState.STOP;
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GreatDragonRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<GreatDragon> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new GreatDragonRenderer();
                }
                return renderer;
            }
        });
    }

    // ==================== 动画触发 ====================
    public void triggerCrossAnim(Player player, int entityId) {
        triggerAnim(player, entityId, "controller", "cross");
    }

    public void triggerShowsAnim(Player player, int entityId) {
        triggerAnim(player, entityId, "controller", "shows");
    }

    public void triggerOpenAnim(Player player, int entityId) {
        triggerAnim(player, entityId, "controller", "open");
    }

    // ==================== 主要交互逻辑 ====================
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // ✅ Shift + 右键：取出眼镜蛇
        if (player.isShiftKeyDown()) {
            return handleShiftRightClick(level, player, stack);
        }

        // ========== 正常右键逻辑 ==========

        // 检查副手是否有 Cobra（眼镜蛇满瓶）
        InteractionHand offHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack offHandStack = player.getItemInHand(offHand);

        // ✅ 眼镜蛇 + 伟大龙 合成
        if (offHandStack.getItem() instanceof Cobra) {
            if (getMode(stack) == Mode.EMPTY) {
                setMode(stack, Mode.NORMAL);

                if (!level.isClientSide()) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "shows");
                }

                offHandStack.shrink(1);


                return InteractionResultHolder.success(stack);
            }
        }

        // 检查是否装备了 BuildDriver 且模式为 HAZARD_EMPTY
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player,
                item -> item.getItem() instanceof BuildDriver);

        if (beltOpt.isPresent() && getMode(stack) == Mode.NORMAL) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();

            if (belt.insertGreatDragon(player, beltStack)) {
                if (!level.isClientSide()) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "open");
                }

                stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
        }

        // 普通右键：播放 OPEN 动画
        if (!level.isClientSide()) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "open");
        }

        return InteractionResultHolder.success(stack);
    }

    /**
     * Shift + 右键：从伟大龙中取出眼镜蛇满瓶
     */
    private InteractionResultHolder<ItemStack> handleShiftRightClick(Level level, Player player, ItemStack stack) {
        // 检查伟大龙是否处于 NORMAL 模式
        if (getMode(stack) != Mode.NORMAL) {

            return InteractionResultHolder.fail(stack);
        }

        // ✅ 检查腰带是否已经使用了伟大龙
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player,
                item -> item.getItem() instanceof BuildDriver);

        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            BuildDriver.BeltMode mode = belt.getMode(beltStack);

            // 如果腰带是 HAZARD_EMPTY 或 HAZARD_GD，说明伟大龙已被使用
            if (mode == BuildDriver.BeltMode.HAZARD_EMPTY || mode == BuildDriver.BeltMode.HAZARD_GD) {

                return InteractionResultHolder.fail(stack);
            }
        }

        // 检查背包是否有空位
        if (!player.getInventory().add(new ItemStack(ModItems.COBRA.get()))) {

            return InteractionResultHolder.fail(stack);
        }

        // 将伟大龙设置为 EMPTY 模式
        setMode(stack, Mode.EMPTY);

        // 播放动画
        if (!level.isClientSide()) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "open");
        }


        return InteractionResultHolder.success(stack);
    }
}