package com.xiaoshi2022.kamen_rider_boss_you_and_me.effects;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GiifuDnaEffect extends MobEffect {
    // 存储已获得蝙蝠印章的玩家UUID
    private static final Set<UUID> playersWithBatStamp = new HashSet<>();
    
    public GiifuDnaEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00); // 绿色
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每次效果触发时都执行
        return duration % 20 == 0; // 每1秒检查一次
    }

    @Override
    public void applyEffectTick(net.minecraft.world.entity.LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            // 获取玩家变量
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            
            // 检查玩家是否已经是基夫种族
            if (!variables.isGiifu) {
                // 将玩家设置为基夫种族
                variables.isGiifu = true;
                variables.syncPlayerVariables(player);
                
                // 显示基夫血脉觉醒的消息
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d基夫血脉觉醒！你现在是基夫种族！§r"), true);
                
                // 触发成就：献上感谢，亡命徒
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    kamen_rider_boss_you_and_me.GIIFU_TRIGGER.trigger(serverPlayer);
                }
            }

            // 当玩家成为基夫种族时，有10%几率获得蝙蝠印章
            if (variables.isGiifu && !hasBatStamp(player) && !playersWithBatStamp.contains(player.getUUID())) {
                // 有10%几率获得蝙蝠印章
                if (player.level().random.nextFloat() < 0.1f) {
                    player.addItem(new ItemStack(ModItems.BAT_STAMP.get()));
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d基夫血脉觉醒！获得了蝙蝠印章§r"), true);

                    // 将玩家UUID添加到集合中，标记为已获得
                    playersWithBatStamp.add(player.getUUID());
                }
            }
        }
    }
    
    private static boolean hasBatStamp(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.BAT_STAMP.get()) {
                return true;
            }
        }
        return false;
    }
}