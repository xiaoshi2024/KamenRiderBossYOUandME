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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}