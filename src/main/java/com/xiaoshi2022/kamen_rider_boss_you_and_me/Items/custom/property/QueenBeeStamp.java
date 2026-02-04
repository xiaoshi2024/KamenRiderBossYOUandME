package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.queenbeestamp.QueenBeeStampRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class QueenBeeStamp extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation SEAL = RawAnimation.begin().thenPlay("seal");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 用于存储当前渲染的物品栈的静态ThreadLocal
    private static final ThreadLocal<ItemStack> CURRENT_RENDER_STACK = new ThreadLocal<>();

    public QueenBeeStamp(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 设置当前渲染的物品栈
    public static void setCurrentRenderStack(ItemStack stack) {
        CURRENT_RENDER_STACK.set(stack);
    }

    // 清除当前渲染的物品栈
    public static void clearCurrentRenderStack() {
        CURRENT_RENDER_STACK.remove();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private QueenBeeStampRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new QueenBeeStampRenderer();
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查玩家是否装备了安息日驱动器
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                curios -> curios.findFirstCurio(item -> item.getItem() instanceof WeekEndriver)
        );

        if (beltOptional.isPresent()) {
            // 如果玩家装备了安息日驱动器
            if (!stack.getOrCreateTag().contains("first_click")) {
                // 第一次右键点击
                stack.getOrCreateTag().putBoolean("first_click", true);

                // 播放idle动画
                if (level instanceof ServerLevel serverLevel) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "idle");
                }

                // 播放女王蜂印章的右键音
                player.playSound(ModBossSounds.QUNEN_BEE.get(), 1.0F, 1.0F);

                player.displayClientMessage(Component.literal("再次点击装备女王蜂印章"), true);
                return InteractionResultHolder.success(stack);
            } else {
                // 第二次右键点击 - 将印章插入腰带
                // 检查玩家是否已经装备了印章
                ItemStack beltStack = beltOptional.get().stack();
                WeekEndriver belt = (WeekEndriver) beltStack.getItem();
                if (belt.getMode(beltStack) != WeekEndriver.BeltMode.DEFAULT) {
                    player.sendSystemMessage(Component.literal("您的腰带中已装有印章，请先解除变身！"));
                    return InteractionResultHolder.success(stack);
                }

                // 消耗印章
                stack.shrink(1);

                // 更新腰带为女王蜂形态
                WeekEndriver beltx = (WeekEndriver) beltStack.getItem();
                beltx.setMode(beltStack, WeekEndriver.BeltMode.QUEEN_BEE);

                // ===== 关键修复：设置必要的状态 =====
                beltx.setHenshin(beltStack, true);      // 设置henshin状态为true
                beltx.setShowing(beltStack, false);     // 不显示show动画
                beltx.setActive(beltStack, true);       // 设置激活状态

                // 播放腰带变身待机音（在服务器端播放，让所有玩家都能听到）
                if (!level.isClientSide) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                            ModBossSounds.QUEENBE_BY.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                // 获取PlayerVariables实例并设置状态
                if (!level.isClientSide) {
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    variables.queenBee_ready = true;
                    variables.syncPlayerVariables(player);

                    // 触发WeekEndriver的henshin-a动画
                    // 这里需要直接调用startHenshinAnimation方法，而不是triggerAnim
                    beltx.startHenshinAnimation(player, beltStack);
                    
                    // 播放玩家动画queenbeea
                    if (player instanceof ServerPlayer sp) {
                        PacketHandler.sendAnimationToAllTrackingAndSelf(
                                Component.literal("queenbeea"),
                                player.getId(),
                                true,
                                sp
                        );
                    }
                    
                    // 发送客户端提示消息
                    player.sendSystemMessage(Component.literal("女王蜂印章已装载！按变身键变身"));
                }
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        } else {
            // 如果玩家没有装备安息日驱动器，则播放idle动画
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "idle");
            }
            player.displayClientMessage(Component.literal("需要装备安息日驱动器才能使用女王蜂印章！"), true);
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, state -> PlayState.STOP)
                .triggerableAnim("idle", IDLE)
                .triggerableAnim("seal", SEAL));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}