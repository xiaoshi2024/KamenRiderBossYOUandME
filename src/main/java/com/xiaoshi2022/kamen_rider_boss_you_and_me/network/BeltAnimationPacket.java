package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.function.Supplier;

public class BeltAnimationPacket {

    private final int entityId;
    private final String animationName;
    private final String beltType;
    private final String beltMode;

    /* ---------------- 构造 & 编解码 ---------------- */

    public BeltAnimationPacket(int entityId, String animationName, sengokudrivers_epmty.BeltMode mode) {
        this(entityId, animationName, "sengoku", mode.name());
    }

    public BeltAnimationPacket(int entityId, String animationName, Genesis_driver.BeltMode mode) {
        this(entityId, animationName, "genesis", mode.name());
    }

    public BeltAnimationPacket(int entityId, String animationName, DrakKivaBelt.DrakKivaBeltMode mode) {
        this(entityId, animationName, "drakkiva", mode.name());
    }

    public BeltAnimationPacket(int entityId, String animationName, Two_sidriver.DriverType mode) {
        this(entityId, animationName, "two_sidriver", mode.name());
    }

    public BeltAnimationPacket(int entityId, String animationName, String beltType, String beltMode) {
        this.entityId = entityId;
        this.animationName = animationName;
        this.beltType = beltType;
        this.beltMode = beltMode;
    }

    public static void encode(BeltAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.animationName);
        buf.writeUtf(msg.beltType);
        buf.writeUtf(msg.beltMode);
    }

    public static BeltAnimationPacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        String anim = buf.readUtf();
        String type = buf.readUtf();
        String mode = buf.readUtf();

        switch (type) {
            case "sengoku":
                return new BeltAnimationPacket(id, anim, sengokudrivers_epmty.BeltMode.valueOf(mode));
            case "genesis":
                return new BeltAnimationPacket(id, anim, Genesis_driver.BeltMode.valueOf(mode));
            case "drakkiva":
                return new BeltAnimationPacket(id, anim, DrakKivaBelt.DrakKivaBeltMode.valueOf(mode));
            case "two_sidriver":
                return new BeltAnimationPacket(id, anim, Two_sidriver.DriverType.valueOf(mode));
            default:
                throw new IllegalArgumentException("Unknown belt type: " + type);
        }
    }

    /* ---------------- 客户端处理 ---------------- */

    public static void handle(BeltAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) return;
            Entity e = Minecraft.getInstance().level.getEntity(msg.entityId);
            if (!(e instanceof LivingEntity living)) return;

            living.getCapability(CuriosCapability.INVENTORY).ifPresent(curios ->
                    curios.findCurio("belt", 0).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        Item item = stack.getItem();

                        if (item instanceof sengokudrivers_epmty s) {
                            s.triggerAnim(living, "controller", msg.animationName);
                        } else if (item instanceof Genesis_driver g) {
                            g.triggerAnim(living, "controller", msg.animationName);
                        } else if (item instanceof DrakKivaBelt dk) {
                            dk.triggerAnim(living, "controller", msg.animationName);
                        } else if (item instanceof Two_sidriver ts) {
                            ts.triggerAnim(living, "controller", msg.animationName);
                        }
                    })
            );
        });
        ctx.get().setPacketHandled(true);
    }


    /* ---------------- Getter 供调试 ---------------- */

    public int getEntityId()      { return entityId; }
    public String getAnimationName() { return animationName; }
    public String getBeltType()   { return beltType; }
    public String getBeltMode()   { return beltMode; }
}