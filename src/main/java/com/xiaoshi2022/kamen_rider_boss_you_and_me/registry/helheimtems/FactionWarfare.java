package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;
import java.util.function.Consumer;

public class FactionWarfare {
    // 命令两个阵营开战
    public static void declareWar(FactionLeader leader1, FactionLeader leader2) {
        Objects.requireNonNull(leader1, "Leader1 cannot be null");
        Objects.requireNonNull(leader2, "Leader2 cannot be null");

        // 检查是否是同一个阵营
        if (leader1.getFactionId().equals(leader2.getFactionId())) {
            return;
        }

        // 准备战斗效果
        MobEffectInstance damageBoost = new MobEffectInstance(
                MobEffects.DAMAGE_BOOST, 600, 1, false, true);
        MobEffectInstance speed = new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 600, 0, false, true);

        // 指挥双方小兵
        leader1.commandMinions(minion -> {
            minion.setTarget((LivingEntity) leader2);
            minion.addEffect(damageBoost);
            minion.addEffect(speed);
        });

        leader2.commandMinions(minion -> {
            minion.setTarget((LivingEntity) leader1);
            minion.addEffect(damageBoost);
            minion.addEffect(speed);
        });
    }

    // 自动检测敌对阵营
    @SubscribeEvent
    public static void onLeaderTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof FactionLeader leader)) {
            return;
        }

        if (event.getEntity().level().isClientSide) {
            return;
        }

        leader.commandMinions(minion -> {
            AABB area = minion.getBoundingBox().inflate(30);
            minion.level().getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    e -> e instanceof FactionLeader otherLeader &&
                            !otherLeader.getFactionId().equals(leader.getFactionId())
            ).forEach(minion::setTarget);
        });
    }
}