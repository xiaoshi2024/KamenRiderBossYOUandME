package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosCapability;

public class BeltAnimationPacket {
    private final int entityId;
    private final String animationName;
    private final String beltType;
    private final String beltMode;

    public BeltAnimationPacket(int entityId, String animationName, sengokudrivers_epmty.BeltMode beltMode) {
        this.entityId = entityId;
        this.animationName = animationName;
        this.beltType = "sengoku";
        this.beltMode = beltMode.name();
    }

    public BeltAnimationPacket(int entityId, String animationName, Genesis_driver.BeltMode beltMode) {
        this.entityId = entityId;
        this.animationName = animationName;
        this.beltType = "genesis";
        this.beltMode = beltMode.name();
    }

    public static void encode(BeltAnimationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeUtf(msg.animationName);
        buffer.writeUtf(msg.beltType);
        buffer.writeUtf(msg.beltMode);
    }

    public static BeltAnimationPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        String animationName = buffer.readUtf();
        String beltType = buffer.readUtf();
        String beltMode = buffer.readUtf();

        if ("sengoku".equals(beltType)) {
            return new BeltAnimationPacket(
                    entityId,
                    animationName,
                    sengokudrivers_epmty.BeltMode.valueOf(beltMode)
            );
        } else {
            return new BeltAnimationPacket(
                    entityId,
                    animationName,
                    Genesis_driver.BeltMode.valueOf(beltMode)
            );
        }
    }

    public static void handle(BeltAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Entity entity = mc.level.getEntity(msg.getEntityId());
            if (!(entity instanceof LivingEntity living)) return;

            living.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
                curios.findCurio("belt", 0).ifPresent(slotResult -> {
                    ItemStack stack = slotResult.stack();
                    Item item = stack.getItem();

                    if (item instanceof Genesis_driver g) {
                        g.triggerAnim(living, "controller", msg.getAnimationName());
                    } else if (item instanceof sengokudrivers_epmty s) {
                        s.triggerAnim(living, "controller", msg.getAnimationName());
                    }
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private static void processPacket(BeltAnimationPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.entityId);
        if (!(entity instanceof LivingEntity living)) return;

        /* 1. 找到腰带 Item（不需要读 NBT） */
        living.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
            curios.findCurio("belt", 0).ifPresent(slotResult -> {
                ItemStack stack = slotResult.stack();
                Item item = stack.getItem();

                /* 2. 直接播动画，不再二次计算 */
                if (item instanceof sengokudrivers_epmty) {
                    ((sengokudrivers_epmty) item).triggerAnim(living, "controller", msg.animationName);
                } else if (item instanceof Genesis_driver) {
                    ((Genesis_driver) item).triggerAnim(living, "controller", msg.animationName);
                }
            });
        });
    }

    private static void updateSengokuBeltState(sengokudrivers_epmty belt, ItemStack stack, LivingEntity entity, sengokudrivers_epmty.BeltMode mode) {
        belt.currentMode = mode;

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BeltMode")) {
            belt.currentMode = sengokudrivers_epmty.BeltMode.valueOf(tag.getString("BeltMode"));
        }
        if (tag.contains("IsEquipped")) {
            belt.isEquipped = tag.getBoolean("IsEquipped");
        }

        String animation = belt.isEquipped ? (belt.currentMode == sengokudrivers_epmty.BeltMode.BANANA ? "banana_idle" : "show") : "idle";
        // 确保动画名称正确映射
        belt.triggerAnim(entity, "controller", animation);

        entity.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
            curios.getCurios().get("belt").update();
        });
    }

    private static void updateGenesisBeltState(
            Genesis_driver belt,
            ItemStack stack,
            LivingEntity entity,
            String animationName) {

        // 1. 直接播服务端指定的动画
        belt.triggerAnim(entity, "controller", animationName);

        // 2. 通知 Curios 刷新（可选）
        entity.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
            curios.getCurios().get("belt").update();
        });
    }

    public int getEntityId() {
        return entityId;
    }

    public String getAnimationName() {
        return animationName;
    }

    public String getBeltType() {
        return beltType;
    }

    public String getBeltMode() {
        return beltMode;
    }
}