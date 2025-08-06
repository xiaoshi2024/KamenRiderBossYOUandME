package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.function.Consumer;

public interface FactionLeader {
    // 获取阵营唯一ID
    String getFactionId();

    // 指挥所有小兵执行动作
    void commandMinions(Consumer<ElementaryInvesHelheim> action);

    // 添加小兵到阵营
    default void addMinion(ElementaryInvesHelheim minion) {
        HelheimFactionManager.getFaction(this).ifPresent(f -> f.minions.add(minion));
    }

    /**
     * 设置阵营所有者
     */
    void setOwner(LivingEntity owner);

    /**
     * 获取所有者
     */
    LivingEntity getOwner();

    /**
     * 获取显示名称（用于UI）
     */
    default Component getDisplayName() {
        return ((Entity) this).getDisplayName();
    }

    // 从接口中移除 level() 方法
    // ServerLevel level(); // 不再需要，使用强制转换获取level

    // 获取所属阵营
    default Optional<HelheimFactionManager.Faction> getFaction() {
        return HelheimFactionManager.getFaction(this);
    }
}