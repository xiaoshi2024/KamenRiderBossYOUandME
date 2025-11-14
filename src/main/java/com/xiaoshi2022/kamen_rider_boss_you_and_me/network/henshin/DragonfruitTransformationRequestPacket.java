package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.DragonfruitBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.DragonfruitRiderHenshin;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class DragonfruitTransformationRequestPacket {

    private final UUID playerId;

    public DragonfruitTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    /* --------------------------------------------------------------------- */
    /*  编码 / 解码                                                            */
    /* --------------------------------------------------------------------- */
    public static void encode(DragonfruitTransformationRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
    }

    public static DragonfruitTransformationRequestPacket decode(FriendlyByteBuf buf) {
        return new DragonfruitTransformationRequestPacket(buf.readUUID());
    }

    /* --------------------------------------------------------------------- */
    /*  主逻辑                                                               */
    /* --------------------------------------------------------------------- */
    public static void handle(DragonfruitTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getUUID().equals(msg.playerId)) {
                return;
            }
            
            // 直接调用静态方法处理变身
            handleDragonfruitTransformation(player);
        });

        ctx.get().setPacketHandled(true);
    }
    
    /**
     * 服务器端直接调用的火龙果变身处理方法
     */
    public static void handleDragonfruitTransformation(ServerPlayer player) {
        /* 1. 是否已就绪 */
        KRBVariables.PlayerVariables vars = player.getCapability(
                KRBVariables.PLAYER_VARIABLES_CAPABILITY, null
        ).orElse(new KRBVariables.PlayerVariables());
        if (!vars.dragonfruit_ready) {
            player.displayClientMessage(Component.literal("请先装备火龙果锁种！"), true);
            return;
        }

        /* 2. 取得腰带 */
        Optional<SlotResult> beltOpt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(
                        stack -> stack.getItem() instanceof Genesis_driver));

        if (beltOpt.isEmpty()) return;

        ItemStack beltStack = beltOpt.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();
        
        // 检查腰带是否处于冷却状态（被瘫痪）
        if (beltStack.hasTag() && beltStack.getTag().contains("cooldownUntil")) {
            long cooldownUntil = beltStack.getTag().getLong("cooldownUntil");
            if (player.level().getGameTime() < cooldownUntil) {
                long remaining = (cooldownUntil - player.level().getGameTime()) / 20;
                player.displayClientMessage(Component.literal("腰带已被瘫痪！剩余时间：" + remaining + " 秒"), true);
                return;
            }
        }

        /* 3. 腰带模式检查 */
        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.DRAGONFRUIT) {
            player.displayClientMessage(Component.literal("腰带未设置为火龙果模式！"), true);
            return;
        }

        /* 4. 重复变身判定（可选）*/
        // TODO: 若有火龙果实装检查，可在此补充

        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "lemon_lockonby"
        );
        PacketHandler.sendToAllTracking(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        /* 6. 播放变身音效 */
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.DRAGONFRUIT_ARMS.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F);

        /* 6b. 火龙果榨汁瀑布·伞状扩散 */
        ServerLevel level = (ServerLevel) player.level();
        Vector3f color = new Vector3f(0.882f, 0.0f, 0.427f);
        float size = 1.4f;
        int total = 120;                     // 一次性粒子总量
        double topY = player.getY() + player.getBbHeight() + 2.0; // 起始高度
        double radius = 2.0;                 // 起始扩散圆半径

        for (int i = 0; i < total; i++) {
            // 均匀分布在圆盘内
            double theta = level.random.nextDouble() * Math.PI * 2;
            double dist = level.random.nextDouble() * radius;
            double sx = player.getX() + Math.cos(theta) * dist;
            double sz = player.getZ() + Math.sin(theta) * dist;
            double sy = topY + (level.random.nextDouble() - 0.5) * 0.2;

            // 速度：先往下，再随距离外扩
            double vy = -0.35 - level.random.nextDouble() * 0.15;
            double vx = Math.cos(theta) * dist * 0.08;
            double vz = Math.sin(theta) * dist * 0.08;

            level.sendParticles(
                    new DustParticleOptions(color, size),
                    sx, sy, sz,
                    1,
                    vx, vy, vz,
                    1.0
            );
        }

        cleardragonEntities(player);

        /* 7. 播放腰带动画 */
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "dragonfruit_move",
                        Genesis_driver.BeltMode.DRAGONFRUIT),
                player);

        /* 8. 换装 & 设置腰带状态 */
        DragonfruitRiderHenshin.trigger(player);   // 穿火龙果装甲
        belt.setEquipped(beltStack, false);
        belt.setMode(beltStack, Genesis_driver.BeltMode.DRAGONFRUIT);
        belt.setHenshin(beltStack, true);
        belt.setShowing(beltStack, false);
        belt.startHenshinAnimation(player, beltStack);

        /* 9. 同步 Curios 槽位 */
        SlotResult sr = beltOpt.get();
        CurioUtils.updateCurioSlot(
                player,
                sr.slotContext().identifier(),
                sr.slotContext().index(),
                beltStack);

        /* 10. 清除就绪标记并设置持续时间 */
        vars.dragonfruit_ready = false;
        vars.dragonfruit_time = 20 * 60; // 60 秒
        vars.syncPlayerVariables(player);
        
        // 给予玩家对应的武器（如果配置启用了武器给予功能）
        TransformationWeaponManager.giveWeaponOnGenesisDriverTransformation(player, Genesis_driver.BeltMode.DRAGONFRUIT);
    }

    private static void cleardragonEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof DragonfruitBlockEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}