package com.xiaoshi2022.kamen_rider_boss_you_and_me.commands;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID)
public class CommandRegistry {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // 注册异类Decade命令
        AnotherDecadeCommand.register(event.getDispatcher());
        
        // 这里可以添加更多命令注册
    }
}