package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosCapability;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.*;

public class modEventBusEvents {
    @Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
    public class ClientEvents {
        //        @SubscribeEvent
//        public static void onKeyInput(InputEvent.Key event) {
//            if (Minecraft.getInstance().screen == null) {
//                if (CHANGE_KEY.isDown()) {
//                    // 获取当前玩家
//                    LocalPlayer player = Minecraft.getInstance().player;
//                    if (player != null) {
//                        // 发送网络消息到服务器
//                        PacketHandler.sendToServer(new PlayerAnimationPacket(player.getId(), STORIOUS_VFX_HEIXIN));
//                    }
//                }
//            }
//        }
//    @SubscribeEvent
//    public static void onKeyInput(InputEvent.Key event) {
//        if (CHANGE_KEY.consumeClick()) {
//            // 按键被按下，发送网络包到服务器
//            if (Minecraft.getInstance().level != null) {
//                PacketHandler.INSTANCE.sendToServer(new SoundStopPacket(
//                        Minecraft.getInstance().player.getId(), "kamen_rider_boss_you_and_me:banana_lockonby"));
//                }
//            }
//        }
        // 在事件处理器类中添加
        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
                    curios.findCurio("belt", 0).ifPresent(slotResult -> {
                        ItemStack stack = slotResult.stack();

                        // 战极驱动器处理（保持原有逻辑）
                        if (stack.getItem() instanceof sengokudrivers_epmty belt) {
                            PacketHandler.sendToClient(
                                    new BeltAnimationPacket(
                                            player.getId(),
                                            "login_sync",
                                            belt.currentMode
                                    ),
                                    player
                            );
                        }

                        // Genesis驱动器处理（新增柠檬状态同步）
                        if (stack.getItem() instanceof Genesis_driver belt) {
                            Genesis_driver.BeltMode mode = belt.getMode(stack);
                            boolean hen = belt.getHenshin(stack);
                            boolean equipped = belt.getEquipped(stack);

                            String anim;
                            if (hen) {
                                anim = "move";
                            } else if (equipped && mode == Genesis_driver.BeltMode.LEMON) {
                                anim = "lemon_idle";
                            } else {
                                anim = "idles";
                            }

                            System.out.printf("登录同步Genesis驱动器: 模式=%s 动画=%s%n", mode, anim);

                            PacketHandler.sendToClient(
                                    new BeltAnimationPacket(player.getId(), anim, mode),
                                    player);

                            // 不需要 setTag，只是读取
                        }
                    });
                });
            }
        }
    }

    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public final class CommonListener {

        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            // 统一使用自定义属性系统

            event.put(ModEntityTypes.LORD_BARON.get(), LordBaronEntity.createAttributes()
                    .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 5.0D)
                    .add(Attributes.MAX_HEALTH, 100.0D)
                    .build()
            );

            event.put(ModEntityTypes.GIIFUDEMOS_ENTITY.get(),
                    GiifuDemosEntity.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 5.0D)
                            .add(Attributes.MAX_HEALTH, 100.0D)
                            .build()
            );

            event.put(ModEntityTypes.STORIOUS.get(),
                    StoriousEntity.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 5.0D)
                            .add(Attributes.MAX_HEALTH, 122.0D)
                            .build()
            );

            event.put(ModEntityTypes.GIFFTARIAN.get(),
                    Gifftarian.createMonsterAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 8.0D)
                            .add(Attributes.MAX_HEALTH, 80.0D)
                            .build()
            );

            event.put(ModEntityTypes.INVES_HEILEHIM.get(),
                    ElementaryInvesHelheim.createMonsterAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 6.0D)
                            .add(Attributes.MAX_HEALTH, 50.0D)
                            .build()
            );
        }

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(CHANGE_KEY);
            event.register(CHANGES_KEY);
            event.register(RELIEVE_KEY);
        }
    }
}