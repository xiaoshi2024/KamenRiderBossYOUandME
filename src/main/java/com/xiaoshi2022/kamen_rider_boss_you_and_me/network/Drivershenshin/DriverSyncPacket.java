package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class DriverSyncPacket {

    private final int entityId;
    private final Two_sidriver.DriverType driverType;
    private final Two_sidriver.WeaponMode weaponMode;

    public DriverSyncPacket(int entityId,
                            Two_sidriver.DriverType driverType,
                            Two_sidriver.WeaponMode weaponMode) {
        this.entityId   = entityId;
        this.driverType = driverType;
        this.weaponMode = weaponMode;
    }

    /* ---------------- 编解码 ---------------- */
    public static void encode(DriverSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeEnum(msg.driverType);
        buf.writeEnum(msg.weaponMode);
    }

    public static DriverSyncPacket decode(FriendlyByteBuf buf) {
        int entityId   = buf.readInt();
        Two_sidriver.DriverType type  = buf.readEnum(Two_sidriver.DriverType.class);
        Two_sidriver.WeaponMode mode  = buf.readEnum(Two_sidriver.WeaponMode.class);
        return new DriverSyncPacket(entityId, type, mode);
    }

    /* ---------------- 客户端处理 ---------------- */
    public static void handle(DriverSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端处理，使用完整类名避免服务器导入错误
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                if (net.minecraft.client.Minecraft.getInstance().level == null) return;
                Entity e = net.minecraft.client.Minecraft.getInstance().level.getEntity(msg.entityId);
                if (e == null || !(e instanceof LivingEntity living)) return;

                CuriosApi.getCuriosInventory(living).ifPresent(inv ->
                        inv.findFirstCurio(item -> item.getItem() instanceof Two_sidriver)
                                .ifPresent(slot -> {
                                    ItemStack stack = slot.stack();

                                    /* 只要写 NBT，GeckoLib 会自动重建动画实例 */
                                    Two_sidriver.setDriverType(stack, msg.driverType);
                                    Two_sidriver.setWeaponMode(stack, msg.weaponMode);
                                })
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}