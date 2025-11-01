package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class AnotherDenlinerEntity extends Entity implements GeoAnimatable {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation DEPART = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation ARRIVE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // 保存实体的相关数据
    private List<Map<String, String>> savedEntityData = new ArrayList<>();
    private boolean hasSavedEntities = false;
    private boolean isDeparting = false;
    private boolean isArriving = false;
    private int animationTicks = 0;
    private int maxLifespan = 1200; // 最大生命周期（约60秒）
    private int lifeTicks = 0;
    private UUID summoningPlayerId = null;
    
    // 调试标记
    private static final boolean DEBUG = true;
    
    // 目标维度名称 - 使用武器模组的维度
    private static final String TARGET_DIMENSION = "kamen_rider_weapon_craft:the_desertof_time";

    public AnotherDenlinerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        
        // 添加以下设置来隐藏实体ID
        this.setCustomNameVisible(false);
        this.setCustomName(null);
        
        // 添加调试日志
        if (DEBUG && !level.isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity created with ID: {}", this.getId());
        }
    }

    public AnotherDenlinerEntity(Level level) {
        this(ModEntityTypes.ANOTHER_DENLINER.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        hasSavedEntities = tag.getBoolean("HasSavedEntities");
        isDeparting = tag.getBoolean("IsDeparting");
        isArriving = tag.getBoolean("IsArriving");
        animationTicks = tag.getInt("AnimationTicks");
        
        // 读取保存的实体数据
        int entityCount = tag.getInt("EntityCount");
        savedEntityData.clear();
        for (int i = 0; i < entityCount; i++) {
            Map<String, String> entityData = new HashMap<>();
            entityData.put("type", tag.getString("EntityType_" + i));
            entityData.put("health", String.valueOf(tag.getDouble("EntityHealth_" + i)));
            if (tag.hasUUID("SummoningPlayer")) {
                summoningPlayerId = tag.getUUID("SummoningPlayer");
            }
            savedEntityData.add(entityData);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("HasSavedEntities", hasSavedEntities);
        tag.putBoolean("IsDeparting", isDeparting);
        tag.putBoolean("IsArriving", isArriving);
        tag.putInt("AnimationTicks", animationTicks);
        
        // 保存实体数据
        tag.putInt("EntityCount", savedEntityData.size());
        for (int i = 0; i < savedEntityData.size(); i++) {
            Map<String, String> entityData = savedEntityData.get(i);
            tag.putString("EntityType_" + i, entityData.getOrDefault("type", ""));
            tag.putDouble("EntityHealth_" + i, Double.parseDouble(entityData.getOrDefault("health", "0.0")));
        }
        
        if (summoningPlayerId != null) {
            tag.putUUID("SummoningPlayer", summoningPlayerId);
        }
        
        // 保存生命周期
        tag.putInt("LifeTicks", lifeTicks);
    }

    public void saveEntities(Player summoningPlayer, Entity... entities) {
        if (!level().isClientSide) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: saveEntities called with {} entities", 
                        this.getId(), entities != null ? entities.length : 0);
            }
            
            // 添加null检查，避免空指针异常
            this.summoningPlayerId = summoningPlayer != null ? summoningPlayer.getUUID() : null;
            
            // 清空之前的数据
            savedEntityData.clear();
            
            int savedCount = 0;
            for (Entity entity : entities) {
                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    // 保存实体类型和健康值等关键数据
                    Map<String, String> entityData = new HashMap<>();
                    entityData.put("type", Objects.requireNonNull(entity.getType().getDescriptionId()));
                    entityData.put("health", String.valueOf(mob.getHealth()));
                    savedEntityData.add(entityData);
                    savedCount++;
                    
                    // 移除原始实体
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    
                    if (DEBUG) {
                        kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Saved entity: {}, health: {}", 
                                this.getId(), entity.getType().getDescriptionId(), mob.getHealth());
                    }
                }
            }
            
            hasSavedEntities = !savedEntityData.isEmpty();
            
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: saveEntities completed, hasSavedEntities={}, entity count={}", 
                        this.getId(), hasSavedEntities, savedCount);
            }
            
            if (hasSavedEntities) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: hasSavedEntities is true, about to start depart animation", this.getId());
                }
                // 开始离开动画
                startDepartAnimation();
                
                // 延迟传送
                scheduleTeleportToDimension();
            } else {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: hasSavedEntities is false, skipping animation and teleport", this.getId());
                }
            }
        }
    }

    private void startDepartAnimation() {
        // 添加详细日志，记录状态变化
        if (DEBUG && !level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: startDepartAnimation called, previous state: isDeparting={}, hasSavedEntities={}", 
                    this.getId(), this.isDeparting, this.hasSavedEntities);
        }
        
        // 强制设置状态
        this.isDeparting = true;
        this.animationTicks = 0;
        
        if (DEBUG && !level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Depart animation started, new state: isDeparting={}, animationTicks={}", 
                    this.getId(), this.isDeparting, this.animationTicks);
        }
    }

    // 传送延迟计时器
    private int teleportDelayTicks = 0;
    private boolean teleportScheduled = false;
    
    private void scheduleTeleportToDimension() {
        // 确保传送逻辑正确执行
        if (!level().isClientSide() && hasSavedEntities) {
            // 不需要再次调用startDepartAnimation，因为在saveEntities中已经调用过了
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Teleport scheduled with 4 seconds delay", this.getId());
            }
            // 设置传送延迟标志和计时器
            teleportScheduled = true;
            teleportDelayTicks = 80; // 4秒 = 80tick
        }
    }

    private void teleportToDesertOfTime() {
        if (!level().isClientSide() && hasSavedEntities) {
            ServerLevel currentLevel = (ServerLevel) level();
            ServerLevel targetLevel = null;
            boolean targetDimensionFound = false;
            
            // 尝试获取目标维度
            for (ServerLevel level : currentLevel.getServer().getAllLevels()) {
                String dimensionName = level.dimension().location().toString();
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Checking dimension: {}", dimensionName);
                }
                if (dimensionName.equals(TARGET_DIMENSION)) {
                    targetLevel = level;
                    targetDimensionFound = true;
                    break;
                }
            }
            
            // 添加额外的日志
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("Target dimension found: {}, Dimension name: {}", 
                        targetDimensionFound, targetLevel != null ? targetLevel.dimension().location().toString() : "null");
            }
            
            // 如果找到目标维度且不同于当前维度，进行传送
            if (targetDimensionFound && currentLevel != targetLevel) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Teleporting to dimension: {}", 
                            this.getId(), targetLevel.dimension().location());
                }
                
                try {
                    // 直接改变维度
                    this.changeDimension(targetLevel);
                    // 设置到达位置 - 使用随机位置而不是固定的生成点
                    this.setPos(
                            -1980 + random.nextInt(3960), // X坐标范围
                            100, // Y坐标固定高度
                            -1980 + random.nextInt(3960)  // Z坐标范围
                    );
                    // 开始到达动画
                    startArriveAnimation();
                } catch (Exception e) {
                    if (DEBUG) {
                        kamen_rider_boss_you_and_me.LOGGER.error("AnotherDenlinerEntity[{}]: Error during dimension change: {}", 
                                this.getId(), e.getMessage());
                    }
                    e.printStackTrace();
                    // 发生错误时，开始到达动画
                    startArriveAnimation();
                }
            } else {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.warn("AnotherDenlinerEntity[{}]: Target dimension not found or already in target dimension: {}", 
                            this.getId(), TARGET_DIMENSION);
                }
                
                // 如果找不到目标维度或已在目标维度，开始到达动画
                startArriveAnimation();
            }
        }
    }

    private void startArriveAnimation() {
        this.isArriving = true;
        this.isDeparting = false;
        this.animationTicks = 0;
        
        if (DEBUG && !level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Starting arrive animation", this.getId());
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            // 无论是否手持骑士通票，玩家右键点击都可以传送回主世界
            teleportToOverworld(player);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }
    
    /**
     * 传送玩家和电班列回主世界
     */
    private void teleportToOverworld(Player player) {
        if (!level().isClientSide()) {
            ServerLevel currentLevel = (ServerLevel) level();
            ServerLevel overworld = currentLevel.getServer().getLevel(Level.OVERWORLD);
            
            if (overworld != null && currentLevel != overworld) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Teleporting to overworld with player: {}", 
                            this.getId(), player.getName().getString());
                }
                
                try {
                    // 重要：在传送到主世界前清空保存的实体数据，避免后续重新生成怪物
                    // 这是解决被拉回时之沙漠的关键
                    savedEntityData.clear();
                    hasSavedEntities = false;
                    
                    // 设置主世界的传送位置（随机但合理）
                    double spawnX = player.getX() + random.nextDouble() * 20 - 10;
                    double spawnZ = player.getZ() + random.nextDouble() * 20 - 10;
                    double spawnY = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)spawnX, (int)spawnZ) + 1;
                    
                    // 传送玩家到主世界
                    if (player instanceof ServerPlayer) {
                        ((ServerPlayer) player).teleportTo(overworld, spawnX, spawnY, spawnZ, player.getYRot(), player.getXRot());
                    }
                    
                    // 传送电班列
                    this.changeDimension(overworld);
                    this.setPos(spawnX, spawnY, spawnZ);
                    
                    // 开始到达动画
                    startArriveAnimation();
                    
                } catch (Exception e) {
                    if (DEBUG) {
                        kamen_rider_boss_you_and_me.LOGGER.error("AnotherDenlinerEntity[{}]: Error teleporting to overworld: {}", 
                                this.getId(), e.getMessage());
                    }
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 延迟释放保存的实体
     */
    private void scheduleEntitiesRelease() {
        if (!level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level();
            // 使用 ServerLevel 的 scheduleTick 方法，传入方块位置作为标识
            BlockPos tickPos = this.blockPosition();
            serverLevel.scheduleTick(tickPos, serverLevel.getBlockState(tickPos).getBlock(), 40, TickPriority.NORMAL);
        }
    }
    


    public void releaseEntities(Player player) {
        if (!level().isClientSide() && hasSavedEntities) {
            ServerLevel serverLevel = (ServerLevel) level();
            
            for (Map<String, String> entityData : savedEntityData) {
                String entityTypeStr = entityData.getOrDefault("type", "");
                double health = Double.parseDouble(entityData.getOrDefault("health", "0.0"));
                
                // 根据保存的类型重新生成实体
                if (entityTypeStr.contains("time_jacker")) {
                    TimeJackerEntity timeJacker = ModEntityTypes.TIME_JACKER.get().create(serverLevel);
                    if (timeJacker != null) {
                        // 设置位置和健康值
                        timeJacker.setPos(player.getX() + 1, player.getY(), player.getZ());
                        timeJacker.setHealth((float) health);
                        serverLevel.addFreshEntity(timeJacker);
                        
                        if (DEBUG) {
                            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Released TimeJackerEntity at position: {}", 
                                    this.getId(), timeJacker.position());
                        }
                    }
                } else if (entityTypeStr.contains("another_den_o")) {
                    Another_Den_o anotherDenO = ModEntityTypes.ANOTHER_DEN_O.get().create(serverLevel);
                    if (anotherDenO != null) {
                        // 设置位置和健康值
                        anotherDenO.setPos(player.getX() - 1, player.getY(), player.getZ());
                        anotherDenO.setHealth((float) health);
                        serverLevel.addFreshEntity(anotherDenO);
                        
                        if (DEBUG) {
                            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Released Another_Den_o at position: {}", 
                                    this.getId(), anotherDenO.position());
                        }
                    }
                }
            }
            
            // 清空保存的实体数据
            savedEntityData.clear();
            hasSavedEntities = false;
            
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: All entities released, clearing data", this.getId());
            }
            
            // 班列完成任务后移除自身
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
    
    public void releaseEntitiesAutomatically() {
        if (!level().isClientSide && hasSavedEntities) {
            ServerLevel serverLevel = (ServerLevel) level();
            
            for (Map<String, String> entityData : savedEntityData) {
                String entityTypeStr = entityData.getOrDefault("type", "");
                double health = Double.parseDouble(entityData.getOrDefault("health", "0.0"));
                
                // 根据保存的类型重新生成实体
                if (entityTypeStr.contains("time_jacker")) {
                    TimeJackerEntity timeJacker = ModEntityTypes.TIME_JACKER.get().create(serverLevel);
                    if (timeJacker != null) {
                        // 设置位置和健康值 - 使用电班列的位置作为参考
                        timeJacker.setPos(this.getX() + 1, this.getY(), this.getZ());
                        timeJacker.setHealth((float) health);
                        serverLevel.addFreshEntity(timeJacker);
                        
                        if (DEBUG) {
                            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Automatically released TimeJackerEntity at position: {}", 
                                    this.getId(), timeJacker.position());
                        }
                    }
                } else if (entityTypeStr.contains("another_den_o")) {
                    Another_Den_o anotherDenO = ModEntityTypes.ANOTHER_DEN_O.get().create(serverLevel);
                    if (anotherDenO != null) {
                        // 设置位置和健康值 - 使用电班列的位置作为参考
                        anotherDenO.setPos(this.getX() - 1, this.getY(), this.getZ());
                        anotherDenO.setHealth((float) health);
                        serverLevel.addFreshEntity(anotherDenO);
                        
                        if (DEBUG) {
                            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Automatically released Another_Den_o at position: {}", 
                                    this.getId(), anotherDenO.position());
                        }
                    }
                }
            }
            
            // 清空保存的实体数据
            savedEntityData.clear();
            hasSavedEntities = false;
            
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: All entities automatically released in target dimension, clearing data", this.getId());
            }
            
            // 班列完成任务后移除自身
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public void tick() {
        // 检查实体是否已经被标记为删除
        if (this.isRemoved()) {
            return;
        }

        super.tick();
        
        lifeTicks++;
        animationTicks++;
        
        // 添加移动逻辑 - 缓慢向前移动
        if (!isDeparting && !isArriving) {
            // 以缓慢的速度向前移动
            this.setPos(this.getX() + 0.05D * Math.cos(this.getYRot() * Math.PI / 180.0D), 
                        this.getY(), 
                        this.getZ() + 0.05D * Math.sin(this.getYRot() * Math.PI / 180.0D));
            
            // 破坏前方的方块
            if (!level().isClientSide()) {
                // 计算前方方块的位置（向前看3格）
                double yawRad = this.getYRot() * Math.PI / 180.0D;
                int forwardX = (int)(this.getX() + 3.0D * Math.cos(yawRad));
                int forwardZ = (int)(this.getZ() + 3.0D * Math.sin(yawRad));
                int[] yPositions = {(int)this.getY(), (int)(this.getY() + 1), (int)(this.getY() + 2)}; // 检查多个高度
                
                // 破坏前方的方块
                for (int y : yPositions) {
                    BlockPos blockPos = new BlockPos(forwardX, y, forwardZ);
                    BlockState blockState = level().getBlockState(blockPos);
                    
                    // 只破坏普通方块，不破坏基岩等不可破坏的方块
                    if (!blockState.isAir() && blockState.canBeReplaced() && blockState.getDestroySpeed(level(), blockPos) >= 0) {
                        level().destroyBlock(blockPos, true); // 第二个参数表示是否掉落方块
                        
                        if (DEBUG) {
                            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Broke block at position: {}", 
                                    this.getId(), blockPos);
                        }
                    }
                }
            }
        }
        
        // 处理传送延迟逻辑
        if (!level().isClientSide() && teleportScheduled && hasSavedEntities && isDeparting) {
            if (teleportDelayTicks > 0) {
                teleportDelayTicks--;
                if (DEBUG && teleportDelayTicks % 20 == 0) {
                    kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Teleport delay countdown: {} ticks", 
                            this.getId(), teleportDelayTicks);
                }
            } else {
                // 延迟结束，执行传送
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Delay complete, executing teleport", this.getId());
                }
                teleportToDesertOfTime();
                // 重置传送状态
                teleportScheduled = false;
            }
        }
        
        // 调试：显示实体位置和状态
        if (DEBUG && lifeTicks % 40 == 0 && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}] tick: pos={}, hasSavedEntities={}, isDeparting={}, isArriving={}, lifeTicks={}", 
                    this.getId(), this.position(), hasSavedEntities, isDeparting, isArriving, lifeTicks);
        }
        
        // 检查生命周期
        if (lifeTicks > maxLifespan && !this.level().isClientSide()) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Max lifespan reached, discarding", this.getId());
            }
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        
        // 递增动画计时器
        if (isDeparting) {
            animationTicks++;
        }
        
        // 自动检测并修复状态不一致：如果有实体但没有开始离开动画
        if (hasSavedEntities && !isDeparting && !isArriving && !level().isClientSide()) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: State inconsistency detected! hasSavedEntities={} but isDeparting={}, forcing depart animation", 
                        this.getId(), hasSavedEntities, isDeparting);
            }
            // 强制启动离开动画
            startDepartAnimation();
        }
        
        // 动画控制
        if (isDeparting && animationTicks > 40) { // 2秒后结束离开动画
            isDeparting = false;
            // 移除直接传送调用，因为在传送延迟逻辑中已经会触发teleportToDesertOfTime()
            // teleportToDesertOfTime(); // 此调用已移除以避免重复传送
        }
        
        // 在到达动画期间释放实体（适用于所有维度）
        if (!level().isClientSide() && hasSavedEntities && isArriving && animationTicks > 30) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("AnotherDenlinerEntity[{}]: Releasing saved entities during arrival animation", this.getId());
            }
            releaseEntitiesAutomatically();
        }
        
        if (isArriving && animationTicks > 40) { // 2秒后结束到达动画
            isArriving = false;
        }
        
        // 添加粒子效果
        if (this.level().isClientSide()) {
            if (isDeparting || isArriving) {
                // 动画期间播放更多粒子
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.SMOKE, 
                            this.getX() + (random.nextDouble() - 0.5) * 3.0, 
                            this.getY() + random.nextDouble() * 2.0,
                            this.getZ() + (random.nextDouble() - 0.5) * 1.0,
                            0.0D, 0.1D, 0.0D);
                }
            } else if (hasSavedEntities) {
                // 有实体时的普通粒子效果
                this.level().addParticle(ParticleTypes.END_ROD, 
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        0.0D, 0.05D, 0.0D);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public boolean hasSavedEntities() {
        return hasSavedEntities;
    }
    
    private PlayState predicate(AnimationState<AnotherDenlinerEntity> event) {
        // 检查实体是否已经被标记为删除
        if (this.isRemoved()) {
            return PlayState.STOP;
        }
        
        if (isDeparting) {
            event.getController().setAnimation(DEPART);
        } else if (isArriving) {
            event.getController().setAnimation(ARRIVE);
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return this.tickCount;
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }

    protected void doPush(Entity entity) {
    }

    protected void pushEntities() {
    }
    
    @Override
    public boolean isNoGravity() {
        return true; // 禁用重力
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // 允许碰撞，使玩家可以与实体交互
    }

    @Override
    public boolean isPickable() {
        return true; // 允许被拾取/选择，使玩家可以右键点击实体
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public Component getCustomName() {
        return null;
    }

    public Component getName() {
        return Component.empty();
    }

    public Component getDisplayName() {
        return Component.empty();
    }
}