package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * MCA时劫者实体类
 * 这个类是时劫者实体的MCA副本，当加载MCA模组时使用
 */
public class MCATimeJackerEntity extends TimeJackerEntity {

    public MCATimeJackerEntity(EntityType<? extends TimeJackerEntity> entityType, Level level) {
        super(entityType, level);

        // 设置默认名称为时劫者
        this.setCustomName(Component.literal("时劫者"));

        // 设置时劫者特有的属性
        this.setHealth(30.0F);

        // 如果MCA可用，使用MCAUtil类来获取和设置MCA村民的属性
        if (MCAUtil.isMCAAvailable()) {
            try {
                // 获取MCA村民类
                Class<?> mcaVillagerClass = MCAUtil.getVillagerEntityMCAClass();
                if (mcaVillagerClass != null) {
                    // 使用反射复制MCA村民的属性到当前实体
                    // 复制所有字段值
                    for (java.lang.reflect.Field field : mcaVillagerClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        try {
                            // 尝试获取并设置字段值
                            Object value = field.get(this);
                            java.lang.reflect.Field currentField = this.getClass().getSuperclass().getSuperclass().getDeclaredField(field.getName());
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
                }
            } catch (Exception e) {
                // 如果反射操作失败，忽略并继续使用默认实体
            }
        }
    }

    @Override
    public boolean isVillagerMCA() {
        // 始终返回true，因为这是MCA时劫者实体
        return true;
    }
}