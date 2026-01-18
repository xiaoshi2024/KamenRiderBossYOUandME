package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.RiderInvisibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.*;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        RiderInvisibilityManager.updateInvisibility(player);
    }

    /* 装备栏变化时立即刷新（防止延迟） */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) return;

        RiderInvisibilityManager.updateInvisibility(player);
    }
    
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(CHANGE_KEY);
        event.register(CHANGES_KEY);
        event.register(RELIEVE_KEY);
        event.register(KEY_GUARD);
        event.register(KEY_BLAST);
        event.register(KEY_BOOST);
        event.register(KEY_FLIGHT);
        event.register(KEY_BARRIER_PULL);
    }
}