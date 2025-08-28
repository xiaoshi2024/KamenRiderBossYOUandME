package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class DarkKivaSequence {

    /* ========== 变身 ========== */
    public static void startHenshin(ServerPlayer player) {
        if (player.getInventory().armor.get(3).getItem() == ModItems.DARK_KIVA_HELMET.get()) return;

        ServerLevel level = (ServerLevel) player.level();

        /* 1. 确保腰带处于变身状态（仅标记，不穿盔甲） */
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findFirstCurio(stack -> stack.getItem() instanceof DrakKivaBelt)
                        .ifPresent(slot -> {
                            ItemStack belt = slot.stack();
                            DrakKivaBelt.setHenshin(belt, true);
                            DrakKivaBelt.playAnimation(player, "henshin");
                        })
        );

        /* 2. 播放音效 */
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.DARK_KIVAS.get(),
                SoundSource.PLAYERS, 1f, 1f
        );

        /* 3. 延迟 400 tick 穿盔甲 */
        applyArmorAfterDelay(player);
    }

    /* 仅负责 400 tick 后穿盔甲，不重复触发音效、不重复设置状态 */
    public static void applyArmorAfterDelay(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();   // ← 先拿到 level

        level.getServer().tell(
                new net.minecraft.server.TickTask(
                        level.getServer().getTickCount() + 400,
                        () -> {
                            if (!player.isAlive()) return;
                            if (player.getInventory().armor.get(3).getItem() == ModItems.DARK_KIVA_HELMET.get()) return;

                            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_KIVA_HELMET.get()));
                            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_KIVA_CHESTPLATE.get()));
                            player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_KIVA_LEGGINGS.get()));
                            System.out.println("Dark Kiva 盔甲已穿戴");
                        }
                )
        );
    }

    /* ========== 解除变身 ========== */
    /* 供按键调用：只负责找到腰带并执行完整解除 */
    public static void startDisassembly(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findFirstCurio(s -> s.getItem() instanceof DrakKivaBelt)
                        .ifPresent(slot -> doFullDisassembly(player, slot.stack()))
        );
    }

    /* ===== 工具方法：立即清盔甲 ===== */
        private static void removeArmor(ServerPlayer player) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }

    private DarkKivaSequence() {}

    /* 统一入口：卸盔甲 + 播音效 + 刷蝙蝠 + 删腰带 */
    public static void doFullDisassembly(ServerPlayer player,
                                         ItemStack beltStack) {
        ServerLevel level = (ServerLevel) player.level();

        /* 1. 立即卸盔甲 */
        removeArmor(player);

        /* 2. 播放解除音效 */
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.DRAK_KIVA_DISASSEMBLY.get(),
                SoundSource.PLAYERS, 1f, 1f
        );

        /* 3. 读取腰带 NBT */
        CompoundTag tag = beltStack.getOrCreateTag();
        UUID beltUUID   = tag.hasUUID("UUID")      ? tag.getUUID("UUID")      : UUID.randomUUID();
        UUID ownerUUID  = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : player.getUUID();
        String name     = tag.contains("CustomName") ? tag.getString("CustomName") : null;
        float health    = tag.getFloat("Health");

        /* 4. 立即生成蝙蝠（不用再等 100 tick） */
        KivatBatTwoNd bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
        if (bat != null) {
            CompoundTag batTag = new CompoundTag();
            batTag.putUUID("UUID", beltUUID);
            batTag.putUUID("OwnerUUID", ownerUUID);
            if (name != null) batTag.putString("CustomName", name);

            /* 关键：保底血量 */
            float healths = tag.contains("Health", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)
                    ? tag.getFloat("Health")
                    : 79.0F;
            batTag.putFloat("Health", healths);

            bat.load(batTag);
            bat.moveTo(player.getX(), player.getY() + 0.5, player.getZ());
            bat.setDeltaMovement(Vec3.ZERO);
            bat.tame(player);
            level.addFreshEntity(bat);
        }

        /* 5. 删除腰带 */
        beltStack.shrink(1);
    }
}