package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
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
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                processPacket(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void processPacket(BeltAnimationPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.entityId);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
                curios.findCurio("belt", 0).ifPresent(slotResult -> {
                    ItemStack stack = slotResult.stack();
                    Item item = stack.getItem();

                    if (item instanceof sengokudrivers_epmty belt && "sengoku".equals(msg.beltType)) {
                        updateSengokuBeltState(belt, stack, livingEntity,
                                sengokudrivers_epmty.BeltMode.valueOf(msg.beltMode));
                    }
                    else if (item instanceof Genesis_driver belt && "genesis".equals(msg.beltType)) {
                        updateGenesisBeltState(belt, stack, livingEntity,
                                Genesis_driver.BeltMode.valueOf(msg.beltMode));
                    }
                });
            });
        }
    }

    private static void updateSengokuBeltState(sengokudrivers_epmty belt, ItemStack stack,
                                               LivingEntity entity, sengokudrivers_epmty.BeltMode mode) {
        // 更新本地状态
        belt.currentMode = mode;

        // 从NBT读取完整状态
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BeltMode")) {
            belt.currentMode = sengokudrivers_epmty.BeltMode.valueOf(tag.getString("BeltMode"));
        }
        if (tag.contains("IsEquipped")) {
            belt.isEquipped = tag.getBoolean("IsEquipped");
        }

        // 触发正确动画
        String animation = belt.isEquipped ?
                (belt.currentMode == sengokudrivers_epmty.BeltMode.BANANA ? "banana_idle" : "show") : "idle";
        belt.triggerAnim(entity, "controller", animation);

        // 刷新槽位
        entity.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
            curios.getCurios().get("belt").update();
        });
    }

    private static void updateGenesisBeltState(Genesis_driver belt, ItemStack stack,
                                               LivingEntity entity, Genesis_driver.BeltMode mode) {
        // 更新本地状态
        belt.currentMode = mode;

        // 从NBT读取完整状态
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BeltMode")) {
            belt.currentMode = Genesis_driver.BeltMode.valueOf(tag.getString("BeltMode"));
        }
        if (tag.contains("IsActive")) {
            belt.isActive = tag.getBoolean("IsActive");
        }
        if (tag.contains("IsShowing")) {
            belt.isShowing = tag.getBoolean("IsShowing");
        }

        // 触发正确动画
        String animation = "idles";
        if (belt.isActive) {
            animation = (belt.currentMode == Genesis_driver.BeltMode.LEMON) ? "start" : "show";
        } else if (belt.isShowing) {
            animation = "show";
        }
        belt.triggerAnim(entity, "controller", animation);

        // 刷新槽位
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