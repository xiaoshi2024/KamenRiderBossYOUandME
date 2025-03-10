package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tocraft.walkers.api.PlayerShape;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class Playerchanger {
    @SubscribeEvent
    public static void onKeyInput(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;

            if (player != null && KeyBinding.CHANGES_KEY.isDown()) {
                //检测玩家手上是否拿着StoriousMonsterBook
                if (player.getMainHandItem().getItem() == ModItems.STORIOUSMONSTERBOOK.get().asItem()) {
                // 按键被按下，触发变形逻辑
                transformPlayerToStoriousEntity(player);
                }
            }
        }
    }

    private static void transformPlayerToStoriousEntity(LocalPlayer player) {
        // 获取服务器玩家实例
        if (player == null || Minecraft.getInstance().getSingleplayerServer() == null) return;
        ServerPlayer serverPlayer = Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(player.getUUID());
        if (serverPlayer == null) return;

        // 创建 StoriousEntity 实例
        EntityType<StoriousEntity> storiousEntityType = ModEntityTypes.STORIOUS.get();
        StoriousEntity storiousEntity = storiousEntityType.create(player.level());
        if (storiousEntity == null) {
            System.err.println("Failed to create StoriousEntity");
            return;
        }

        // 初始化实体并设置位置
        storiousEntity.moveTo(player.getX(), player.getY(), player.getZ());
        if (!storiousEntity.isAlive()) {
            System.err.println("StoriousEntity initialization failed");
            return;
        }

        // 调用 PlayerShape.updateShapes 方法进行变形
        if (PlayerShape.updateShapes(serverPlayer, storiousEntity)) {
            System.out.println("Player transformed to StoriousEntity successfully");
        } else {
            System.err.println("Player transformation failed");
        }
    }
}
