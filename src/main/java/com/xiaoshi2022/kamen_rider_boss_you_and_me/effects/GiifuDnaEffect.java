package com.xiaoshi2022.kamen_rider_boss_you_and_me.effects;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GiifuDnaEffect extends MobEffect {
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

            // 有10%几率获得蝙蝠印章
            if (level.random.nextFloat() < 0.1f) {
                if (!hasBatStamp(player)) {
                    player.addItem(new ItemStack(ModItems.BAT_STAMP.get()));
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d基夫血脉觉醒！获得了蝙蝠印章§r"), true);
                }
            }
        }
    }

    private boolean hasBatStamp(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.BAT_STAMP.get()) {
                return true;
            }
        }
        return false;
    }

}
