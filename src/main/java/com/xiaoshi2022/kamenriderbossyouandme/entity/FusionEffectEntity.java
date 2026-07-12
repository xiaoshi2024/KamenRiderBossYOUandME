// entity/FusionEffectEntity.java
package com.xiaoshi2022.kamenriderbossyouandme.entity;

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

    // 服务端和客户端都能使用的默认皮肤路径
    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    // 同步数据
    private static final EntityDataAccessor<String> PLAYER_NAME_1 =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PLAYER_NAME_2 =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PLAYER_NAME_3 =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> CURRENT_ANIMATION =
            SynchedEntityData.defineId(FusionEffectEntity.class, EntityDataSerializers.STRING);

    // 皮肤纹理 (客户端缓存)
    private ResourceLocation[] playerSkins = new ResourceLocation[3];
    private SkinState[] skinStates = new SkinState[3];

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

    /**
     * 从服务端设置皮肤纹理 (网络同步)
     */
    public void setPlayerSkinFromServer(ResourceLocation texture) {
        // 只在客户端执行
        if (this.level().isClientSide) {
            // 根据当前同步的玩家索引设置
            // 注意：需要额外同步玩家索引，或通过其他方式确定
        }
    }

    /**
     * 从服务端设置皮肤状态 (网络同步)
     */
    public void setSkinStateFromServer(SkinState state) {
        // 只在客户端执行
        if (this.level().isClientSide) {
            // 设置皮肤状态
        }
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

    /**
     * 设置玩家皮肤 (由 SkinLoader 调用)
     */
    public void setPlayerSkin(int index, ResourceLocation texture) {
        if (index >= 0 && index < 3) {
            this.playerSkins[index] = texture != null ? texture : DEFAULT_SKIN;
            this.skinStates[index] = texture != null ? SkinState.LOADED : SkinState.FAILED;
            
            String cacheKey = getPlayerName(0) + "_" + getPlayerName(1) + "_" + getPlayerName(2);
            CombinedSkinBuilder.invalidateCache(cacheKey);
        }
    }

    /**
     * 设置皮肤加载状态
     */
    public void setSkinState(int index, SkinState state) {
        if (index >= 0 && index < 3) {
            this.skinStates[index] = state;
        }
    }

    // ========== 动画相关 ==========

    public void setAnimationState(AnimationState state) {
        this.entityData.set(CURRENT_ANIMATION, state.name);
    }

    public AnimationState getAnimationState() {
        String name = this.entityData.get(CURRENT_ANIMATION);
        try {
            return AnimationState.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AnimationState.IDLE;
        }
    }

    public ResourceLocation getEffectTexture() {
        return switch (getAnimationState()) {
            case IDLE -> EffectTextures.BLOOD_TX;
            case FINISH -> EffectTextures.TX_FINLSH;
        };
    }

    private AnimationController<FusionEffectEntity> finishController;

    @Override
    protected void registerAnimationControllers(AnimatableManager.ControllerRegistrar registrar) {

        AnimationController<FusionEffectEntity> idleController = new AnimationController<>(
                this, "idle_controller", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        }
        );

        finishController = new AnimationController<>(
                this, "finish_controller", 0, state -> {
            if (getAnimationState() == AnimationState.FINISH) {
                state.getController().setAnimation(
                        RawAnimation.begin().then("finlsh", Animation.LoopType.HOLD_ON_LAST_FRAME)
                );
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }
        );

        addController(registrar, "idle", idleController);
        addController(registrar, "finish", finishController);
    }

    public void triggerFinish() {
        if (this.level().isClientSide) return;
        setAnimationState(AnimationState.FINISH);
        if (finishController != null) {
            finishController.forceAnimationReset();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && getAnimationState() == AnimationState.FINISH) {
            if (finishController != null && finishController.getAnimationState() == AnimationController.State.STOPPED) {
                this.discard();
            }
        }
        
        if (this.level().isClientSide) {
            tickClient();
        }
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
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("PlayerName1", getPlayerName(0));
        tag.putString("PlayerName2", getPlayerName(1));
        tag.putString("PlayerName3", getPlayerName(2));
    }
}

class EffectTextures {
    public static final ResourceLocation BLOOD_TX =
            ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/entity/bloodtx.png");
    public static final ResourceLocation TX_FINLSH =
            ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/entity/tx_finlsh.png");
}