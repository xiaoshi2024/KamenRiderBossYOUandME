package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.villagers.TimeJackerProfession;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeJackerTradesEvent {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        // 检查是否是时劫者职业
        if (event.getType() == TimeJackerProfession.TIME_JACKER_PROFESSION.get()) {
            // 添加交易：玩家用绿宝石换AIZIOWC（异类表盘）
            event.getTrades().get(1).add(new BasicItemListing(
                    new ItemStack(Items.EMERALD, 3), // 3个绿宝石
                    new ItemStack(ModItems.AIZIOWC.get()), // 异类表盘
                    8, 4, 0.05f)); // 最大供应次数、经验、价格乘数

            // 添加交易：玩家用AIZIOWC换绿宝石
            event.getTrades().get(2).add(new BasicItemListing(
                    new ItemStack(ModItems.AIZIOWC.get()), // 异类表盘
                    new ItemStack(Items.EMERALD, 2), // 2个绿宝石
                    8, 4, 0.05f)); // 最大供应次数、经验、价格乘数

            // 可以根据需要添加更多交易选项
            // 例如添加更高级的交易，需要更多绿宝石或其他物品
            event.getTrades().get(3).add(new BasicItemListing(
                    new ItemStack(Items.EMERALD, 8), // 8个绿宝石
                    new ItemStack(ModItems.AIZIOWC.get(), 2), // 2个异类表盘
                    4, 8, 0.05f)); // 更少的供应次数、更多经验
        }
    }
}