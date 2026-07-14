package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.config.FormConfig;
import com.jpigeon.ridebattlelib.common.data.HenshinSessionData;
import com.jpigeon.ridebattlelib.common.data.RiderAttachments;
import com.jpigeon.ridebattlelib.common.data.RiderData;
import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.event.UnhenshinDelayHandler;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.build.BloodConfig;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class BloodHandler {

    private static final Map<UUID, Long> lastHenshinTime = new HashMap<>();
    private static final Map<UUID, Boolean> unhenshinInProgress = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100;

    // ==================== 事件监听 ====================

    @SubscribeEvent
    public static void onHenshinPre(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();
        if (!riderId.equals(RiderIds.BLOOD_ID)) return;

        // 取消原有变身，我们自己控制
        event.setCanceled(true);

        // 如果已经变身则跳过
        if (RideBattleAPI.isTransformed(player)) return;

        // ✅ 移除或改为 DEBUG 级别
        // KamenRiderBossYOUandME.LOGGER.info("Blood变身被阻止：需要通过长按变身键触发摇动！");

        // 可选：给玩家提示（但频繁触发会刷屏，建议只在特定情况显示）
        // if (!player.level().isClientSide()) {
        //     player.displayClientMessage(
        //         net.minecraft.network.chat.Component.literal("§c请长按变身键触发摇动！"),
        //         true
        //     );
        // }
    }

    /**
     * 变身完成后的处理
     */
    @SubscribeEvent
    public static void onHenshinPost(HenshinEvent.Post event) {
        Player player = event.getPlayer();
        ResourceLocation riderId = event.getRiderId();

        if (!riderId.equals(RiderIds.BLOOD_ID)) return;

        KamenRiderBossYOUandME.LOGGER.info("🎉 Blood变身完成！玩家: {}", player.getName().getString());

        if (!player.level().isClientSide()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c❤️ Blood 形态！"),
                    true
            );
        }
    }

    /**
     * 解除变身
     */
    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();
        if (riderId.equals(RiderIds.BLOOD_ID)) {
            if (!UnhenshinDelayHandler.hasPendingUnhenshin(player.getUUID())) {
                event.setCanceled(true);
                performUnhenshin(player);
            }
        }
    }

    // ==================== 由 BuildDriver 调用 ====================

    /**
     * 摇动完成回调 - 由 BuildDriver.stopShaking 在动画播放完后调用
     */
    public static void onShakingComplete(Player player, ItemStack beltStack) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        KamenRiderBossYOUandME.LOGGER.info("🎬 摇动完成，开始执行变身!");

        // 设置腰带状态
        BuildDriver belt = (BuildDriver) beltStack.getItem();
        belt.setShowing(beltStack, false);
        belt.setActive(beltStack, true);

        // 延迟后完成变身
        RideBattleAPI.scheduleTicks(10, () -> {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
                try {
                    forceCompleteHenshin(serverPlayer);
                    KamenRiderBossYOUandME.LOGGER.info("✅ Blood变身完成: {}", player.getName().getString());
                } catch (Exception e) {
                    KamenRiderBossYOUandME.LOGGER.error("❌ 完成Blood变身失败", e);
                }
            }
        });
    }

    // ==================== 条件检查 ====================

    private static boolean checkBloodConditions(Player player) {
        var beltOpt = CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BuildDriver);

        if (beltOpt.isEmpty()) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c请先装备Build Driver腰带！"),
                        true
                );
            }
            return false;
        }

        ItemStack beltStack = beltOpt.get().stack();
        BuildDriver belt = (BuildDriver) beltStack.getItem();
        BuildDriver.BeltMode mode = belt.getMode(beltStack);

        if (mode != BuildDriver.BeltMode.HAZARD_GD) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c需要伟大龙危险模式才能变身 Blood！"),
                        true
                );
            }
            return false;
        }

        if (!belt.getHasGreatDragon(beltStack)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c腰带中没有伟大龙！"),
                        true
                );
            }
            return false;
        }

        return true;
    }

    // ==================== 变身执行 ====================

    private static void forceCompleteHenshin(ServerPlayer player) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);

        FormConfig formConfig = com.jpigeon.ridebattlelib.common.registry.RiderRegistry.getForm(player, BloodConfig.BLOOD_BASE_ID);
        if (formConfig == null) {
            KamenRiderBossYOUandME.LOGGER.warn("找不到Blood形态配置");
            return;
        }

        // 保存原始装备
        Map<EquipmentSlot, ItemStack> originalGear = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                originalGear.put(slot, stack.copy());
            }
        }

        HenshinSessionData sessionData = new HenshinSessionData(
                RiderIds.BLOOD_ID,
                BloodConfig.BLOOD_BASE_ID,
                originalGear,
                new HashMap<>()
        );

        data.startHenshinSession(sessionData);

        // 装备盔甲
        applyBloodArmor(player, formConfig);
        applyBloodEffects(player, formConfig);

        // 同步状态
        RideBattleAPI.syncHenshinState(player);
        RideBattleAPI.syncClientState(player);

        // 发布 Post 事件
        RideBattleAPI.scheduleTicks(2, () -> {
            if (player.isAlive()) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                        new HenshinEvent.Post(player, RiderIds.BLOOD_ID, BloodConfig.BLOOD_BASE_ID)
                );
                KamenRiderBossYOUandME.LOGGER.info("✅ HenshinEvent.Post 发布成功");
            }
        });

        KamenRiderBossYOUandME.LOGGER.info("Blood数据已设置: 形态={}", BloodConfig.BLOOD_BASE_ID);
    }

    // ==================== 盔甲和效果 ====================

    private static void applyBloodArmor(ServerPlayer player, FormConfig formConfig) {
        ItemStack helmet = formConfig.getHelmet() != null ? new ItemStack(formConfig.getHelmet()) : ItemStack.EMPTY;
        ItemStack chestplate = formConfig.getChestplate() != null ? new ItemStack(formConfig.getChestplate()) : ItemStack.EMPTY;
        ItemStack leggings = formConfig.getLeggings() != null ? new ItemStack(formConfig.getLeggings()) : ItemStack.EMPTY;
        ItemStack boots = formConfig.getBoots() != null ? new ItemStack(formConfig.getBoots()) : ItemStack.EMPTY;

        if (!helmet.isEmpty()) player.setItemSlot(EquipmentSlot.HEAD, helmet);
        if (!chestplate.isEmpty()) player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        if (!leggings.isEmpty()) player.setItemSlot(EquipmentSlot.LEGS, leggings);
        if (!boots.isEmpty()) player.setItemSlot(EquipmentSlot.FEET, boots);
    }

    private static void applyBloodEffects(ServerPlayer player, FormConfig formConfig) {
        for (MobEffectInstance effect : formConfig.getEffects()) {
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    // ==================== 解除变身 ====================

    public static void performUnhenshin(Player player) {
        if (player == null || player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        UUID playerId = player.getUUID();

        if (unhenshinInProgress.getOrDefault(playerId, false)) {
            return;
        }

        try {
            unhenshinInProgress.put(playerId, true);

            playSound(player, ModBossSounds.LOCKOFF.get());

            CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BuildDriver).ifPresent(slotResult -> {
                ItemStack beltStack = slotResult.stack();
                BuildDriver belt = (BuildDriver) beltStack.getItem();

                // 返回伟大龙
                if (belt.getHasGreatDragon(beltStack)) {
                    ItemStack greatDragon = new ItemStack(
                            com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems.GREAT_DRAGON.get(),
                            1
                    );
                    GreatDragon greatDragonItem = (GreatDragon) greatDragon.getItem();
                    greatDragonItem.setMode(greatDragon, GreatDragon.Mode.NORMAL);

                    if (!player.getInventory().add(greatDragon)) {
                        player.drop(greatDragon, false);
                    }
                    KamenRiderBossYOUandME.LOGGER.info("返回伟大龙给玩家: {}", player.getName().getString());
                }

                // 返回危险扳机
                if (belt.hasUsedHazardTrigger(beltStack)) {
                    ItemStack hazardTrigger = new ItemStack(
                            com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems.HAZARD_TRIGGER.get(),
                            1
                    );
                    if (!player.getInventory().add(hazardTrigger)) {
                        player.drop(hazardTrigger, false);
                    }
                    KamenRiderBossYOUandME.LOGGER.info("返回危险扳机给玩家: {}", player.getName().getString());
                }

                // 重置腰带
                belt.resetBelt(beltStack);
            });

            // 移除盔甲
            serverPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            serverPlayer.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            serverPlayer.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            serverPlayer.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

            serverPlayer.removeAllEffects();

            RiderData data = serverPlayer.getData(RiderAttachments.RIDER_DATA);
            data.endHenshinSession();

            RideBattleAPI.syncHenshinState(serverPlayer);
            RideBattleAPI.syncClientState(serverPlayer);

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§a✅ 伟大龙和危险扳机已返回！"),
                    true
            );

            KamenRiderBossYOUandME.LOGGER.info("Blood解除变身完成: {}", player.getName().getString());

        } finally {
            unhenshinInProgress.remove(playerId);
        }
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        RideBattleAPI.playPublicSound(player, soundEvent, ((float) Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }

    /**
     * 执行变身 - 由 BuildDriver.stopShaking 调用
     * 直接执行变身，不触发 HenshinEvent.Pre（避免循环）
     */
    public static void executeHenshin(Player player, ItemStack beltStack) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        KamenRiderBossYOUandME.LOGGER.info("执行 Blood 变身!");

        // 设置腰带状态
        if (beltStack != null && !beltStack.isEmpty()) {
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            belt.setShowing(beltStack, false);
            belt.setActive(beltStack, true);
        }

        // 直接完成变身（音效和延迟已经在 stopShaking 中处理了）
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
            try {
                forceCompleteHenshin(serverPlayer);
                KamenRiderBossYOUandME.LOGGER.info("✅ Blood变身完成: {}", player.getName().getString());
            } catch (Exception e) {
                KamenRiderBossYOUandME.LOGGER.error("❌ 完成Blood变身失败", e);
            }
        }
    }
}