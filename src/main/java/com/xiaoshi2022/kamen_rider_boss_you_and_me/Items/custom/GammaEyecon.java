package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.gamma_eyecon.GammaEyeconRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GammaEyeconEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
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

import java.util.function.Consumer;

public class GammaEyecon extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GammaEyecon(Properties p_41383_) {
        super(p_41383_);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GammaEyeconRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GammaEyeconRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 右键使用眼魔眼魂的逻辑 - 手中右键放生
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            
            // 生成眼魔眼魂实体
            GammaEyeconEntity eyeconEntity = ModEntityTypes.GAMMA_EYECON_ENTITY.get().create(level);
            if (eyeconEntity != null) {
                // 设置实体位置在玩家前方
                Vec3 lookVec = player.getLookAngle();
                double spawnX = player.getX() + lookVec.x * 1.5;
                double spawnY = player.getY() + 1;
                double spawnZ = player.getZ() + lookVec.z * 1.5;
                
                eyeconEntity.setPos(spawnX, spawnY, spawnZ);
                level.addFreshEntity(eyeconEntity);
                
                // 播放释放音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_EYE_LAUNCH, player.getSoundSource(), 1.0F, 1.0F);
                
                // 消耗一个物品
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                
                return InteractionResultHolder.success(stack);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::idleAnimController));
    }

    private PlayState idleAnimController(AnimationState<GammaEyecon> animationState) {
        animationState.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}