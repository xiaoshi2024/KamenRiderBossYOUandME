package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class WeaponRemovePacket {
    private final boolean isBatForm;

    public WeaponRemovePacket(boolean isBatForm) {
        this.isBatForm = isBatForm;
    }

    public static void encode(WeaponRemovePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isBatForm);
    }

    public static WeaponRemovePacket decode(FriendlyByteBuf buf) {
        return new WeaponRemovePacket(buf.readBoolean());
    }

    public static void handle(WeaponRemovePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // 在服务端执行实际的武器取下逻辑
                handleWeaponRemoval(player, msg.isBatForm);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleWeaponRemoval(ServerPlayer player, boolean isBatForm) {
        // 获取腰带
        Optional<SlotResult> beltOpt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));

        if (beltOpt.isEmpty()) return;

        SlotResult slotResult = beltOpt.get();
        ItemStack beltStack = slotResult.stack();
        Two_sidriver belt = (Two_sidriver) beltStack.getItem();

        // 修改腰带状态
        if (isBatForm) {
            belt.setDriverType(beltStack, Two_sidriver.DriverType.DEFAULT);
        }

        // 更新Curios槽位
        CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                slotResult.slotContext().index(), beltStack);

        // 同步腰带状态
        Two_sidriver.syncToTracking(player, beltStack);
        Two_sidriver.syncToSelf(player, beltStack);

        // 处理武器
        handleWeaponItems(player, isBatForm);

        // 播放音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void handleWeaponItems(ServerPlayer player, boolean isBatForm) {
        // 处理主手武器
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof TwoWeaponItem) {
            TwoWeaponItem.setVariant(mainHand, TwoWeaponItem.Variant.DEFAULT);
        } else if (mainHand.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem || 
                   mainHand.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem) {
            // 对于新的武器类，也需要重置其变体状态
            com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.setVariant(mainHand, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant.DEFAULT);
        }

        // 处理副手武器
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof TwoWeaponItem) {
            TwoWeaponItem.setVariant(offHand, TwoWeaponItem.Variant.DEFAULT);
        } else if (offHand.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem || 
                  offHand.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem) {
            // 对于新的武器类，也需要重置其变体状态
            com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.setVariant(offHand, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant.DEFAULT);
        }

        // 同步武器状态
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant mainVariant = com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.getVariant(mainHand);
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant offVariant = com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.getVariant(offHand);
        WeaponSyncPacket packet = new WeaponSyncPacket(player.getId(), mainVariant, offVariant);
        PacketHandler.sendToAllTrackingAndSelf(packet, player);

        // 返还物品
        if (isBatForm) {
            ItemStack batWeapon = new ItemStack(ModItems.TWO_WEAPON.get());
            TwoWeaponItem.setVariant(batWeapon, TwoWeaponItem.Variant.BAT);
            if (!player.getInventory().add(batWeapon)) {
                player.spawnAtLocation(batWeapon);
            }
        }
    }
}