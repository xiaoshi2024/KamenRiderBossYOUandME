package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerJoinSyncPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.RiderInvisibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.List;

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
                            sengokudrivers_epmty.BeltMode mode = belt.getMode(stack);   // ← 读取 NBT
                            PacketHandler.sendToClient(
                                    new BeltAnimationPacket(player.getId(), "login_sync", mode),
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
        public static void onPlayerChat(ServerChatEvent event) {
            String msg = event.getMessage().getString(); // 获取玩家发送的消息
            Player player = event.getPlayer(); // 获取发送消息的玩家
            Level level = player.level(); // 获取玩家所在的世界

            // 安全检查：确保世界和玩家有效
            if (level == null || !player.isAlive()) {
                return;
            }

            /* ---------- 彩蛋：小丑蝙蝠 ---------- */
            if (KivatBatTwoNd.isClownBatCall(msg)) {
                event.setCanceled(true);

                // 使用正确的世界引用
                List<KivatBatTwoNd> clowns = level.getEntitiesOfClass(
                        KivatBatTwoNd.class,
                        player.getBoundingBox().inflate(16), //范围
                        k -> k != null && k.isAlive() && !k.isTame() // 添加安全检查
                );

                if (clowns.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("dialog.kivat.noclowntarget"));
                    return;
                }

                // 安全地召唤每个小丑蝙蝠
                clowns.forEach(k -> {
                    if (k != null && k.isAlive()) {
                        k.temptToPlayer(player);
                    }
                });

                player.sendSystemMessage(Component.translatable("dialog.kivat.clownbat"));
                return;
            }

            /* ---------- 正常 @kivat 对话 ---------- */
            if (!msg.startsWith("@kivat")) return;
            event.setCanceled(true);

            KivatBatTwoNd kivat = level.getEntitiesOfClass(
                    KivatBatTwoNd.class,
                    player.getBoundingBox().inflate(20),
                    k -> k != null && k.isAlive() && k.isTame() && k.isOwnedBy(player) // 添加安全检查
            ).stream().findFirst().orElse(null);

            if (kivat == null) {
                player.sendSystemMessage(Component.translatable("dialog.kivat.notfound"));
                return;
            }

            String query = msg.substring("@kivat".length()).trim();
            if (level instanceof ServerLevel) {
                if (query.equals("变身") || query.equals("好了!灭绝时刻到了") || query.equals("henshin")) {

                    // 1. 先检查盔甲
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                            ItemStack armor = player.getItemBySlot(slot);
                            if (!armor.isEmpty() && armor.getItem() instanceof KamenBossArmor) {
                                player.sendSystemMessage(Component.translatable("dialog.kivat.already_transformed"));
                                return;
                            }
                        }
                    }

                    // 2. 再检查距离
                    if (kivat.distanceTo(player) <= 2.0) {
                        kivat.transform(player);
                    } else {
                        player.sendSystemMessage(Component.translatable("dialog.kivat.too_far"));
                        // 如果你想让它飞过来再变身，把下面两行取消注释
                         kivat.temptToPlayer(player);
                         kivat.pendingTransformPlayer = player.getUUID();
                    }
                } else {
                    KivatBatTwoNd.reply(query, kivat, (ServerLevel) level);
                }
            }
        }

        @SubscribeEvent
        public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer newPlayer) {
                // ⭐ 给新玩家发送所有已在线玩家的状态
                for (ServerPlayer onlinePlayer : newPlayer.server.getPlayerList().getPlayers()) {
                    if (onlinePlayer != newPlayer) {
                        syncPlayerState(onlinePlayer, newPlayer); // 发送给新玩家
                    }
                }

                // ⭐ 同步自己的状态给所有玩家
                syncPlayerState(newPlayer, null);
            }
        }

        // 在 modEventBusEvents.onPlayerLoggedIn 中：
        private void syncPlayerState(ServerPlayer targetPlayer, ServerPlayer excludePlayer) {
            CuriosApi.getCuriosInventory(targetPlayer).ifPresent(inv ->
                    inv.findCurio("belt", 0).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        if (stack.getItem() instanceof DrakKivaBelt belt && belt.getHenshin(stack)) {
                            PlayerJoinSyncPacket packet = new PlayerJoinSyncPacket(
                                    targetPlayer.getId(), "henshin", "drakkiva", "DEFAULT"
                            );
                            // 广播给所有追踪者（除了 excludePlayer）
                            PacketHandler.sendToAllTrackingExcept(packet, targetPlayer, excludePlayer);
                        }
                    })
            );
        }
    }


    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public final class CommonListener {

        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            // 统一使用自定义属性系统

            event.put(ModEntityTypes.LORD_BARON.get(), LordBaronEntity.createAttributes()
                    .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 15.0D)
                    .add(Attributes.MAX_HEALTH, 350.0D)
                    .add(Attributes.ARMOR, 15.0D)
                    .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                    .build()
            ); // 增强版：提高防御、韧性和生命值，添加击退抗性

            event.put(ModEntityTypes.GIIFUDEMOS_ENTITY.get(),
                    GiifuDemosEntity.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 12.0D)
                            .add(Attributes.MAX_HEALTH, 300.0D)
                            .add(Attributes.ARMOR, 12.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                            .build()
            ); // 增强版：提高防御、韧性和生命值，添加击退抗性

            event.put(ModEntityTypes.STORIOUS.get(),
                    StoriousEntity.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 14.0D)
                            .add(Attributes.MAX_HEALTH, 500.0D)
                            .add(Attributes.ARMOR, 20.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                            .build()
            ); // 强化版：大幅提高防御、韧性和生命值，增强击退抗性

            event.put(ModEntityTypes.GIFFTARIAN.get(),
                    Gifftarian.createMonsterAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 16.0D)
                            .add(Attributes.MAX_HEALTH, 350.0D)
                            .add(Attributes.ARMOR, 15.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                            .build()
            ); // 强化版：大幅提高防御、韧性和生命值，增强击退抗性

            event.put(ModEntityTypes.INVES_HEILEHIM.get(),
                    ElementaryInvesHelheim.createMonsterAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 10.0D)
                            .add(Attributes.MAX_HEALTH, 220.0D)
                            .add(Attributes.ARMOR, 12.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                            .build()
            ); // 强化版：大幅提高防御、韧性和生命值，增强击退抗性

            event.put(ModEntityTypes.ANOTHER_ZI_O.get(),
                    Monster.createMonsterAttributes()
                            .add(Attributes.MAX_HEALTH, 200.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.26D)      // ← 保持移速
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 8.0D)
                            .add(Attributes.ARMOR, 15.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性，增强击退抗性

            event.put(ModEntityTypes.KIVAT_BAT_II.get(),
                    KivatBatTwoNd.createAttributes()
                            .add(Attributes.MAX_HEALTH, 150.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.26D)
                            .add(Attributes.FLYING_SPEED, 0.6D)
                            .add(Attributes.ATTACK_DAMAGE, 8.0D)          // 必须给标准伤害
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 8.0D)
                            .add(Attributes.ARMOR, 12.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性

            event.put(ModEntityTypes.GIIFU_HUMAN.get(),
                    GiifuHumanEntity.createAttributes()
                            .add(Attributes.MAX_HEALTH, 800.0D)
                            .add(Attributes.ARMOR, 30.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 15.0D)
                            .add(Attributes.ATTACK_DAMAGE, 15.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.28D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 20.0D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性

            event.put(ModEntityTypes.BAT_DARKS.get(), BatDarksEntity.createAttributes());

            event.put(ModEntityTypes.BAT_STAMP_FINISH.get(), BatStampFinishEntity.createAttributes());

            event.put(ModEntityTypes.KNECROMGHOST.get(), KnecromghostEntity.createAttributes());

            event.put(ModEntityTypes.DUKE_KNIGHT.get(),DukeKnightEntity.createAttributes());

            event.put(ModEntityTypes.BARON_BANANA_ENERGY.get(), BaronBananaEnergyEntity.createAttributes());

            event.put(ModEntityTypes.BARON_LEMON_ENERGY.get(), BaronLemonEnergyEntity.createAttributes());

//            event.put(ModEntityTypes.KAITO.get(),
//                    KaitoVillager.createAttributes()
//                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 3.0D)
//                            .add(Attributes.ATTACK_DAMAGE, 3.0D) // ⚠️ 添加这行
//                            .add(Attributes.MAX_HEALTH, 40.0D)
//                            .build()
//            );
        }

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(CHANGE_KEY);
            event.register(CHANGES_KEY);
            event.register(RELIEVE_KEY);
            event.register(KEY_GUARD);
            event.register(KEY_BLAST);
            event.register(KEY_BOOST);
        }
    }
}