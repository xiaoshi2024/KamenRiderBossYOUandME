package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

// 注意：
// 1. 在与僵尸交互时，EntityCurioInteractionHandler.onEntityInteract会调用EntityCuriosListMenu.openMenu()
// 2. 当解除变身后，onCurioChange事件会清除僵尸的所有盔甲，自动恢复本体模型

public class RiderZombie extends Zombie {

    public RiderZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    // 为变种僵尸注册属性
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D) // 增加生命值
                .add(Attributes.ATTACK_DAMAGE, 4.0D) // 增加攻击力
                .add(Attributes.MOVEMENT_SPEED, 0.28D); // 稍微增加移动速度
    }

    // 在实体生成时设置装备
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficulty, spawnType, spawnGroupData, compoundTag);
        
        // 生成时给予随机锁种的腰带
        giveRandomDriverWithLockseed();
        
        return data;
    }

    // 给予随机锁种的腰带
    private void giveRandomDriverWithLockseed() {
        // 创建一个新的腰带物品栈
        ItemStack driverStack = new ItemStack(ModItems.GENESIS_DRIVER.get()); // 使用正确的物品引用
        
        if (driverStack.getItem() instanceof Genesis_driver driver) {
            // 随机选择一个锁种模式
            Genesis_driver.BeltMode[] modes = new Genesis_driver.BeltMode[]{
                    Genesis_driver.BeltMode.LEMON,
                    Genesis_driver.BeltMode.MELON,
                    Genesis_driver.BeltMode.CHERRY,
                    Genesis_driver.BeltMode.PEACH,
                    Genesis_driver.BeltMode.DRAGONFRUIT
            };
            
            RandomSource random = this.random;
            Genesis_driver.BeltMode randomMode = modes[random.nextInt(modes.length)];
            
            // 设置腰带模式
            driver.setMode(driverStack, randomMode);
            driver.setActive(driverStack, true);
            
            // 将腰带装备到主手
            this.setItemInHand(this.getUsedItemHand(), driverStack);
            
            // 设置自定义名称
            String riderName = getRiderNameByMode(randomMode);
            this.setCustomName(Component.literal(riderName + " 僵尸"));
        }
    }

    // 根据锁种模式获取骑士名称
    private String getRiderNameByMode(Genesis_driver.BeltMode mode) {
        return switch (mode) {
            case LEMON -> "Baron";
            case MELON -> "Zangetsu Shin";
            case CHERRY -> "Sigurd";
            case PEACH -> "Marika";
            case DRAGONFRUIT -> "Tyrant";
            default -> "未知骑士";
        };
    }

    // 在tick方法中确保腰带状态正确
    @Override
    public void tick() {
        super.tick();
        
        // 每20tick检查一次装备
        if (this.tickCount % 20 == 0) {
            ItemStack mainHand = this.getMainHandItem();
            
            // 如果主手没有腰带，给予一个
            if (!(mainHand.getItem() instanceof Genesis_driver)) {
                giveRandomDriverWithLockseed();
            } else if (mainHand.getItem() instanceof Genesis_driver driver) {
                // 确保腰带不是默认模式
                if (driver.getMode(mainHand) == Genesis_driver.BeltMode.DEFAULT) {
                    giveRandomDriverWithLockseed();
                }
            }
        }
    }
}