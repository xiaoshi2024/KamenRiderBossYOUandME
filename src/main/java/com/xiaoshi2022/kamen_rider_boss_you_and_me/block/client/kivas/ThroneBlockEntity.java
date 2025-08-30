package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity.SeatEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ThroneBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation OCCUPIED = RawAnimation.begin().thenLoop("occupied");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ThroneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THRONE_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (level == null) return PlayState.STOP;

            // 🎯 检查座位上是否有乘客
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class,
                    new AABB(worldPosition).inflate(0.5));

            boolean hasRider = !seats.isEmpty() && seats.get(0).isVehicle();

            if (hasRider) {
                state.setAnimation(OCCUPIED);
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