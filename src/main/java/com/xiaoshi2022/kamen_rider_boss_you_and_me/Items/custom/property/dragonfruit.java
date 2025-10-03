package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.dragonfruit.dragonfruitRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class dragonfruit extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("start");
    private static final RawAnimation CUT_OPEN = RawAnimation.begin().thenPlay("scatter");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public dragonfruit(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private dragonfruitRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new dragonfruitRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查玩家是否装备了Genesis Driver腰带
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver)
        );

        if (beltOptional.isPresent()) {
            // 如果玩家装备了Genesis Driver腰带
            if (!stack.getOrCreateTag().contains("first_click")) {
                // 第一次右键点击
                stack.getOrCreateTag().putBoolean("first_click", true);

                // 播放open动画
                if (!level.isClientSide()) {
                    // 在服务器端触发动画
                    triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "start");
                }

                // 在玩家头顶生成特效方块
                BlockPos aboveHead = player.blockPosition().above(2);
                if (level.isEmptyBlock(aboveHead)) {
                    level.setBlock(aboveHead,
                            ModBlocks.DRAGONFRUITX_BLOCK.get().defaultBlockState(),
                            Block.UPDATE_ALL);

                    // 播放音效
                    level.playSound(null, aboveHead,
                            ModSounds.OPENDLOCK.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);
                    level.playSound(null, aboveHead,
                            ModBossSounds.DRAGONFRUIT_ENERGY.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);
                }

                player.displayClientMessage(Component.literal("再次点击装备锁种"), true);
                return InteractionResultHolder.success(stack);
            } else {
                // 第二次右键点击 - 将锁种插入腰带
                if (!level.isClientSide) {
                    // 检查玩家是否已经装备了其他锁种
                    if (com.xiaoshi2022.kamen_rider_boss_you_and_me.util.BeltUtils.hasActiveLockseed(player)) {
                        player.sendSystemMessage(Component.literal("您已经装备了其他锁种，请先解除变身！"));
                        return InteractionResultHolder.success(stack);
                    }
                    
                    // 播放LOCK ON音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.LEMON_LOCKONBY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 消耗锁种
                    stack.shrink(1);
                    //更新腰带为dragonfruit形态
                    ItemStack beltStack = beltOptional.get().stack();
                    Genesis_driver belt = (Genesis_driver) beltStack.getItem();
                    belt.setMode(beltStack, Genesis_driver.BeltMode.DRAGONFRUIT);
                    // 获取PlayerVariables实例并设置状态
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    variables.dragonfruit_ready = true;
                    variables.syncPlayerVariables(player); // 同步变量到客户端
                }
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        } else {
            // 如果玩家没有装备Genesis Driver腰带，提示装备
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("请先装备Genesis Driver腰带！"));
            }
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate)
                .triggerableAnim("start", OPEN)
                .triggerableAnim("scatter", CUT_OPEN));
    }


    private PlayState predicate(AnimationState<dragonfruit> dragonfruitAnimationState) {
        // 检查是否有正在播放的动画
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
