package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InvesMeat extends Item {
    public InvesMeat() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationMod(0.1f)
                        .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 100, 0), 1.0f) // 100刻（5秒）的反胃效果，100% 概率
                        .build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.kamen_rider_boss_you_and_me.inves_meat.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
}