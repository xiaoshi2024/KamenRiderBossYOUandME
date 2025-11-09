package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.goal.TimeJackerAnotherRiderGoal;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeJackerEntity extends VillagerEntityMCA {
    // 标记是否已经使用过异类表盘
    private boolean hasUsedAnotherWatch = false;
    
    // 重写父类方法提供实体属性
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
    
    public TimeJackerEntity(EntityType<? extends VillagerEntityMCA> p_35958_, Level p_35959_) {
        // 根据VillagerEntityMCA的构造函数要求，必须传递Gender参数
        super((EntityType<VillagerEntityMCA>) p_35958_, p_35959_, Gender.MALE);
        
        // 设置默认名称为时劫者
        this.setCustomName(Component.literal("时劫者"));
        
        // 设置村民数据，使用平原类型作为基础，无职业
        this.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
        
        // 设置时劫者特有的属性
        this.setHealth(30.0F);
        
        // 为了保持代码简洁，不在构造函数中直接设置物品，通过其他方法或事件设置
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        
        // 当实体被添加到世界时，给予异类表盘
        if (!this.level().isClientSide) {
            this.equipAnotherWatch();
        }
    }
    
    // 为自己装备异类表盘
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
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(5, new TimeJackerAnotherRiderGoal(this)); // 添加时劫者特有的AI目标
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new FloatGoal(this));
    }
    
    @Override
    public void setCustomName(@Nullable Component p_20053_) {
        super.setCustomName(p_20053_);
    }
    
    @Override
    public void setAge(int p_146763_) {
        super.setAge(p_146763_);
    }
    
    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }
    
    // 用于存储上一tick的生命值
    private float lastHealth = -1.0F;
    // 用于存储伤害来源
    private LivingEntity lastAttacker = null;
    // 伤害来源记忆时间（tick）
    private int attackerMemoryTicks = 0;
    private static final int MAX_ATTACKER_MEMORY = 20; // 1秒
    
    // 时间冻结相关字段
    private Map<LivingEntity, Integer> frozenEntities = new HashMap<>(); // 存储被冻结的实体及其冻结时间
    private int timeFreezeCooldown = 0; // 时间冻结冷却时间（tick）
    private static final int TIME_FREEZE_COOLDOWN = 100; // 5秒冷却时间（20tick=1秒）
    private static final int TIME_FREEZE_DURATION = 60; // 冻结持续时间3秒
    
    @Override
    public void tick() {
        super.tick();
        
        // 非客户端执行
        if (!this.level().isClientSide()) {
            // 更新冷却时间
            if (timeFreezeCooldown > 0) {
                timeFreezeCooldown--;
            }
            
            // 更新冻结实体的时间并解除冻结
            updateFrozenEntities();
            
            // 检查生命值变化来判断是否受到伤害
            float currentHealth = this.getHealth();
            
            if (lastHealth > 0 && currentHealth < lastHealth) {
                // 生命值减少，说明受到了伤害
                float damageAmount = lastHealth - currentHealth;
                
                // 寻找攻击者（优先检查玩家，然后检查其他敌对实体）
                LivingEntity attacker = null;
                
                // 1. 优先检查附近的玩家
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                        Player.class,
                        this.getBoundingBox().inflate(10.0D)
                );
                
                // 简单方法：任何在范围内的玩家都可能是攻击者
                // 因为在伤害事件中，我们知道时劫者受到了伤害
                if (!nearbyPlayers.isEmpty()) {
                    // 选择最近的玩家作为可能的攻击者
                    attacker = nearbyPlayers.get(0);
                    for (Player player : nearbyPlayers) {
                        if (player.distanceToSqr(this) < attacker.distanceToSqr(this)) {
                            attacker = player;
                        }
                    }
                } else {
                    // 2. 如果没找到玩家，检查其他敌对实体
                    List<LivingEntity> nearbyHostiles = this.level().getEntitiesOfClass(
                            LivingEntity.class,
                            this.getBoundingBox().inflate(10.0D),
                            entity -> entity != this && 
                                    entity instanceof Mob && 
                                    ((Mob)entity).getTarget() == this
                    );
                    
                    if (!nearbyHostiles.isEmpty()) {
                        attacker = nearbyHostiles.get(0);
                    }
                }
                
                if (attacker != null) {
                    // 找到了攻击者
                    lastAttacker = attacker;
                    attackerMemoryTicks = MAX_ATTACKER_MEMORY;
                    
                    // 通知附近的异类骑士
                    notifyNearbyAnotherRiders(lastAttacker);
                    
                    // 尝试高伤害逃离逻辑
                    tryEscapeOnHighDamage(null, damageAmount);
                    
                    // 尝试冻结攻击者
                    if (timeFreezeCooldown <= 0) {
                        freezeEntity(lastAttacker);
                    }
                }
            }
            
            // 更新上一tick的生命值
            lastHealth = currentHealth;
            
            // 处理攻击者记忆
            if (attackerMemoryTicks > 0) {
                attackerMemoryTicks--;
                // 如果还有攻击者记忆，持续通知异类骑士
                if (lastAttacker != null && lastAttacker.isAlive()) {
                    notifyNearbyAnotherRiders(lastAttacker);
                } else {
                    lastAttacker = null;
                }
            }
            
            // 检测附近试图攻击的生物
            detectAndFreezeAttackers();
            
            // 实现时间冻结无敌效果：在非冷却状态（非破绽）时恢复生命值
            // 这样可以模拟只有在冷却期间才会真正受伤
            if (!isVulnerable() && this.getHealth() < this.getMaxHealth()) {
                // 检查是否是被异类骑士攻击导致的伤害
                boolean recentlyAttackedByAnotherRider = false;
                
                // 使用lastAttacker来检查最近的攻击者
                if (lastAttacker != null) {
                    String attackerName = lastAttacker.getType().toString().toLowerCase();
                    String attackerClass = lastAttacker.getClass().getName().toLowerCase();
                    recentlyAttackedByAnotherRider = attackerName.contains("another") || 
                                                   attackerClass.contains("another") ||
                                                   lastAttacker instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o ||
                                                   lastAttacker instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o;
                }
                
                // 只有在不是被异类骑士攻击且不在冷却状态时，才恢复生命值（无敌效果）
                if (!recentlyAttackedByAnotherRider) {
                    this.setHealth(this.getMaxHealth());
                }
            }
        }
    }
    
    // 检测附近试图攻击的生物并冻结它们
    private void detectAndFreezeAttackers() {
        // 移除自动检测手持物品玩家的功能，只在玩家实际攻击时才冻结
        // 这个方法现在可以保持为空，因为冻结逻辑已经在tick方法中处理了
    }
    
    // 冻结实体
    private void freezeEntity(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || frozenEntities.containsKey(entity)) {
            return;
        }
        
        // 添加到冻结列表
        frozenEntities.put(entity, TIME_FREEZE_DURATION);
        
        // 设置实体无法移动（仅对Mob类型有效）
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        
        // 对所有攻击者（包括玩家和非玩家生物）施加缓慢和虚弱效果
        // 确保效果可见
        boolean showParticles = true;
        boolean showIcon = true;
        
        // 施加缓慢效果（等级5，持续3秒）
        MobEffectInstance slowEffect = new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, 
            TIME_FREEZE_DURATION, 
            4, // 0是等级1，所以4是等级5
            false, 
            showParticles, 
            showIcon
        );
        entity.addEffect(slowEffect);
        
        // 施加虚弱效果（等级5，持续3秒）
        MobEffectInstance weaknessEffect = new MobEffectInstance(
            MobEffects.WEAKNESS, 
            TIME_FREEZE_DURATION, 
            4, // 0是等级1，所以4是等级5
            false, 
            showParticles, 
            showIcon
        );
        entity.addEffect(weaknessEffect);
        
        // 如果是玩家，发送消息提示
        if (entity instanceof Player player) {
            // 播放冻结音效，让玩家知道被冻结了
//            this.playSound(ModBossSounds.ANOTHER_ZI_O_CLICK.get(), 1.0F, 0.8F);
            
            // 发送消息提示玩家
            player.displayClientMessage(Component.literal("时劫者冻结了你的行动！"), true);
        }
        
        // 设置冷却时间
        timeFreezeCooldown = TIME_FREEZE_COOLDOWN;
        
        // 在实体周围生成粒子效果（这里可以根据需要添加）
        // 例如：this.level().addParticle(...)
    }
    
    // 更新冻结实体的状态
    private void updateFrozenEntities() {
        List<LivingEntity> toRemove = new ArrayList<>();
        
        for (Map.Entry<LivingEntity, Integer> entry : frozenEntities.entrySet()) {
            LivingEntity entity = entry.getKey();
            int remainingTime = entry.getValue() - 1;
            
            if (remainingTime <= 0 || !entity.isAlive()) {
                // 解冻实体（仅对Mob类型有效）
                if (entity instanceof Mob mob) {
                    mob.setNoAi(false);
                }
                toRemove.add(entity);
            } else {
                // 更新剩余冻结时间
                frozenEntities.put(entity, remainingTime);
            }
        }
        
        // 移除已解冻的实体
        for (LivingEntity entity : toRemove) {
            frozenEntities.remove(entity);
        }
    }
    
    // 检查时劫者是否处于冷却状态（破绽）
    public boolean isVulnerable() {
        return timeFreezeCooldown > 0;
    }
    
    // 通知附近的异类骑士来保护时劫者
    private void notifyNearbyAnotherRiders(LivingEntity attacker) {
        if (attacker == null || !attacker.isAlive()) {
            return;
        }
        
        // 查找附近的所有异类骑士
        List<LivingEntity> nearbyAnotherRiders = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(20.0D),
                entity -> {
                    // 判断是否是异类骑士实体
                    String entityName = entity.getType().toString().toLowerCase();
                    String className = entity.getClass().getName().toLowerCase();
                    return entityName.contains("another") || 
                           className.contains("another") || 
                           entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o ||
                           entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o;
                }
        );
        
        // 通知每个异类骑士来保护时劫者
        for (LivingEntity rider : nearbyAnotherRiders) {
            if (rider instanceof Mob mobRider) {
                // 设置攻击目标为攻击者
                mobRider.setTarget(attacker);
            }
        }
    }
    
    // 添加一个自定义方法来处理高伤害触发的逃离逻辑
    // 由于 hurt 方法是 final 的，我们将在其他地方（如 AI 或 tick 方法）调用此方法
    public void tryEscapeOnHighDamage(DamageSource source, float amount) {
        // 受到高伤害时，有几率召唤异类电班列
        if (!this.level().isClientSide() && amount > this.getMaxHealth() * 0.3 && this.random.nextFloat() < 0.4) {
            // 查找附近的异类骑士（优先找异类电王）
            List<com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o> nearbyAnotherDenO = this.level().getEntitiesOfClass(
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o.class,
                    this.getBoundingBox().inflate(20.0D)
            );
            
            // 召唤异类电班列
            com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity denliner = new com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity(this.level());
            denliner.setPos(this.getX(), this.getY() + 2, this.getZ());
            this.level().addFreshEntity(denliner);
            
            // 保存自己和附近的异类骑士到班列中
            List<Entity> entitiesToSave = new ArrayList<>();
            entitiesToSave.add(this);
            if (!nearbyAnotherDenO.isEmpty()) {
                entitiesToSave.add(nearbyAnotherDenO.get(0)); // 保存最近的一个异类电王
            }
            
            // 保存实体并传送 - 传递一个假的玩家（null），因为实际上是AI触发的
            denliner.saveEntities(null, entitiesToSave.toArray(new Entity[0]));
            
            // 传送班列到时之沙漠维度
            try {
                if (denliner.level() instanceof ServerLevel) {
                    // 查找目标维度
                    ServerLevel timeDesert = null;
                    for (ServerLevel level : ((ServerLevel) denliner.level()).getServer().getAllLevels()) {
                        if (level.dimension().location().toString().equals("kamen_rider_weapon_craft:the_desertof_time")) {
                            timeDesert = level;
                            break;
                        }
                    }
                    
                    if (timeDesert != null) {
                        denliner.changeDimension(timeDesert);
                        // 随机设置班列在时之沙漠中的位置
                        denliner.setPos(
                                -1980 + this.random.nextInt(3960), // X坐标范围
                                100, // Y坐标固定高度
                                -1980 + this.random.nextInt(3960)  // Z坐标范围
                        );
                    }
                }
            } catch (Exception e) {
                // 如果传送失败，至少确保实体被保存
                e.printStackTrace();
            }
        }
    }
    
    // 由于getDisplayName()在父类中是final的，我们不能覆盖它
    
    // 时劫者特有方法：可以创建或控制异类骑士
    public void createAnotherRider(LivingEntity target) {
        // 检查是否已经使用过表盘，并且目标必须是MCA村民
        if (target == null || !target.isAlive() || hasUsedAnotherWatch() || !(target instanceof VillagerEntityMCA)) {
            return;
        }
        
        try {
            // 随机选择要创建的异类骑士类型
            int randomType = this.random.nextInt(2); // 暂时支持两种异类骑士
            Entity anotherRider = null;
            
            // 创建时劫者时保存的村民数据
            CompoundTag villagerTag = new CompoundTag();
            ((VillagerEntityMCA) target).save(villagerTag);
            
            // 根据随机类型创建不同的异类骑士
            if (randomType == 0 && ModEntityTypes.ANOTHER_ZI_O.isPresent()) {
                anotherRider = ModEntityTypes.ANOTHER_ZI_O.get().create(this.level());
                if (anotherRider != null) {
                    // 保存村民数据到异类骑士
                    ((Mob) anotherRider).getPersistentData().put("StoredVillager", villagerTag);
                }
            } else if (randomType == 1 && ModEntityTypes.ANOTHER_DEN_O.isPresent()) {
                anotherRider = ModEntityTypes.ANOTHER_DEN_O.get().create(this.level());
                if (anotherRider != null) {
                    // 保存村民数据到异类骑士
                    ((Mob) anotherRider).getPersistentData().put("StoredVillager", villagerTag);
                }
            }
            
            // 如果成功创建了异类骑士实体
            if (anotherRider != null) {
                // 设置异类骑士的位置在目标的位置
                anotherRider.setPos(target.getX(), target.getY(), target.getZ());
                anotherRider.setYRot(target.getYRot());
                
                // 播放对应的异类骑士音效
                if (randomType == 0) {
                    // 播放异类时王音效
                    this.playSound(ModBossSounds.ANOTHER_ZI_O_CLICK.get(), 1.0F, 1.0F);
                } else if (randomType == 1) {
                    // 播放异类电王音效（如果有）
                    // 这里使用AIDEN_OWC作为异类电王的音效
                    this.playSound(ModBossSounds.AIDEN_OWC.get(), 1.0F, 1.0F);
                }
                
                // 添加到世界中
                this.level().addFreshEntity(anotherRider);
                
                // 标记已经使用过表盘
                this.getPersistentData().putBoolean("HasUsedAnotherWatch", true);
                
                // 丢弃主手物品（异类表盘）
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                
                // 移除原村民
                target.discard();
            }
        } catch (Exception e) {
            // 捕获可能的异常，确保游戏不会崩溃
            this.level().broadcastEntityEvent(this, (byte)35); // 发送自定义实体事件
        }
    }
    
    // 提供方法检查是否已使用过表盘，使用持久化数据而不是成员变量
    public boolean hasUsedAnotherWatch() {
        return this.getPersistentData().getBoolean("HasUsedAnotherWatch");
    }
    
    /**
     * 判断时劫者是否应该害怕某个实体
     * 时劫者不应该害怕异类骑士
     */
    public boolean shouldFearEntity(LivingEntity entity) {
        // 检查是否是异类骑士实体
        if (entity != null) {
            String entityName = entity.getType().toString().toLowerCase();
            String className = entity.getClass().getName().toLowerCase();
            
            // 检查是否是异类骑士相关实体
            if (entityName.contains("another") || 
                className.contains("another") || 
                entityName.contains("den_o") || 
                className.contains("den_o") ||
                entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o ||
                entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o) {
                // 不害怕异类骑士
                return false;
            }
        }
        
        // 对于其他实体，使用默认逻辑（如果父类有此方法）
        // 由于没有直接看到父类的实现，这里返回false表示默认不害怕
        return false;
    }
    

    
    /**
     * 处理村民传感器使用的isHostile方法
     * 确保时劫者不会将异类骑士视为敌对实体
     */
    public boolean isAfraidOf(LivingEntity entity) {
        // 调用shouldFearEntity方法来判断是否应该害怕
        return shouldFearEntity(entity);
    }
    
    /**
     * 死亡掉落逻辑
     * 使用数据包形式掉落物品，不再在代码中直接添加掉落物
     */
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        // 只调用父类方法，让游戏通过战利品表（数据包）处理掉落
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
    }
}