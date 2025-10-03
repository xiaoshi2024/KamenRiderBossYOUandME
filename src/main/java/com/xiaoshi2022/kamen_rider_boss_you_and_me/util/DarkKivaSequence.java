package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DarkKivaSequence {

    // 玩家 UUID -> 蝙蝠 UUID（null 代表要新生成）
    public static final Map<UUID, UUID> BAT_RESTORE_MAP = new ConcurrentHashMap<>();


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

        // 立即广播变身动画
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "henshin", DrakKivaBelt.DrakKivaBeltMode.DEFAULT),
                player
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

        /* 5. 保存玩家原版盔甲 */
	KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
	variables.isDarkKivaBeltEquipped = true;
	variables.syncPlayerVariables(player);
	
	/* 6. 延迟穿戴盔甲 */
	applyArmorAfterDelay(player, 125); // 100 ticks = 5秒
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
		HENSHIN_COOLDOWN.put(player.getUUID(), delayTicks);
	}

    /* 1. 在 DarkKivaSequence 里建一个 Map 记录倒计时 */
    private static final Map<UUID,Integer> HENSHIN_COOLDOWN = new ConcurrentHashMap<>();

    /* 3. 用 Forge TickEvent 每 tick 减 1 */
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide()) return;
        ServerPlayer player = (ServerPlayer) e.player;
        UUID uuid = player.getUUID();
        if (!HENSHIN_COOLDOWN.containsKey(uuid)) return;

        int left = HENSHIN_COOLDOWN.get(uuid) - 1;
        if (left <= 0) {
            HENSHIN_COOLDOWN.remove(uuid);
            if (!player.isAlive()) return;
            // 重新拿最新实例
            player = (ServerPlayer) player.level().getPlayerByUUID(uuid);
            if (player == null) return;

            // 真正穿盔甲
            // 使用DarkKivaArmorSequence来装备盔甲，同时保存玩家原版盔甲
            com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkKivaArmorSequence.equip(player);
        } else {
            HENSHIN_COOLDOWN.put(uuid, left);
        }
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
// 先把主人写进去，再生成
        CompoundTag beltTag = beltStack.getOrCreateTag();
        beltTag.putUUID("OwnerUUID", player.getUUID());   // 关键！
        spawnKivatFromBelt(player, level, beltStack);

        // 在解除变身后允许再次播放
        DrakKivaBelt.setHenshin(beltStack, false);
        beltStack.getOrCreateTag().remove("HenshinHandled");

        /* 5. 删除腰带 */
        beltStack.shrink(1);
    }

    /* 移除盔甲并恢复玩家原版盔甲 */
    private static void removeArmor(ServerPlayer player) {
        // 现在盔甲在装备时已经立即返还，这里只需要清空盔甲槽位
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
        
        // 重置黑暗Kiva腰带状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        variables.isDarkKivaBeltEquipped = false;
        variables.syncPlayerVariables(player);
    }

    /* 从腰带生成Kivat蝙蝠 */
    private static void spawnKivatFromBelt(ServerPlayer player, ServerLevel level, ItemStack beltStack) {
        UUID restoreId = BAT_RESTORE_MAP.remove(player.getUUID());
        
        // 先获取腰带中的蝙蝠UUID
        CompoundTag beltTag = beltStack.getOrCreateTag();
        UUID beltBatId = beltTag.hasUUID("UUID") ? beltTag.getUUID("UUID") : null;
        
        // 关键修复：移除所有已经存在的旧蝙蝠实体，确保只保留一个
        level.getEntitiesOfClass(KivatBatTwoNd.class, player.getBoundingBox().inflate(32))
             .stream()
             .filter(b -> b.isOwnedBy(player))  // 只移除属于该玩家的蝙蝠
             .forEach(Entity::discard);

        KivatBatTwoNd bat;

        if (restoreId == null) {
            /* 生成全新的蝙蝠 */
            bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
        } else {
            /* 尝试取回旧实体 - 由于上面已经移除了所有旧实体，这里实际会创建新实体 */
            bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
            if (beltTag.contains("UUID")) {
                bat.load(beltTag);
            }
        }

        bat.moveTo(player.getX(), player.getY() + 0.5, player.getZ());
        bat.setDeltaMovement(Vec3.ZERO);
        bat.tame(player);          // 无论哪种情况，都重新绑定主人
        level.addFreshEntity(bat);
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