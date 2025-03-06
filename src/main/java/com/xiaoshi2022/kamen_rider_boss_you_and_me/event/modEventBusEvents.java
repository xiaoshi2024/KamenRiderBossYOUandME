package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup.STORIOUS_VFX_HEIXIN;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.CHANGE_KEY;

public class modEventBusEvents {
    @Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
    public class ClientEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (Minecraft.getInstance().screen == null) {
                if (CHANGE_KEY.isDown()) {
                    // 获取当前玩家
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        // 发送网络消息到服务器
                        PacketHandler.sendToServer(new PlayerAnimationPacket(player.getUUID(), STORIOUS_VFX_HEIXIN));
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public final class CommonListener {
        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), GiifuDemosEntity.createAttributes().add(Attributes.MAX_HEALTH, 100.0D).build());
            event.put(ModEntityTypes.STORIOUS.get(), GiifuDemosEntity.createAttributes().add(Attributes.MAX_HEALTH, 122.0D).build());
            event.put(ModEntityTypes.GIFFTARIAN.get(), GiifuDemosEntity.createAttributes().add(Attributes.MAX_HEALTH,80.0D).add(Attributes.ATTACK_DAMAGE,8.0D).build());
            event.put(ModEntityTypes.INVES_HEILEHIM.get(), ElementaryInvesHelheim.createMonsterAttributes().add(Attributes.MAX_HEALTH,50.0D).add(Attributes.ATTACK_DAMAGE,6.0D).build());
        }
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(CHANGE_KEY);
        }
    }

}