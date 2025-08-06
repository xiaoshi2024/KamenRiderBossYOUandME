package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BananasEntity extends BlockEntity implements GeoBlockEntity {
    //定义动画
    private static final RawAnimation OPENZIPB = RawAnimation.begin().thenPlayAndHold("openzipb");


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifespan = 3600; // 40 tick lifespan (2 seconds)

    public BananasEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANANAS_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenPlay("openzipb"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BananasEntity entity) {
        if (!level.isClientSide && entity.lifespan-- <= 0) {
            level.removeBlock(pos, false);
        }
    }
}