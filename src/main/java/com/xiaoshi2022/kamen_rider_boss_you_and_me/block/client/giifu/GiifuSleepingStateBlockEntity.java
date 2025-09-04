package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiifuSleepingStateBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HEART_EXTRACTED = RawAnimation.begin().thenLoop("heart_extracted");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean heartExtracted = false;

    public boolean hasHeartExtracted() {
        return heartExtracted;
    }

    public boolean extractHeart() {
        if (!heartExtracted) {
            heartExtracted = true;
            return true;
        }
        return false;
    }

    public GiifuSleepingStateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GIIFU_SLEEPING_STATE_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (level == null) return PlayState.STOP;

            // 检查是否已经抽取了心脏
            boolean heartExtracted = hasHeartExtracted();

            if (heartExtracted) {
                state.setAnimation(HEART_EXTRACTED);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}