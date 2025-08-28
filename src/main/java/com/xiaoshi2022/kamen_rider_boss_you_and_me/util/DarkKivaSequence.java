package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class DarkKivaSequence {

    /* ========== 变身序列 ========== */
    public static void startHenshin(ServerPlayer player) {
        if (player.getInventory().armor.get(3).getItem() == ModItems.DARK_KIVA_HELMET.get()) {
            return; // 已经穿戴盔甲，不再重复变身
        }

        ServerLevel level = (ServerLevel) player.level();

        /* 1. 播放变身动画 */
        playPlayerAnimation(player, "raise");

        /* 2. 设置腰带变身状态 */
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findFirstCurio(stack -> stack.getItem() instanceof DrakKivaBelt)
                        .ifPresent(slot -> {
                            ItemStack belt = slot.stack();
                            DrakKivaBelt.setHenshin(belt, true);
                        })
        );

        /* 3. 播放音效 */
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.DARK_KIVAS.get(),
                SoundSource.PLAYERS, 1f, 1f
        );

        /* 4. 播放变身粒子效果 */
        spawnHenshinParticles(player, level);

        /* 5. 延迟穿戴盔甲 */
        applyArmorAfterDelay(player, 300); // 300 ticks = 15秒
    }

    /* 播放变身粒子效果 */
    private static void spawnHenshinParticles(ServerPlayer player, ServerLevel level) {
        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleTypes.FLASH,
                    player.getX() + (level.random.nextDouble() - 0.5) * 2,
                    player.getY() + level.random.nextDouble() * 2,
                    player.getZ() + (level.random.nextDouble() - 0.5) * 2,
                    1, 0, 0, 0, 0
            );
        }
    }

    /* 延迟穿戴盔甲 */
    public static void applyArmorAfterDelay(ServerPlayer player, int delayTicks) {
        ServerLevel level = (ServerLevel) player.level();

        level.getServer().tell(
                new net.minecraft.server.TickTask(
                        level.getServer().getTickCount() + delayTicks,
                        () -> {
                            if (!player.isAlive()) return;
                            if (player.getInventory().armor.get(3).getItem() == ModItems.DARK_KIVA_HELMET.get()) return;

                            // 穿戴盔甲
                            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_KIVA_HELMET.get()));
                            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_KIVA_CHESTPLATE.get()));
                            player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_KIVA_LEGGINGS.get()));

                            System.out.println("Dark Kiva 盔甲已穿戴");
                        }
                )
        );
    }

    /* ========== 解除变身序列 ========== */
    public static void startDisassembly(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findFirstCurio(s -> s.getItem() instanceof DrakKivaBelt)
                        .ifPresent(slot -> doFullDisassembly(player, slot.stack()))
        );
    }

    /* 完整解除变身 */
    public static void doFullDisassembly(ServerPlayer player, ItemStack beltStack) {
        ServerLevel level = (ServerLevel) player.level();

        /* 1. 播放解除动画 */
        playPlayerAnimation(player, "disassembly");

        /* 2. 立即卸盔甲 */
        removeArmor(player);

        /* 3. 播放解除音效 */
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.DRAK_KIVA_DISASSEMBLY.get(),
                SoundSource.PLAYERS, 1f, 1f
        );

        /* 4. 读取腰带数据并生成蝙蝠 */
        spawnKivatFromBelt(player, level, beltStack);

        /* 5. 删除腰带 */
        beltStack.shrink(1);
    }

    /* 移除盔甲 */
    private static void removeArmor(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    /* 从腰带生成Kivat蝙蝠 */
    private static void spawnKivatFromBelt(ServerPlayer player, ServerLevel level, ItemStack beltStack) {
        CompoundTag tag = beltStack.getOrCreateTag();
        UUID beltUUID = tag.hasUUID("UUID") ? tag.getUUID("UUID") : UUID.randomUUID();
        UUID ownerUUID = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : player.getUUID();
        String name = tag.contains("CustomName") ? tag.getString("CustomName") : null;

        // 保底血量
        float health = tag.contains("Health", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)
                ? tag.getFloat("Health")
                : 79.0F;

        KivatBatTwoNd bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
        if (bat != null) {
            CompoundTag batTag = new CompoundTag();
            batTag.putUUID("UUID", beltUUID);
            batTag.putUUID("OwnerUUID", ownerUUID);
            if (name != null) batTag.putString("CustomName", name);
            batTag.putFloat("Health", health);

            bat.load(batTag);
            bat.moveTo(player.getX(), player.getY() + 0.5, player.getZ());
            bat.setDeltaMovement(Vec3.ZERO);
            bat.tame(player);
            level.addFreshEntity(bat);
        }
    }

    /* ========== 玩家动画支持 ========== */
    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player.level().isClientSide()) return;

        PacketHandler.sendAnimationToAll(
                Component.literal(animationName),
                player.getId(),
                false
        );
    }

    private DarkKivaSequence() {}
}