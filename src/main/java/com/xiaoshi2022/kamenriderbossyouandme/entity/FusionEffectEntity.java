package com.xiaoshi2022.kamenriderbossyouandme.entity;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.skin.CombinedSkinBuilder;
import com.xiaoshi2022.kamenriderbossyouandme.client.skin.SkinCache;
import com.xiaoshi2022.kamenriderbossyouandme.client.skin.SkinLoader;
import com.xiaoshi2022.kamenriderbossyouandme.client.skin.SkinState;
import com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib.entity.BaseKamenRiderEffectEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.*;

public class FusionEffectEntity extends BaseKamenRiderEffectEntity {

    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    private static final EntityDataAccessor<String> PLAYER_NAME_1 =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PLAYER_NAME_2 =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PLAYER_NAME_3 =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> CURRENT_ANIMATION =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_FINISHING =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.BOOLEAN);

    private ResourceLocation[] playerSkins = new ResourceLocation[3];
    private SkinState[] skinStates = new SkinState[3];

    private boolean finishTriggered = false;
    private int finishTickCount = 0;
    private static final int FINISH_DURATION_TICKS = 80; // 4秒

    // ✅ 客户端缓存状态，用于检测变化
    private boolean clientIsFinishing = false;

    public enum AnimationState {
        IDLE("idle"),
        FINISH("finlsh");

        public final String name;
        AnimationState(String name) { this.name = name; }
    }

    public FusionEffectEntity(EntityType<?> entityType, Level level) {
        super(entityType, level, "fusion", "effect");
        for (int i = 0; i < 3; i++) {
            playerSkins[i] = DEFAULT_SKIN;
            skinStates[i] = SkinState.NOT_LOADED;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLAYER_NAME_1, "");
        builder.define(PLAYER_NAME_2, "");
        builder.define(PLAYER_NAME_3, "");
        builder.define(CURRENT_ANIMATION, "idle");
        builder.define(IS_FINISHING, false);
    }

    public void setPlayerNames(String name1, String name2, String name3) {
        this.entityData.set(PLAYER_NAME_1, name1 != null ? name1 : "");
        this.entityData.set(PLAYER_NAME_2, name2 != null ? name2 : "");
        this.entityData.set(PLAYER_NAME_3, name3 != null ? name3 : "");

        if (this.level().isClientSide) {
            loadAllSkins();
        }
    }

    public String getPlayerName(int index) {
        return switch (index) {
            case 0 -> this.entityData.get(PLAYER_NAME_1);
            case 1 -> this.entityData.get(PLAYER_NAME_2);
            case 2 -> this.entityData.get(PLAYER_NAME_3);
            default -> "";
        };
    }

    public String[] getPlayerNames() {
        return new String[]{
                this.entityData.get(PLAYER_NAME_1),
                this.entityData.get(PLAYER_NAME_2),
                this.entityData.get(PLAYER_NAME_3)
        };
    }

    private void loadAllSkins() {
        for (int i = 0; i < 3; i++) {
            loadSkinForPlayer(i);
        }
    }

    private void loadSkinForPlayer(int index) {
        String name = getPlayerName(index);
        if (name == null || name.isEmpty()) {
            playerSkins[index] = DEFAULT_SKIN;
            skinStates[index] = SkinState.LOADED;
            return;
        }

        ResourceLocation cached = SkinCache.get(name);
        if (cached != null) {
            playerSkins[index] = cached;
            skinStates[index] = SkinState.LOADED;
            return;
        }

        skinStates[index] = SkinState.LOADING;
        SkinLoader.loadSkinAsync(this, name, index);
    }

    public ResourceLocation getPlayerSkin(int index) {
        if (index < 0 || index >= 3) return null;
        return playerSkins[index];
    }

    public SkinState getSkinState(int index) {
        if (index < 0 || index >= 3) return SkinState.NOT_LOADED;
        return skinStates[index];
    }

    public boolean isSkinLoaded(int index) {
        return index >= 0 && index < 3 && skinStates[index] == SkinState.LOADED;
    }

    public boolean areAllSkinsLoaded() {
        for (int i = 0; i < 3; i++) {
            if (skinStates[i] != SkinState.LOADED) return false;
        }
        return true;
    }

    public void setPlayerSkin(int index, ResourceLocation texture) {
        if (index >= 0 && index < 3) {
            this.playerSkins[index] = texture != null ? texture : DEFAULT_SKIN;
            this.skinStates[index] = texture != null ? SkinState.LOADED : SkinState.FAILED;

            String cacheKey = getPlayerName(0) + "_" + getPlayerName(1) + "_" + getPlayerName(2);
            CombinedSkinBuilder.invalidateCache(cacheKey);
        }
    }

    public void setSkinState(int index, SkinState state) {
        if (index >= 0 && index < 3) {
            this.skinStates[index] = state;
        }
    }

    // ========== 动画相关 ==========

    public void setAnimationState(AnimationState state) {
        this.entityData.set(CURRENT_ANIMATION, state.name);
        this.entityData.set(IS_FINISHING, state == AnimationState.FINISH);
    }

    public AnimationState getAnimationState() {
        String name = this.entityData.get(CURRENT_ANIMATION);
        try {
            return AnimationState.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AnimationState.IDLE;
        }
    }

    public boolean isFinishing() {
        return this.entityData.get(IS_FINISHING);
    }

    public ResourceLocation getEffectTexture() {
        return switch (getAnimationState()) {
            case IDLE -> EffectTextures.BLOOD_TX;
            case FINISH -> EffectTextures.TX_FINLSH;
        };
    }

    @Override
    protected void registerAnimationControllers(AnimatableManager.ControllerRegistrar registrar) {

        // ✅ IDLE 动画控制器 - 当不是完成状态时循环播放
        AnimationController<FusionEffectEntity> idleController = new AnimationController<>(
                this, "idle_controller", 0, state -> {
            // ✅ 如果正在完成，停止 idle
            if (isFinishing()) {
                return PlayState.STOP;
            }
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        }
        );

        // ✅ FINISH 动画控制器 - 播放完成动画
        AnimationController<FusionEffectEntity> finishController = new AnimationController<>(
                this, "finish_controller", 0, state -> {
            if (isFinishing()) {
                // ✅ 播放 finlsh 动画并保持最后一帧
                state.getController().setAnimation(
                        RawAnimation.begin().then("finlsh", Animation.LoopType.HOLD_ON_LAST_FRAME)
                );
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }
        );

        registrar.add(idleController);
        registrar.add(finishController);
    }

    /**
     * 触发完成动画
     */
    public void triggerFinish() {
        if (this.level().isClientSide) return;

        // 防止重复触发
        if (finishTriggered) return;
        finishTriggered = true;

        // ✅ 设置状态为 FINISH
        setAnimationState(AnimationState.FINISH);

        // ✅ 强制同步数据到客户端
        this.entityData.set(IS_FINISHING, true);
        this.entityData.set(CURRENT_ANIMATION, AnimationState.FINISH.name);

        com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.LOGGER.info("🔥 触发 Fusion 完成动画! isFinishing={}", isFinishing());
    }

    @Override
    public void tick() {
        super.tick();

        // ✅ 客户端检测状态变化
        if (this.level().isClientSide) {
            boolean currentFinishing = isFinishing();
            if (currentFinishing != clientIsFinishing) {
                clientIsFinishing = currentFinishing;
                if (currentFinishing) {
                    // ✅ 状态变为 FINISH，强制刷新动画
                    KamenRiderBossYOUandME.LOGGER.info("🎬 客户端检测到 FINISH 状态，触发完成动画!");
                    // 强制重置动画控制器
                    this.forceAnimationReset();
                }
            }
        }

        // ✅ 服务端逻辑：检查动画是否播放完毕
        if (!this.level().isClientSide && isFinishing()) {
            finishTickCount++;

            // ✅ 播放完 finish 动画后（等待足够时间）移除实体
            if (finishTickCount >= FINISH_DURATION_TICKS) {
                this.discard();
                com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.LOGGER.info("✅ Fusion 特效完成，已移除");
            }
        }

        // ✅ 客户端逻辑
        if (this.level().isClientSide) {
            tickClient();
        }
    }

    // ✅ 强制重置动画（客户端调用）
    @OnlyIn(Dist.CLIENT)
    public void forceAnimationReset() {
        // 通过 GeckoLib 的方式重置动画
        // 这里通过重新注册控制器来强制刷新
        // 实际上 GeckoLib 会自动处理状态变化
    }

    private int skinRetryCounter = 0;

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        skinRetryCounter++;

        for (int i = 0; i < 3; i++) {
            String name = getPlayerName(i);
            SkinState state = getSkinState(i);

            if (name != null && !name.isEmpty()) {
                if (state == SkinState.NOT_LOADED) {
                    ResourceLocation cached = SkinCache.get(name);
                    if (cached != null) {
                        setPlayerSkin(i, cached);
                    } else {
                        skinStates[i] = SkinState.LOADING;
                        SkinLoader.loadSkinAsync(this, name, i);
                    }
                } else if (state == SkinState.FAILED && skinRetryCounter % 200 == 0) {
                    skinStates[i] = SkinState.LOADING;
                    SkinLoader.loadSkinAsync(this, name, i);
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String name1 = tag.getString("PlayerName1");
        String name2 = tag.getString("PlayerName2");
        String name3 = tag.getString("PlayerName3");
        setPlayerNames(name1, name2, name3);

        // ✅ 读取完成状态
        if (tag.contains("IsFinishing")) {
            boolean finishing = tag.getBoolean("IsFinishing");
            if (finishing) {
                setAnimationState(AnimationState.FINISH);
                finishTriggered = true;
            }
        }
    }

    // ========== 网络同步方法 ==========

    public void setPlayerSkinFromServer(ResourceLocation texture) {
        if (!this.level().isClientSide()) return;
        if (texture == null) return;

        for (int i = 0; i < 3; i++) {
            if (skinStates[i] == SkinState.NOT_LOADED || skinStates[i] == SkinState.LOADING) {
                setPlayerSkin(i, texture);
                KamenRiderBossYOUandME.LOGGER.debug("📥 从服务端同步皮肤: index={}, texture={}", i, texture);
                break;
            }
        }
    }

    public void setSkinStateFromServer(SkinState state) {
        if (!this.level().isClientSide()) return;
        if (state == null) return;

        for (int i = 0; i < 3; i++) {
            if (skinStates[i] == SkinState.NOT_LOADED || skinStates[i] == SkinState.LOADING) {
                setSkinState(i, state);
                KamenRiderBossYOUandME.LOGGER.debug("📥 从服务端同步皮肤状态: index={}, state={}", i, state);
                break;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("PlayerName1", getPlayerName(0));
        tag.putString("PlayerName2", getPlayerName(1));
        tag.putString("PlayerName3", getPlayerName(2));
        tag.putBoolean("IsFinishing", isFinishing());
    }
}

class EffectTextures {
    public static final ResourceLocation BLOOD_TX =
            ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/entity/bloodtx.png");
    public static final ResourceLocation TX_FINLSH =
            ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/entity/tx_finlsh.png");
}