package com.xiaoshi2022.kamenriderbossyouandme.items;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver;
import com.xiaoshi2022.kamenriderbossyouandme.block.DragonfruitBlock;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.dragonfruit.DragonfruitRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBlocks;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class Dragonfruit extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("start");
    private static final RawAnimation CUT_OPEN = RawAnimation.begin().thenPlay("scatter");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final String TAG_FIRST_CLICK = "first_click";

    public Dragonfruit(Properties properties) {
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

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private DragonfruitRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<Dragonfruit> getGeoItemRenderer() {
                if (renderer == null)
                    renderer = new DragonfruitRenderer();
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        Optional<SlotResult> beltOptional = CurioUtils.findFirstCurio(player,
                item -> item.getItem() instanceof Genesis_driver);

        if (beltOptional.isEmpty()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("请先装备Genesis Driver腰带！"));
            }
            return InteractionResultHolder.success(stack);
        }

        CompoundTag tag = getOrCreateTag(stack);

        // ========== 第一次右键 ==========
        if (!tag.contains(TAG_FIRST_CLICK)) {
            tag.putBoolean(TAG_FIRST_CLICK, true);
            saveTag(stack, tag);

            if (!level.isClientSide()) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "start");

                BlockPos aboveHead = player.blockPosition().above(2);
                if (level.isEmptyBlock(aboveHead)) {
                    level.setBlock(aboveHead,
                            ModBlocks.DRAGONFRUITX_BLOCK.get().defaultBlockState(),
                            Block.UPDATE_ALL);

                    level.playSound(null, aboveHead,
                            ModBossSounds.DRAGONFRUIT_ENERGY.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);
                }
            }

            player.displayClientMessage(Component.literal("§e再次点击装备锁种"), true);

            // ✅ 使用 CONSUME 阻止其他处理器
            return InteractionResultHolder.consume(stack);
        }

        // ========== 第二次右键：通过 NBT 放入锁种 ==========
        if (!level.isClientSide()) {
            ItemStack beltStack = beltOptional.get().stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();

            // ✅ 1. 检查腰带是否已有锁种
            if (belt.hasLockseed(beltStack)) {
                player.displayClientMessage(Component.literal("§c腰带已有锁种！"), true);
                return InteractionResultHolder.success(stack);
            }

            // ✅ 2. 将锁种保存到腰带 NBT
            ItemStack lockseedStack = stack.copy();
            lockseedStack.setCount(1);
            belt.setLockseed(beltStack, lockseedStack);

            // ✅ 3. 设置腰带状态
            belt.setMode(beltStack, Genesis_driver.BeltMode.DRAGONFRUIT);
            belt.setActive(beltStack, true);
            belt.setShowing(beltStack, false);

            // ✅ 4. 消耗道具
            stack.shrink(1);

            // ✅ 5. 清除标记，允许下次使用
            tag.remove(TAG_FIRST_CLICK);
            saveTag(stack, tag);

            // ✅ 6. 待机音效和提示
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModBossSounds.LEMON_LOCKONBY.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            player.displayClientMessage(Component.literal("§a龙果锁种已放入腰带！按变身键完成变身"), true);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate)
                .triggerableAnim("start", OPEN)
                .triggerableAnim("scatter", CUT_OPEN));
    }

    private PlayState predicate(AnimationState<Dragonfruit> dragonfruitAnimationState) {
        if (dragonfruitAnimationState.getController().getCurrentAnimation() != null) {
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}