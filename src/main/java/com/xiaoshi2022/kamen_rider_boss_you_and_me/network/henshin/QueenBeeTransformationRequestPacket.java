package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TickHandler;
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

        // 1. 立即触发变身b动画（henshin-b）
        belt.startHenshinBAnimation(player, beltStack);

        // 2. 立即播放玩家动画queenbeeb，无延迟
        PacketHandler.sendAnimationToAllTrackingAndSelf(
                "queenbeeb",
                player.getId(),
                true,
                player,
                1,
                4000
        );

        // 3. 立即播放变身完成音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.QUEENBE_COM.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        // 4. 立即更新状态和变量
        variables.queenBee_ready = false;
        variables.isQueenBeeTransformed = true;
        variables.syncPlayerVariables(player);

        // 5. 立即更新Curio槽位
        CurioUtils.updateCurioSlot(
                player,
                weekendriver.slotContext().identifier(),
                weekendriver.slotContext().index(),
                beltStack);

        // 6. 发送变身成功提示
        player.sendSystemMessage(Component.literal("女王蜂形态已激活！"));

        // 7. 设置延迟装备（60 ticks = 3秒后装备）
        // 使用TickHandler来处理延迟
        TickHandler.setQueenBeeArmorDelay(player.getUUID(), 60);

        System.out.println("已设置女王蜂装甲延迟装备: 玩家=" + player.getName().getString() + ", 延迟=60ticks");
    }
}