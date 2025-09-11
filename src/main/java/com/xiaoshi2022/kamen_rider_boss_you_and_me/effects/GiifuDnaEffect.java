package com.xiaoshi2022.kamen_rider_boss_you_and_me.effects;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GiifuDnaEffect extends MobEffect {
    // 用于存储已经获得蝙蝠印章的玩家UUID
    private static final Set<UUID> playersWithBatStamp = ConcurrentHashMap.newKeySet();

    public GiifuDnaEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00); // 绿色
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每10秒触发一次
        return duration % 200 == 0;
    }

    @Override
    public void applyEffectTick(net.minecraft.world.entity.LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            Level level = player.level();

            // 检查玩家是否已经获得过蝙蝠印章
            if (!playersWithBatStamp.contains(player.getUUID())) {
                // 有10%几率获得蝙蝠印章
                if (level.random.nextFloat() < 0.1f) {
                    player.addItem(new ItemStack(ModItems.BAT_STAMP.get()));
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d基夫血脉觉醒！获得了蝙蝠印章§r"), true);

                    // 将玩家UUID添加到集合中，标记为已获得
                    playersWithBatStamp.add(player.getUUID());
                }
            }
        }
    }
}