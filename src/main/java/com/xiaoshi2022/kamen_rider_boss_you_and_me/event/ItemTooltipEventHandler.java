package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemTooltipEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        List<Component> tooltips = event.getToolTip();
        String modId = ForgeRegistries.ITEMS.getKey(item).getNamespace();
        String registryName = ForgeRegistries.ITEMS.getKey(item).getPath();
        
        // 只处理本模组的物品
        if (modId.equals(kamen_rider_boss_you_and_me.MODID)) {
            String tooltipKey = "item." + modId + "." + registryName + ".tooltip";
            try {
                // 尝试获取tooltip文本
                Component tooltip = Component.translatable(tooltipKey);
                // 检查是否存在有效的tooltip（不是原封不动的键名）
                if (!tooltip.getString().equals(tooltipKey)) {
                    // 添加tooltip到物品描述中，使用灰色文本
                    if (tooltip instanceof MutableComponent mutableTooltip) {
                        tooltips.add(mutableTooltip.withStyle(ChatFormatting.GRAY));
                    } else {
                        tooltips.add(tooltip);
                    }
                }
            } catch (Exception e) {
                // 如果获取tooltip失败，忽略错误
            }
        }
    }
}