package com.xiaoshi2022.kamenriderbossyouandme.block.client;

import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DragonfruitBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int lifespan = 600;

    public DragonfruitBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModBlockEntities.DRAGONFRUITX_ENTITY.get(), p_155229_, p_155230_);
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

    public static void tick(Level level, BlockPos pos, BlockState state, DragonfruitBlockEntity entity) {
        if (!level.isClientSide && entity.lifespan-- <= 0) {
            level.removeBlock(pos, false);
        }
    }
}