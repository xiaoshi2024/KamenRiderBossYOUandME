package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class GiifuSleepingStateBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HEART_EXTRACTED = RawAnimation.begin().thenLoop("heart_extracted");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean heartExtracted = false;

    private float accumulatedDamage = 0.0f;

    private boolean kicked = false;          // 本石像是否已被踢爆

    public void addDamage(float amount) {
        if (isKicked()) return;
        accumulatedDamage += amount;

        // 每 20 点伤害降低一次硬度
        float hardnessReduction = accumulatedDamage / 20.0f;
        float remainingHardness = Math.max(3.0f - hardnessReduction, 0.0f);

        if (remainingHardness <= 0.0f) {
            explodeAndRemove();
        }
    }

    private void explodeAndRemove() {
        Level level = this.level;
        if (!(level instanceof ServerLevel srv)) return;

        // 播放爆炸效果（不破坏方块）
        srv.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                1, 0, 0, 0, 1);
        srv.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS, 1.0f, 1.0f);

        // 移除石像
        level.destroyBlock(worldPosition, false);
        this.setKicked(true);
    }

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

    public boolean isKicked() { return kicked; }
    public void setKicked(boolean flag) {
        kicked = flag;
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Kicked", kicked);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        kicked = tag.getBoolean("Kicked");
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