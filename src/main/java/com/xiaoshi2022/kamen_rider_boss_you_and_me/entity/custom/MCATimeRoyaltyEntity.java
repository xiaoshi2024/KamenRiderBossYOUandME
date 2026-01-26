package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * MCA时间王族实体类
 * 这个类是时间王族实体的MCA副本，当加载MCA模组时使用
 */
public class MCATimeRoyaltyEntity extends TimeRoyaltyEntity {

    public MCATimeRoyaltyEntity(EntityType<? extends TimeRoyaltyEntity> entityType, Level level) {
        super(entityType, level);

        // 设置默认名称为时间王族
        this.setCustomName(Component.literal("时间王族"));

        // 设置时间王族特有的属性
        this.setHealth(50.0F);

        // 如果MCA可用，使用反射获取MCA村民的属性
        if (MCAUtil.isMCAAvailable()) {
            try {
                // 使用反射获取MCA村民类
                Class<?> mcaVillagerClass = Class.forName("forge.net.mca.entity.VillagerEntityMCA");

                // 使用反射创建MCA村民实例
                Constructor<?> mcaConstructor = mcaVillagerClass.getConstructor(EntityType.class, Level.class);
                Object mcaVillager = mcaConstructor.newInstance(entityType, level);

                // 使用反射复制MCA村民的属性到当前实体
                // 复制所有字段值
                for (Field field : mcaVillagerClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        // 尝试获取并设置字段值
                        Object value = field.get(mcaVillager);
                        Field currentField = this.getClass().getSuperclass().getSuperclass().getDeclaredField(field.getName());
                        currentField.setAccessible(true);
                        currentField.set(this, value);
                    } catch (NoSuchFieldException e) {
                        // 如果当前实体没有对应的字段，跳过
                        continue;
                    } catch (IllegalAccessException | IllegalArgumentException e) {
                        // 访问权限或类型不匹配，跳过
                        continue;
                    }
                }
            } catch (Exception e) {
                // 如果反射操作失败，忽略并继续使用默认实体
                // 这可能是因为MCA版本不兼容或其他原因
            }
        }
    }

    @Override
    public boolean isVillagerMCA() {
        // 始终返回true，因为这是MCA时间王族实体
        return true;
    }
}