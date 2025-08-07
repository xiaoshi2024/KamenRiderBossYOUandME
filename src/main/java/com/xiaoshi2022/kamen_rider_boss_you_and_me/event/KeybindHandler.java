package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class KeybindHandler {
    private static boolean keyCooldown = false;
    private static int delayTicks = 0;
    private static ItemStack delayedBeltStack = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            if (KeyBinding.RELIEVE_KEY.isDown() && !keyCooldown) {
                keyCooldown = true;
                handleKeyPress(player);
            } else if (!KeyBinding.RELIEVE_KEY.isDown()) {
                keyCooldown = false;
            }

            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && !delayedBeltStack.isEmpty()) {
                    // 发送包含动画信息的网络包
                    PacketHandler.sendToServer(new ReleaseBeltPacket(true));
                    delayedBeltStack = ItemStack.EMPTY;
                }
            }
        }
    }

    private static void handleKeyPress(Player player) {
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof sengokudrivers_epmty)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

                    if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA) {
                        // 立即发送动画触发包给服务器
                        PacketHandler.sendToServer(new ReleaseBeltPacket(false));

                        // 本地播放动画
                        belt.startReleaseAnimation(player);
                        delayTicks = 40;
                        delayedBeltStack = beltStack.copy();
                    }
                });
    }

    // 这个方法应该在服务端调用
    public static void completeBeltRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof sengokudrivers_epmty));

        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

            if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA) {
                // 创建新的物品堆栈
                ItemStack newStack = beltStack.copy();
                belt.setMode(newStack, sengokudrivers_epmty.BeltMode.DEFAULT);

                // 更新槽位
                CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(), newStack);

                // 播放解除变身音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                // 停止变身音效
                PacketHandler.sendToServer(new SoundStopPacket());

                // 清空玩家身上的变身装甲
                clearTransformationArmor(player);

                // 给予香蕉锁种
                ItemStack bananaLockSeed = new ItemStack(ModItems.BANANAFRUIT.get());
                if (!player.getInventory().add(bananaLockSeed)) {
                    player.spawnAtLocation(bananaLockSeed);
                }

                belt.isEquipped = false;

                // 同步给客户端
                player.inventoryMenu.broadcastChanges();
            }
        }
    }
//解除卸甲
    private static void clearTransformationArmor(ServerPlayer player) {
        // 清空玩家身上的变身装甲
        for (int i = 1; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
    }
}