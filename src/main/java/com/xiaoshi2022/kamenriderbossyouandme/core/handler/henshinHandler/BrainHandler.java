package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.config.FormConfig;
import com.jpigeon.ridebattlelib.common.data.HenshinSessionData;
import com.jpigeon.ridebattlelib.common.data.RiderAttachments;
import com.jpigeon.ridebattlelib.common.data.RiderData;
import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.driver.BrainConfig;
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
public class BrainHandler {

    private static final Map<UUID, Long> lastHenshinTime = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100;

    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            event.setCanceled(true);

            UUID playerId = player.getUUID();
            long currentTick = player.level().getGameTime();
            Long lastTime = lastHenshinTime.get(playerId);

            if (lastTime != null && currentTick - lastTime < COOLDOWN_TICKS) {
                KamenRiderBossYOUandME.LOGGER.info("Brain变身冷却中: {}", player.getName().getString());
                return;
            }

            lastHenshinTime.put(playerId, currentTick);

            RideBattleAPI.scheduleTicks(5, () -> {
                triggerBrainHenshin(player);
            });
        }
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            lastHenshinTime.remove(player.getUUID());
            handleBrainUnhenshinLogic(player);
        }
    }

    public static void triggerBrainHenshin(Player player) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        boolean hasBrainDriver = CurioUtils.findFirstCurio(player,
                stack -> stack.getItem() instanceof BrainDriver).isPresent();
        if (!hasBrainDriver) {
            KamenRiderBossYOUandME.LOGGER.warn("玩家未装备BrainDriver: {}", player.getName().getString());
            return;
        }

        playSound(player, ModBossSounds.BRAINRIDER.get());

        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();

            belt.setMode(beltStack, BrainDriver.BeltMode.BRAIN);
            belt.setShowing(beltStack, false);
            belt.setActive(beltStack, true);
            belt.setHenshin(beltStack, true);

            belt.triggerHenshinAnimation(player, beltStack);
        });

        RideBattleAPI.scheduleTicks(30, () -> {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
                try {
                    forceCompleteHenshin(serverPlayer);
                    KamenRiderBossYOUandME.LOGGER.info("✅ Brain变身完成: {}", player.getName().getString());
                } catch (Exception e) {
                    KamenRiderBossYOUandME.LOGGER.error("❌ 完成Brain变身失败", e);
                }
            }
        });
    }

    /**
     * 🥊 直接操作数据完成变身 - 使用正确的API方法
     */
    private static void forceCompleteHenshin(ServerPlayer player) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);

        FormConfig formConfig = com.jpigeon.ridebattlelib.common.registry.RiderRegistry.getForm(player, BrainConfig.BRAIN_BASE_ID);
        if (formConfig == null) {
            KamenRiderBossYOUandME.LOGGER.warn("找不到Brain形态配置");
            return;
        }

        Map<EquipmentSlot, ItemStack> originalGear = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                originalGear.put(slot, stack.copy());
            }
        }

        applyBrainArmor(player, formConfig);
        applyBrainEffects(player, formConfig);

        Map<ResourceLocation, ItemStack> driverSnapshot = new HashMap<>();

        HenshinSessionData sessionData = new HenshinSessionData(
                RiderIds.BRAIN_ID,
                BrainConfig.BRAIN_BASE_ID,
                originalGear,
                driverSnapshot
        );

        data.startHenshinSession(sessionData);

        RideBattleAPI.syncHenshinState(player);
        RideBattleAPI.syncClientState(player);

        KamenRiderBossYOUandME.LOGGER.info("Brain数据已设置: 形态={}", BrainConfig.BRAIN_BASE_ID);
    }

    private static void applyBrainArmor(ServerPlayer player, FormConfig formConfig) {
        ItemStack helmet = formConfig.getHelmet() != null ? new ItemStack(formConfig.getHelmet()) : ItemStack.EMPTY;
        ItemStack chestplate = formConfig.getChestplate() != null ? new ItemStack(formConfig.getChestplate()) : ItemStack.EMPTY;
        ItemStack leggings = formConfig.getLeggings() != null ? new ItemStack(formConfig.getLeggings()) : ItemStack.EMPTY;
        ItemStack boots = formConfig.getBoots() != null ? new ItemStack(formConfig.getBoots()) : ItemStack.EMPTY;

        if (!helmet.isEmpty()) {
            player.setItemSlot(EquipmentSlot.HEAD, helmet);
        }
        if (!chestplate.isEmpty()) {
            player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        }
        if (!leggings.isEmpty()) {
            player.setItemSlot(EquipmentSlot.LEGS, leggings);
        }
        if (!boots.isEmpty()) {
            player.setItemSlot(EquipmentSlot.FEET, boots);
        }
    }

    private static void applyBrainEffects(ServerPlayer player, FormConfig formConfig) {
        for (MobEffectInstance effect : formConfig.getEffects()) {
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    private static void handleBrainUnhenshinLogic(Player player) {
        playSound(player, ModBossSounds.LOCKOFF.get());

        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();

            belt.setMode(beltStack, BrainDriver.BeltMode.DEFAULT);
            belt.setActive(beltStack, false);
            belt.setHenshin(beltStack, false);
            belt.setShowing(beltStack, false);
            belt.setRelease(beltStack, true);

            belt.triggerCancelAnimation(player, beltStack);
        });

        RideBattleAPI.scheduleTicks(20, () -> {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
                RiderData data = serverPlayer.getData(RiderAttachments.RIDER_DATA);

                data.endHenshinSession();

                serverPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                serverPlayer.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                serverPlayer.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                serverPlayer.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

                serverPlayer.removeAllEffects();

                RideBattleAPI.syncHenshinState(serverPlayer);
                KamenRiderBossYOUandME.LOGGER.info("Brain解除变身完成: {}", player.getName().getString());
            }
        });
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        RideBattleAPI.playPublicSound(player, soundEvent, ((float) Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }
}