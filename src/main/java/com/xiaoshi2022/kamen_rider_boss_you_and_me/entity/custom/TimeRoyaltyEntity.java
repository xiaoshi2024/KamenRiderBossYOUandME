package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TimeRoyaltyEntity extends TimeJackerEntity {
    // 标记是否已经使用过异类表盘
    private boolean hasUsedAnotherWatch = false;
    
    // 绑定的异类骑士类型ID（用于确保每个时间王族只绑定一个特定的异类骑士）
    private String boundAnotherRiderType = null;
    
    // 保存当前形态状态
    private boolean isInAnotherForm = false;
    
    // 安全状态检测相关
    private int safeTimer = 0;
    private static final int SAFE_TIME_REQUIRED = 200; // 10秒安全时间后变回人形
    private static final int THREAT_CHECK_RANGE = 15; // 威胁检测范围
    
    // 保存原始属性值，用于变回人形时恢复
    private double originalHealth = 50.0D;
    private double originalDamage = 5.0D;
    private double originalSpeed = 0.6D;
    
    // 重写父类方法提供实体属性
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D) // 生命值高于时劫者
                .add(Attributes.MOVEMENT_SPEED, 0.6D) // 移动速度更快
                .add(Attributes.ATTACK_DAMAGE, 5.0D) // 攻击力更高
                .add(Attributes.FOLLOW_RANGE, 20.0D); // 更远的跟踪范围
    }
    
    public TimeRoyaltyEntity(EntityType<? extends TimeJackerEntity> p_35958_, Level p_35959_) {
        super((EntityType<TimeJackerEntity>) p_35958_, p_35959_);
        
        // 设置默认名称为时间王族
        this.setCustomName(Component.literal("时间王族"));
        
        // 设置时间王族特有的属性
        this.setHealth(50.0F);
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        
        // 确保实体被添加到世界时总是以人类形态开始
        // 清除可能存在的变身状态标记
        this.isInAnotherForm = false;
        if (this.getPersistentData().contains("IsInAnotherForm")) {
            this.getPersistentData().remove("IsInAnotherForm");
        }
        
        // 当实体被添加到世界时，给予更高级的异类表盘
        if (!this.level().isClientSide) {
            this.equipAnotherWatch();
        }
    }
    
    // 为自己装备高级异类表盘
    private void equipAnotherWatch() {
        try {
            // 尝试使用反射获取ModItems中的aiziowc物品实例
            Class<?> modItemsClass = Class.forName("com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems");
            java.lang.reflect.Field watchField = modItemsClass.getDeclaredField("AIZIOWC");
            watchField.setAccessible(true);
            Item watchItem = (Item) watchField.get(null);
            
            // 创建物品栈
            ItemStack watchStack = new ItemStack(watchItem);
            
            // 设置使用次数为0，确保可以使用
            aiziowc.setUseCount(watchStack, 0);
            
            // 装备到主手
            this.setItemSlot(EquipmentSlot.MAINHAND, watchStack);
        } catch (Exception e) {
            // 如果反射失败，尝试使用备用方式
            // 获取所有注册的物品，查找aiziowc类型的物品
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item instanceof aiziowc) {
                    ItemStack watchStack = new ItemStack(item);
                    aiziowc.setUseCount(watchStack, 0);
                    this.setItemSlot(EquipmentSlot.MAINHAND, watchStack);
                    break;
                }
            }
        }
    }
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // 添加基本目标
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new FloatGoal(this));
    }
    
    // 时间减速相关字段
    private Map<LivingEntity, Integer> slowedEntities = new HashMap<>(); // 存储被减速的实体及其减速时间
    private int timeSlowCooldown = 0; // 时间减速冷却时间（tick）
    private static final int TIME_SLOW_COOLDOWN = 80; // 4秒冷却时间
    private static final int TIME_SLOW_DURATION = 80; // 减速持续时间4秒
    
    // 用于检测是否受伤的字段
    private float lastHealth = -1.0F;
    
    // 变身冷却时间
    private int transformationCooldown = 0;
    private static final int TRANSFORMATION_COOLDOWN = 60; // 3秒冷却
    
    @Override
    public void tick() {
        super.tick();
        
        // 确保实体始终以人类形态开始，不读取NBT中的变身状态
        // 保留其他必要的数据同步
        this.isInAnotherForm = false;
        if (this.getPersistentData().contains("IsInAnotherForm")) {
            this.getPersistentData().remove("IsInAnotherForm");
        }
        
        if (this.getPersistentData().contains("AnotherRiderType")) {
            this.boundAnotherRiderType = this.getPersistentData().getString("AnotherRiderType");
        }
        
        // 从NBT同步是否使用过异类表盘的状态
        if (this.getPersistentData().contains("HasUsedAnotherWatch")) {
            this.hasUsedAnotherWatch = this.getPersistentData().getBoolean("HasUsedAnotherWatch");
        }
        
        // 非客户端执行逻辑
        if (!this.level().isClientSide()) {
            // 初始化lastHealth
            if (lastHealth < 0) {
                lastHealth = this.getHealth();
            }
            
            // 更新变身冷却
            if (transformationCooldown > 0) {
                transformationCooldown--;
            }
            
            // 如果是人类形态
            if (!isInAnotherForm) {
                // 检测是否受到伤害
                checkForDamage();
                
                // 更新冷却时间
                if (timeSlowCooldown > 0) {
                    timeSlowCooldown--;
                }
                
                // 更新减速实体的时间并解除减速
                updateSlowedEntities();
                
                // 检查是否有敌对实体靠近，如果有则使用时间减速能力
                if (timeSlowCooldown == 0) {
                    detectAndSlowHostiles();
                }
                
                // 检查威胁，必要时主动变身
                checkThreatAndTransform();
            } else {
                // 异类形态下检测安全状态
                checkSafetyAndRevert();
            }
        }
    }
    
    // 检查是否受到伤害的方法（替代hurt方法重写）
    private void checkForDamage() {
        float currentHealth = this.getHealth();
        
        // 如果生命值减少且没有在变身冷却中，说明受到了伤害
        if (currentHealth < lastHealth && transformationCooldown == 0 && !isInAnotherForm) {
            // 当被攻击时，变身成为绑定的异类骑士
            transformIntoAnotherRider();
        }
        
        // 更新记录的生命值
        lastHealth = currentHealth;
    }
    
    // 检查威胁并在必要时主动变身
    private void checkThreatAndTransform() {
        if (transformationCooldown > 0 || isInAnotherForm) {
            return; // 有冷却或已在变身状态
        }
        
        // 检查附近是否有敌对实体
        List<LivingEntity> nearbyThreats = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(THREAT_CHECK_RANGE),
                entity -> {
                    if (entity instanceof Player || entity instanceof Monster) {
                        // 检查是否具有攻击意向（如玩家手持武器或敌对生物）
                    return (entity instanceof Mob mob && mob.getTarget() == this) || 
                           (entity instanceof Player player && !player.getMainHandItem().isEmpty());
                    }
                    return false;
                }
        );
        
        // 如果有威胁且没有使用过异类表盘，绑定一个异类骑士类型
        if (!nearbyThreats.isEmpty()) {
            transformIntoAnotherRider();
        }
    }
    
    // 变身成为异类骑士的方法 - 参考VillagerEvents.java的转换机制
    // 变身成为异类骑士的方法 - 实际替换实体
    private void transformIntoAnotherRider() {
        if (isInAnotherForm || transformationCooldown > 0 || this.level().isClientSide() || hasUsedAnotherWatch) {
            return;
        }

        // 如果还没有绑定特定的异类骑士类型，默认使用Another_Decade
        if (boundAnotherRiderType == null) {
            boundAnotherRiderType = "Another_Decade";
        }

        try {
            // 保存时间王族的完整NBT数据
            CompoundTag timeRoyaltyTag = new CompoundTag();
            this.save(timeRoyaltyTag);

            // 创建新的异类骑士实体
            Another_Decade anotherRider = new Another_Decade(ModEntityTypes.ANOTHER_DECADE.get(), this.level());

            // 复制位置和旋转
            anotherRider.copyPosition(this);
            anotherRider.setYHeadRot(this.getYHeadRot());
            anotherRider.setYBodyRot(this.yBodyRot);

            // 传递重要数据
            anotherRider.setCustomName(this.getCustomName());
            anotherRider.setHealth((float) (this.getHealth() * 1.5)); // 变身时增加生命值

            // 在异类骑士的NBT中保存原始时间王族数据，用于变回
            CompoundTag storedData = new CompoundTag();
            storedData.put("TimeRoyaltyData", timeRoyaltyTag);
            storedData.putString("OriginalType", this.getEncodeId());
            anotherRider.getPersistentData().put("StoredTimeRoyalty", storedData);

            // 移除原实体并添加新实体
            this.level().addFreshEntity(anotherRider);
            this.discard();

            // 标记已经使用过异类表盘
            hasUsedAnotherWatch = true;
            // 保存到NBT以便在重新加载时保持状态
            this.getPersistentData().putBoolean("HasUsedAnotherWatch", hasUsedAnotherWatch);
            
            // 在聊天框中显示变身消息
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.getPlayers(p -> true).forEach(player -> {
                    player.displayClientMessage(Component.literal("时间王族变身为" + boundAnotherRiderType + "！"), true);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 检查安全状态并在安全时变回人形
    private void checkSafetyAndRevert() {
        if (this.level().isClientSide()) return;
        
        // 检查附近是否有威胁
        boolean isThreatPresent = !this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(THREAT_CHECK_RANGE),
                entity -> {
                    if (entity instanceof Player || entity instanceof Monster) {
                        return (entity instanceof Mob && ((Mob)entity).getTarget() == this) ||
                               (entity instanceof Player && !((Player)entity).getMainHandItem().isEmpty());
                    }
                    return false;
                }).isEmpty();
        
        if (isThreatPresent) {
            // 有威胁，重置安全计时器
            safeTimer = 0;
        } else {
            // 无威胁，增加安全计时器
            safeTimer++;
            
            // 如果安全时间足够长，变回人形
            if (safeTimer >= SAFE_TIME_REQUIRED && transformationCooldown == 0) {
                revertToHumanForm();
            }
        }
    }
    
    // 变回人形的方法 - 参考VillagerEvents.java的转换机制，确保模型正确切换
    private void revertToHumanForm() {
        if (this.level().isClientSide()) {
            return;
        }
        
        try {
            // 检查是否有保存的原始数据
            if (this.getPersistentData().contains("StoredTimeRoyalty")) {
                // 恢复原始属性
                // 恢复原始最大生命值，但当前生命值按比例保留
                double currentHealthPercentage = this.getHealth() / this.getMaxHealth();
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(originalHealth);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(originalDamage);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(originalSpeed);
                
                // 按比例设置当前生命值
                this.setHealth((float)(originalHealth * currentHealthPercentage));
                
                // 清除变身相关的NBT标记
                this.getPersistentData().remove("IsInAnotherForm");
                this.getPersistentData().remove("AnotherRiderType");
                this.getPersistentData().remove("StoredTimeRoyalty");
            } else {
                // 安全回退：如果没有保存的数据，使用默认方式恢复
                double currentHealthPercentage = this.getHealth() / this.getMaxHealth();
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(originalHealth);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(originalDamage);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(originalSpeed);
                this.setHealth((float)(originalHealth * currentHealthPercentage));
                
                this.getPersistentData().remove("IsInAnotherForm");
                this.getPersistentData().remove("AnotherRiderType");
            }
            
            // 立即同步NBT数据到客户端，确保模型正确切换回人形
            // 由于TimeRoyaltyEntity是Mob的子类，直接使用自身实例进行同步
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
            
            // 重置状态
            isInAnotherForm = false;
            transformationCooldown = TRANSFORMATION_COOLDOWN;
            safeTimer = 0;
            
            // 在聊天框中显示变回人形的消息
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.getPlayers(p -> true).forEach(player -> {
                    player.displayClientMessage(Component.literal(boundAnotherRiderType + "变回了时间王族！"), true);
                });
            }
        } catch (Exception e) {
            // 如果出现异常，记录错误但不影响游戏运行
            e.printStackTrace();
        }
    }
    
    // 检测并减速附近的敌对实体
    private void detectAndSlowHostiles() {
        List<LivingEntity> nearbyHostiles = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(10.0D),
                entity -> entity instanceof Player && entity != this && this.shouldFearEntity(entity)
        );
        
        if (!nearbyHostiles.isEmpty()) {
            for (LivingEntity hostile : nearbyHostiles) {
                slowEntity(hostile);
            }
            timeSlowCooldown = TIME_SLOW_COOLDOWN;
        }
    }
    
    // 减速实体
    private void slowEntity(LivingEntity entity) {
        // 给予缓慢效果
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                TIME_SLOW_DURATION,
                2, // 3级缓慢
                false,
                false
        ));
        
        // 添加到减速列表
        slowedEntities.put(entity, TIME_SLOW_DURATION);
    }
    
    // 更新减速实体的时间
    private void updateSlowedEntities() {
        List<LivingEntity> toRemove = new ArrayList<>();
        
        for (Map.Entry<LivingEntity, Integer> entry : slowedEntities.entrySet()) {
            LivingEntity entity = entry.getKey();
            int timeLeft = entry.getValue() - 1;
            
            if (timeLeft <= 0 || entity.isDeadOrDying()) {
                toRemove.add(entity);
            } else {
                slowedEntities.put(entity, timeLeft);
            }
        }
        
        for (LivingEntity entity : toRemove) {
            slowedEntities.remove(entity);
        }
    }
    
    // 获取hasUsedAnotherWatch字段的值
    public boolean hasUsedAnotherWatch() {
        return this.hasUsedAnotherWatch;
    }
    
    // 设置变身冷却时间 - 用于从Another_Decade变回时调用
    public void setTransformationCooldown(int cooldown) {
        this.transformationCooldown = cooldown;
    }
    
    // 获取变身冷却状态
    public int getTransformationCooldown() {
        return this.transformationCooldown;
    }
    
    // 重写shouldFearEntity方法，明确证明时间王族的基类是平民时劫者
    // 通过继承TimeJackerEntity类，时间王族在本质上也是平民时劫者的一种进化形式
    // 这种继承关系表明时间王族和时劫者共享相同的基本能力和特性
    @Override
    public boolean shouldFearEntity(LivingEntity entity) {
        // 虽然时间王族已经进化，但作为时劫者的根基仍然存在
        // 时间王族几乎无所畏惧，只害怕极少数强大的存在
        return false; // 默认不害怕任何实体
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        // 可以在这里添加特殊掉落物
    }
    
    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }
}