package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkSquashPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.JinbaGuardPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.LemonBoostPacket;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
//按键！！！
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
        if (KEY_GUARD.consumeClick()) PacketHandler.sendToServer(new JinbaGuardPacket());
        if (KEY_BLAST.consumeClick()) PacketHandler.sendToServer(new DarkSquashPacket());
        if (KEY_BOOST.consumeClick()) PacketHandler.sendToServer(new LemonBoostPacket());
    }
}
