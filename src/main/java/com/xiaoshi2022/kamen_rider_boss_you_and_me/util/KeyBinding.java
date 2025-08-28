package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkSquashPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.JinbaGuardPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.LemonBoostPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaBatModePacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaBloodSuckPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaSonicBlastPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBinding {
    public static final String KEY_CATEGORY_kamen_rider_boss_you_and_me = "KEY.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me";
    public static final String KEY_CHANGE_BODY = "KEY.kamen_rider_weapon_craft.change_body";
    public static final String KEY_CHANGE_PLAYER = "KEY.kamen_rider_weapon_craft.change_player";
    public static final String KEY_RELIEVE_PLAYER = "KEY.kamen_rider_weapon_craft.relieve_player";

    public static final KeyMapping KEY_GUARD  = new KeyMapping("key.guard",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.kamen_rider");
    public static final KeyMapping KEY_BLAST  = new KeyMapping("key.blast",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.kamen_rider");
    public static final KeyMapping KEY_BOOST  = new KeyMapping("key.boost",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.kamen_rider");

    public static final KeyMapping CHANGE_KEY = new KeyMapping(KEY_CHANGE_BODY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, KEY_CATEGORY_kamen_rider_boss_you_and_me);
    public static final KeyMapping CHANGES_KEY = new KeyMapping(KEY_CHANGE_PLAYER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY_kamen_rider_boss_you_and_me);
    public static final KeyMapping RELIEVE_KEY = new KeyMapping(KEY_RELIEVE_PLAYER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, KEY_CATEGORY_kamen_rider_boss_you_and_me);

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // 直接在客户端检测玩家是否穿着全套黑暗Kiva盔甲
        boolean isDarkKiva = isDarkKivaArmorEquipped(mc.player);
        
        // 根据当前穿着的盔甲类型决定触发哪个能力
        if (isDarkKiva) {
            if (KEY_GUARD.consumeClick()) PacketHandler.sendToServer(new DarkKivaBatModePacket());
            if (KEY_BLAST.consumeClick()) PacketHandler.sendToServer(new DarkKivaBloodSuckPacket());
            if (KEY_BOOST.consumeClick()) PacketHandler.sendToServer(new DarkKivaSonicBlastPacket());
        } else {
            if (KEY_GUARD.consumeClick()) PacketHandler.sendToServer(new JinbaGuardPacket());
            if (KEY_BLAST.consumeClick()) PacketHandler.sendToServer(new DarkSquashPacket());
            if (KEY_BOOST.consumeClick()) PacketHandler.sendToServer(new LemonBoostPacket());
        }
    }
    
    // 客户端专用的黑暗Kiva盔甲检测方法
    private static boolean isDarkKivaArmorEquipped(LocalPlayer player) {
        // 检查是否穿着黑暗Kiva头盔、胸甲和护腿（不需要鞋子）
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DarkKivaItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DarkKivaItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DarkKivaItem;
    }
}
