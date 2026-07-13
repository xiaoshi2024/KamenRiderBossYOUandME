package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.event.UnhenshinDelayHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.SoundStopPacket;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.gaim.TyrantConfig;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class TyrantHandler {

    // ✅ 冷却管理
    private static final Map<UUID, Long> lastHenshinTime = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100; // 5秒冷却（100 tick = 5秒）

    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();

        if (riderId.equals(RiderIds.TYRANT_ID)) {
            event.setCanceled(true);

            // ✅ 检查是否已变身
            if (RideBattleAPI.isTransformed(player)) {
                return;
            }

            // ✅ 检查冷却
            UUID playerId = player.getUUID();
            long currentTick = player.level().getGameTime();
            Long lastTime = lastHenshinTime.get(playerId);
            if (lastTime != null && currentTick - lastTime < COOLDOWN_TICKS) {
                return;
            }
            lastHenshinTime.put(playerId, currentTick);

            if (!checkTyrantConditions(player)) {
                return;
            }

            RideBattleAPI.scheduleTicks(5, () -> {
                triggerTyrantHenshin(player);
            });
        }
    }

    // 在 TyrantHandler.java 中修改 UnhenshinEvent 监听
    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();
        if (riderId.equals(RiderIds.TYRANT_ID)) {
            // ✅ 不再取消事件和立即执行，让 UnhenshinDelayHandler 处理
            // 但仍然要记录一些状态，如果玩家主动解除变身（不是通过腰带移除）
            if (!UnhenshinDelayHandler.hasPendingUnhenshin(player.getUUID())) {
                // 如果是主动解除，直接执行完整解除
                event.setCanceled(true);
                performUnhenshin(player);
            }
        }
    }

    /**
     * 检查Tyrant变身条件
     * 1. 必须装备 Genesis_driver
     * 2. 腰带模式必须为 DRAGONFRUIT
     * 3. 腰带中必须有 Dragonfruit 锁种（NBT 检查）
     */
    private static boolean checkTyrantConditions(Player player) {
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player,
                stack -> stack.getItem() instanceof Genesis_driver);

        if (beltOpt.isEmpty()) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§c请先装备Genesis Driver腰带！"), true);
            }
            return false;
        }

        ItemStack beltStack = beltOpt.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();
        Genesis_driver.BeltMode mode = belt.getMode(beltStack);

        if (mode != Genesis_driver.BeltMode.DRAGONFRUIT) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§c需要装备龙果锁种才能变身 Tyrant！"), true);
            }
            return false;
        }

        if (!belt.hasLockseed(beltStack)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§c腰带中没有龙果锁种！"), true);
            }
            return false;
        }

        ItemStack lockseed = belt.getLockseed(beltStack);
        if (lockseed == null || lockseed.isEmpty() || !lockseed.is(ModItems.DRAGONFRUIT.get())) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§c腰带中的锁种不是龙果锁种！"), true);
            }
            return false;
        }

        return true;
    }

    public static void triggerTyrantHenshin(Player player) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        // ✅ 停止待机音效 (LEMON_LOCKONBY)
        if (player instanceof ServerPlayer serverPlayer) {
            ResourceLocation standBySound = ResourceLocation.fromNamespaceAndPath(
                    com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID,
                    "lemon_lockonby"
            );
            PacketHandler.sendToClient(
                    serverPlayer,
                    new SoundStopPacket(serverPlayer.getUUID(), standBySound)
            );
            PacketHandler.sendToAllTracking(
                    serverPlayer,
                    new SoundStopPacket(serverPlayer.getUUID(), standBySound)
            );
        }

        // ✅ 只触发腰带动画，不播放锁种音效
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();

            belt.setShowing(beltStack, false);
            belt.setActive(beltStack, true);
            belt.setHenshin(beltStack, true);

            belt.startHenshinAnimation(player, beltStack);
        });

        RideBattleAPI.scheduleTicks(60, () -> {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
                try {
                    forceCompleteHenshin(serverPlayer);

                    // ✅ 播放变身完成音效 (DRAGONFRUIT_ARMS)
                    playSound(player, ModBossSounds.DRAGONFRUIT_ARMS.get());

                    // ✅ 播放腰带动画（变身完成 scatter）
                    CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver).ifPresent(slotResult -> {
                        var beltStack = slotResult.stack();
                        Genesis_driver belt = (Genesis_driver) beltStack.getItem();
                        belt.triggerAnim(player, "controller", "scatter");
                        // ✅ 设置为展示模式
                        belt.setShowing(beltStack, true);
                        belt.setHenshin(beltStack, false);
                    });

                    KamenRiderBossYOUandME.LOGGER.info("✅ Tyrant变身完成: {}", player.getName().getString());
                } catch (Exception e) {
                    KamenRiderBossYOUandME.LOGGER.error("❌ 完成Tyrant变身失败", e);
                }
            }
        });
    }

    private static void forceCompleteHenshin(ServerPlayer player) {
        com.jpigeon.ridebattlelib.common.data.RiderData data = player.getData(com.jpigeon.ridebattlelib.common.data.RiderAttachments.RIDER_DATA);

        com.jpigeon.ridebattlelib.common.config.FormConfig formConfig =
                com.jpigeon.ridebattlelib.common.registry.RiderRegistry.getForm(player, TyrantConfig.TYRANT_BASE_ID);

        if (formConfig == null) {
            KamenRiderBossYOUandME.LOGGER.warn("找不到Tyrant形态配置");
            return;
        }

        Map<EquipmentSlot, ItemStack> originalGear = new java.util.EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                originalGear.put(slot, stack.copy());
            }
        }

        applyTyrantArmor(player, formConfig);
        applyTyrantEffects(player, formConfig);

        Map<ResourceLocation, ItemStack> driverSnapshot = new java.util.HashMap<>();

        com.jpigeon.ridebattlelib.common.data.HenshinSessionData sessionData = new com.jpigeon.ridebattlelib.common.data.HenshinSessionData(
                RiderIds.TYRANT_ID,
                TyrantConfig.TYRANT_BASE_ID,
                originalGear,
                driverSnapshot
        );

        data.startHenshinSession(sessionData);

        RideBattleAPI.syncHenshinState(player);
        RideBattleAPI.syncClientState(player);

        KamenRiderBossYOUandME.LOGGER.info("Tyrant数据已设置: 形态={}", TyrantConfig.TYRANT_BASE_ID);
    }

    private static void applyTyrantArmor(ServerPlayer player, com.jpigeon.ridebattlelib.common.config.FormConfig formConfig) {
        ItemStack helmet = formConfig.getHelmet() != null ? new ItemStack(formConfig.getHelmet()) : ItemStack.EMPTY;
        ItemStack chestplate = formConfig.getChestplate() != null ? new ItemStack(formConfig.getChestplate()) : ItemStack.EMPTY;
        ItemStack leggings = formConfig.getLeggings() != null ? new ItemStack(formConfig.getLeggings()) : ItemStack.EMPTY;
        ItemStack boots = formConfig.getBoots() != null ? new ItemStack(formConfig.getBoots()) : ItemStack.EMPTY;

        if (!helmet.isEmpty()) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
        }
        if (!chestplate.isEmpty()) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chestplate);
        }
        if (!leggings.isEmpty()) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, leggings);
        }
        if (!boots.isEmpty()) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, boots);
        }
    }

    private static void applyTyrantEffects(ServerPlayer player, com.jpigeon.ridebattlelib.common.config.FormConfig formConfig) {
        for (net.minecraft.world.effect.MobEffectInstance effect : formConfig.getEffects()) {
            if (effect != null) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect));
            }
        }
    }

    private static void handleTyrantUnhenshinLogic(Player player) {
        playSound(player, ModBossSounds.LOCKOFF.get());

        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();

            // ✅ 获取锁种并返回给玩家
            ItemStack lockseed = belt.getLockseed(beltStack);
            if (!lockseed.isEmpty()) {
                if (!player.getInventory().add(lockseed)) {
                    player.drop(lockseed, false);
                }
                KamenRiderBossYOUandME.LOGGER.info("返回锁种给玩家: {}", lockseed.getItem().getDescriptionId());
            }

            // ✅ 清除腰带中的锁种
            belt.clearLockseed(beltStack);

            belt.setMode(beltStack, Genesis_driver.BeltMode.DEFAULT);
            belt.setActive(beltStack, false);
            belt.setHenshin(beltStack, false);
            belt.setShowing(beltStack, false);
            belt.setRelease(beltStack, true);

            // ✅ 使用带玩家动画的解除变身方法
            belt.startReleaseWithPlayerAnimation(player, beltStack);
        });

        RideBattleAPI.scheduleTicks(20, () -> {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
                com.jpigeon.ridebattlelib.common.data.RiderData data = serverPlayer.getData(com.jpigeon.ridebattlelib.common.data.RiderAttachments.RIDER_DATA);

                data.endHenshinSession();

                serverPlayer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
                serverPlayer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
                serverPlayer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, ItemStack.EMPTY);
                serverPlayer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, ItemStack.EMPTY);

                serverPlayer.removeAllEffects();

                RideBattleAPI.syncHenshinState(serverPlayer);
                KamenRiderBossYOUandME.LOGGER.info("Tyrant解除变身完成: {}", player.getName().getString());
            }
        });
    }

    public static void playSound(Player player, net.minecraft.sounds.SoundEvent soundEvent) {
        RideBattleAPI.playPublicSound(player, soundEvent, ((float) com.xiaoshi2022.kamenriderbossyouandme.Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }

    // 在 TyrantHandler.java 中添加
    public static void performUnhenshin(Player player) {
        if (player == null || player.level().isClientSide()) return;

        // 执行完整的解除逻辑（从 handleTyrantUnhenshinLogic 复制）
        handleTyrantUnhenshinLogic(player);
    }


    /**
     * 处理 Tyrant 解除变身前的准备工作
     * 1. 返回锁种给玩家
     * 2. 清理腰带状态
     * 3. 播放音效
     */
    /**
     * 处理 Tyrant 解除变身前的准备工作（延迟解除的预处理）
     * 1. 播放音效
     * 2. 触发解除动画
     * 注意：不返回锁种！锁种在真正解除时才返回
     */
    public static void handleTyrantPreUnhenshin(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) return;
        if (!(beltStack.getItem() instanceof Genesis_driver)) return;

        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        // ✅ 只播放解除音效
        playSound(player, ModBossSounds.LOCKOFF.get());

        // ✅ 只触发腰带动画（解除变身动画），不修改腰带状态
        belt.startReleaseWithPlayerAnimation(player, beltStack);
    }
}