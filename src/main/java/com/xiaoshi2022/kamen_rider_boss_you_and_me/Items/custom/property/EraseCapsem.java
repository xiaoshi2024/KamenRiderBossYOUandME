package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.EraseCapsem.EraseCapsemRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class EraseCapsem extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation USE = RawAnimation.begin().thenPlay("use");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    public EraseCapsem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private EraseCapsemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new EraseCapsemRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // 检测玩家back槽位是否装备了KnightInvokerBuckle
            Optional<SlotResult> buckleOpt = CurioUtils.findFirstCurio(serverPlayer, 
                    stack -> stack.getItem() instanceof KnightInvokerBuckle);
            
            boolean canUseOnBelt = false;
            if (buckleOpt.isPresent()) {
                ItemStack buckleStack = buckleOpt.get().stack();
                if (buckleStack.getItem() instanceof KnightInvokerBuckle buckle) {
                    // 只有当腰带处于DEFAULT模式时才能使用胶囊
                    if (buckle.getMode(buckleStack) == KnightInvokerBuckle.BeltMode.DEFAULT) {
                        // 设置腰带模式为NOX
                        buckle.setMode(buckleStack, KnightInvokerBuckle.BeltMode.NOX);
                        
                        // 播放阶段一待机音
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModBossSounds.NOX_A.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        
                        // 触发动画
                        triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverPlayer.serverLevel()), "capuse", "use");
                        
                        // 消耗胶囊
                        ItemStack itemStack = player.getItemInHand(hand);
                        if (!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                        
                        canUseOnBelt = true;
                        return InteractionResultHolder.success(itemStack);
                    }
                }
            }
            
            // 如果没有装备腰带或者腰带不在默认模式，则对着方块右键播放use动画
            if (!canUseOnBelt) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverPlayer.serverLevel()), "capuse", "use");
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "capuse", 20, state -> {
            // 检查是否有正在播放的动画
            if (state.getController().getCurrentAnimation() != null) {
                return PlayState.CONTINUE;
            }
            // 默认播放idle动画
            state.setAnimation(IDLE);
            return PlayState.CONTINUE;
        })
        .triggerableAnim("use", USE)
        .triggerableAnim("idle", IDLE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}