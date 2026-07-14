package com.xiaoshi2022.kamenriderbossyouandme.items.prop;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.greatdragon.GreatDragonRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // ========== 检查副手是否有 Cobra（眼镜蛇满瓶） ==========
        InteractionHand offHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack offHandStack = player.getItemInHand(offHand);

        if (offHandStack.getItem() instanceof Cobra) {
            // ✅ 眼镜蛇 + 伟大龙 合成
            if (getMode(stack) == Mode.EMPTY) {
                // 设置为 NORMAL 模式
                setMode(stack, Mode.NORMAL);

                // ✅ 播放 SHOWS 动画（参考 Dragonfruit 使用 triggerAnim）
                if (!level.isClientSide()) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "shows");
                }

                // 消耗副手的眼镜蛇
                offHandStack.shrink(1);

//                // 播放音效
//                if (!level.isClientSide()) {
//                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
//                            ModBossSounds.SUPER_BEST_MATCH.get(),
//                            SoundSource.PLAYERS, 1.0F, 1.0F);
//
//                }

                player.sendSystemMessage(
                        Component.literal("§a✅ 伟大龙已与眼镜蛇合成！获得 NORMAL 形态！")
                );

                return InteractionResultHolder.success(stack);
            }
        }

        // ========== 检查是否装备了 BuildDriver 且模式为 HAZARD_EMPTY ==========
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player,
                item -> item.getItem() instanceof BuildDriver);

        if (beltOpt.isPresent() && getMode(stack) == Mode.NORMAL) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();

            // ✅ 尝试插入伟大龙到腰带
            if (belt.insertGreatDragon(player, beltStack)) {
                // ✅ 播放 OPEN 动画（参考 Dragonfruit 使用 triggerAnim）
                if (!level.isClientSide()) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "open");
                }

                if (!level.isClientSide()) {
                    player.sendSystemMessage(
                            Component.literal("§a✅ 伟大龙已插入腰带！长按变身键开始融合！")
                    );
                }

                // 消耗伟大龙
                stack.shrink(1);

                return InteractionResultHolder.success(stack);
            }
        }

        // ========== 普通右键：播放 OPEN 动画 ==========
        if (!level.isClientSide()) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "open");
        }

        return InteractionResultHolder.success(stack);
    }

    public void triggerCrossAnim(Player player, int entityId) {
        triggerAnim(player, entityId, "controller", "cross");
    }

    public void triggerShowsAnim(Player player, int entityId) {
        triggerAnim(player, entityId, "controller", "shows");
    }

    public void triggerOpenAnim(Player player, int entityId) {
        triggerAnim(player, entityId, "controller", "open");
    }
}