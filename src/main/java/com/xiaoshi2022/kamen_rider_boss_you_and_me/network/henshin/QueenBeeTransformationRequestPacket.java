package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class QueenBeeTransformationRequestPacket {
    private final UUID playerId;

    public QueenBeeTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(QueenBeeTransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static QueenBeeTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new QueenBeeTransformationRequestPacket(playerId);
    }

    public static void handle(QueenBeeTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleQueenBeeTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * 服务器端直接调用的女王蜂变身处理方法
     */
    public static void handleQueenBeeTransformation(ServerPlayer player) {
        Optional<SlotResult> weekendriver = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof WeekEndriver));

        if (weekendriver.isPresent()) {
            handleWeekEndriverQueenBeeTransformation(player, weekendriver.get());
        }
    }

    static void handleWeekEndriverQueenBeeTransformation(ServerPlayer player, SlotResult weekendriver) {
        ItemStack beltStack = weekendriver.stack();
        WeekEndriver belt = (WeekEndriver) beltStack.getItem();

        // 检查玩家是否已经装备了变身盔甲
        boolean isQuinbeeArmor = player.getInventory().armor.get(3).getItem().toString().contains("quinbee") &&
                player.getInventory().armor.get(2).getItem().toString().contains("quinbee") &&
                player.getInventory().armor.get(1).getItem().toString().contains("quinbee");

        if (isQuinbeeArmor) {
            System.out.println("玩家已经装备了女王蜂盔甲，不允许再次变身");
            return;
        }

        // 检查女王蜂准备状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.queenBee_ready) {
            player.sendSystemMessage(Component.literal("请先装备女王蜂印章！"));
            return;
        }

        // 检查腰带模式是否为女王蜂
        if (belt.getMode(beltStack) != WeekEndriver.BeltMode.QUEEN_BEE) {
            player.sendSystemMessage(Component.literal("腰带未设置为女王蜂模式！"));
            return;
        }

        // 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "queenbe_by"
        );
        PacketHandler.sendToAllTrackingAndSelf(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );

        // ===== 关键修改：先触发变身b动画 =====
        // 触发变身b动画（henshin-b）
        belt.startHenshinBAnimation(player, beltStack);

        // 等待一小段时间让动画开始播放，然后装备装甲
        player.getServer().execute(() -> {
            try {
                // 等待500ms，确保动画开始播放
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // ===== 修改这里：不再通过HeartCoreEvent触发动画 =====
            // 直接装备女王蜂装甲（跳过HeartCoreEvent的动画调用）
            equipQueenBeeArmor(player);

            // 播放变身完成音效（女王蜂变身音效）
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModBossSounds.QUEENBE_COM.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            // 清除女王蜂准备状态
            variables.queenBee_ready = false;
            variables.syncPlayerVariables(player);

            // 写完 NBT 立刻同步回 Curios
            CurioUtils.updateCurioSlot(
                    player,
                    weekendriver.slotContext().identifier(),
                    weekendriver.slotContext().index(),
                    beltStack);

            // 发送变身成功提示
            player.sendSystemMessage(Component.literal("女王蜂形态已激活！"));
        });
    }

    // 新增：直接装备女王蜂装甲的方法
    private static void equipQueenBeeArmor(ServerPlayer player) {
        ItemStack[] originalArmor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            originalArmor[i] = player.getInventory().armor.get(i).copy();
        }

        // 装备女王蜂装甲
        player.getInventory().armor.set(3, new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.QUINBEE_HELMET.get()));
        player.getInventory().armor.set(2, new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.QUINBEE_CHESTPLATE.get()));
        player.getInventory().armor.set(1, new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.QUINBEE_LEGGINGS.get()));
        player.getInventory().armor.set(0, ItemStack.EMPTY); // 清空鞋子

        // 恢复原装备到背包
        for (int i = 0; i < 4; i++) {
            if (!originalArmor[i].isEmpty()) {
                if (!player.getInventory().add(originalArmor[i])) {
                    player.drop(originalArmor[i], false);
                }
            }
        }
    }
}