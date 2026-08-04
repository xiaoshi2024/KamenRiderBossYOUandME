package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.config.FormConfig;
import com.jpigeon.ridebattlelib.common.data.HenshinSessionData;
import com.jpigeon.ridebattlelib.common.data.RiderAttachments;
import com.jpigeon.ridebattlelib.common.data.RiderData;
import com.jpigeon.ridebattlelib.server.event.HenshinEvent;
import com.jpigeon.ridebattlelib.server.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.command.FusionCommand;
import com.xiaoshi2022.kamenriderbossyouandme.event.UnhenshinDelayHandler;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionTagManager;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.build.BloodConfig;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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

    // ==================== 飞行系统 ====================

    // 飞行消耗配置
    private static final int FLIGHT_HUNGER_COST_PER_TICK = 1;
    private static final int FLIGHT_SATURATION_COST_PER_TICK = 1;
    private static final int MIN_HUNGER_TO_FLY = 6;
    private static final float FLIGHT_SPEED_MULTIPLIER = 0.15f;

    // 飞行状态追踪
    private static final Map<UUID, Boolean> IS_FLYING = new HashMap<>();
    private static final Map<UUID, Integer> FLIGHT_TICKS = new HashMap<>();

    // 飞行冷却
    private static final int FLIGHT_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Long> FLIGHT_COOLDOWN_END = new HashMap<>();

    // ✅ 标记玩家是否因为饥饿被强制着陆
    private static final Map<UUID, Boolean> FORCED_LANDING = new HashMap<>();

    // ✅ 记录玩家在非Blood状态下的飞行能力状态
    private static final Map<UUID, Boolean> WAS_CREATIVE_FLYING = new HashMap<>();

    // ==================== 事件监听 ====================

    @SubscribeEvent
    public static void onHenshinPre(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ResourceLocation riderId = event.getRiderId();
        if (!riderId.equals(RiderIds.BLOOD_ID)) return;

        event.setCanceled(true);
        if (RideBattleAPI.isTransformed(player)) return;
    }

    @SubscribeEvent
    public static void onHenshinPost(HenshinEvent.Post event) {
        Player player = event.getPlayer();
        ResourceLocation riderId = event.getRiderId();

        if (!riderId.equals(RiderIds.BLOOD_ID)) return;

        enableFlight(player);
    }

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

    // ==================== Stellaris 氧气伤害完全免疫 ====================

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!RideBattleAPI.isTransformed(player)) return;
        if (!isBloodRider(player)) return;

        if (isStellarisOxygenDamage(event.getSource())) {
            event.setCanceled(true);
        }
    }

    private static boolean isStellarisOxygenDamage(DamageSource source) {
        if (!net.neoforged.fml.ModList.get().isLoaded("stellaris")) {
            return false;
        }

        var key = source.typeHolder().unwrapKey();
        if (key.isPresent()) {
            String location = key.get().location().toString();
            return location.equals("stellaris:oxygen");
        }

        return false;
    }

    // ==================== 飞行核心逻辑 ====================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!RideBattleAPI.isTransformed(player)) return;
        if (!isBloodRider(player)) return;

        UUID playerId = player.getUUID();

        // ✅ 如果是创造模式，保持飞行但不消耗饥饿
        if (player.isCreative()) {
            // 确保飞行能力开启
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getAbilities().setFlyingSpeed(FLIGHT_SPEED_MULTIPLIER);
                player.onUpdateAbilities();
            }
            // 清除强制着陆状态
            FORCED_LANDING.remove(playerId);
            FLIGHT_COOLDOWN_END.remove(playerId);
            IS_FLYING.put(playerId, true);
            return;
        }

        // 检查是否处于强制着陆状态
        boolean isForcedLanding = FORCED_LANDING.getOrDefault(playerId, false);
        if (isForcedLanding) {
            // 强制着陆期间，完全禁用飞行能力
            if (player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            // 检查冷却是否结束
            long currentTime = player.level().getGameTime();
            Long cooldownEnd = FLIGHT_COOLDOWN_END.get(playerId);
            if (cooldownEnd != null && currentTime >= cooldownEnd) {
                // 冷却结束，检查饥饿值是否恢复
                if (player.getFoodData().getFoodLevel() >= MIN_HUNGER_TO_FLY) {
                    FORCED_LANDING.remove(playerId);
                    // 重新启用飞行能力
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.getAbilities().mayfly = true;
                        serverPlayer.getAbilities().setFlyingSpeed(FLIGHT_SPEED_MULTIPLIER);
                        serverPlayer.onUpdateAbilities();
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§a✅ 饱食度已恢复，可以再次飞行！"),
                                true
                        );
                    }
                }
            }
            return;
        }

        // 检查玩家是否在飞行（按住空格）
        boolean isFlying = IS_FLYING.getOrDefault(playerId, false);
        boolean wantsToFly = player.getAbilities().flying || player.isFallFlying();

        if (wantsToFly) {
            // 尝试飞行
            if (tryFly(player)) {
                // 飞行成功
                IS_FLYING.put(playerId, true);
                FLIGHT_TICKS.put(playerId, FLIGHT_TICKS.getOrDefault(playerId, 0) + 1);
                spawnFlightParticles(player);
            } else {
                // 飞行失败 - 强制着陆
                if (isFlying || wantsToFly) {
                    forceLandPlayer(player);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§c⚠ 饱食度不足，无法继续飞行！"),
                            true
                    );
                }
            }
        } else {
            // 玩家不在飞行状态
            if (isFlying) {
                landPlayer(player);
                IS_FLYING.put(playerId, false);
                FLIGHT_TICKS.put(playerId, 0);
            }
        }
    }

    /**
     * 尝试飞行 - 消耗饥饿值
     * @return true 如果飞行成功
     */
    private static boolean tryFly(Player player) {
        UUID playerId = player.getUUID();

        // 检查是否在强制着陆冷却中
        if (FORCED_LANDING.getOrDefault(playerId, false)) {
            return false;
        }

        // ✅ 创造模式不消耗饥饿
        if (player.isCreative()) {
            return true;
        }

        int foodLevel = player.getFoodData().getFoodLevel();
        float saturation = player.getFoodData().getSaturationLevel();

        // 检查最低饥饿值
        if (foodLevel < MIN_HUNGER_TO_FLY) {
            return false;
        }

        // 消耗饥饿值（每5tick消耗一次）
        Integer flightTicks = FLIGHT_TICKS.get(playerId);
        if (flightTicks == null || flightTicks % 5 == 0) {
            float saturationCost = FLIGHT_SATURATION_COST_PER_TICK;
            int hungerCost = FLIGHT_HUNGER_COST_PER_TICK;

            if (saturation >= saturationCost) {
                player.getFoodData().setSaturation(saturation - saturationCost);
            } else {
                float remainingCost = saturationCost - saturation;
                player.getFoodData().setSaturation(0);
                int hungerToReduce = (int) Math.ceil(remainingCost);
                if (hungerToReduce > 0) {
                    player.getFoodData().setFoodLevel(Math.max(0, foodLevel - hungerToReduce));
                }
            }

            // 再次检查饥饿值是否还够
            if (player.getFoodData().getFoodLevel() < MIN_HUNGER_TO_FLY) {
                return false;
            }
        }

        // 确保飞行能力开启
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
        }
        if (!player.getAbilities().flying) {
            player.getAbilities().flying = true;
        }
        player.getAbilities().setFlyingSpeed(FLIGHT_SPEED_MULTIPLIER);
        player.onUpdateAbilities();

        return true;
    }

    /**
     * 强制着陆 - 禁用飞行能力并进入冷却
     */
    private static void forceLandPlayer(Player player) {
        UUID playerId = player.getUUID();

        // 标记强制着陆
        FORCED_LANDING.put(playerId, true);

        // 设置冷却
        long currentTime = player.level().getGameTime();
        FLIGHT_COOLDOWN_END.put(playerId, currentTime + FLIGHT_COOLDOWN_TICKS);

        // ✅ 完全禁用飞行能力（非创造模式）
        if (player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
                serverPlayer.getAbilities().setFlyingSpeed(0.05f);
                serverPlayer.onUpdateAbilities();
            } else {
                // ✅ 创造模式保持飞行，但标记强制着陆状态
                serverPlayer.getAbilities().mayfly = true;
                serverPlayer.getAbilities().flying = true;
                serverPlayer.onUpdateAbilities();
            }
        }

        IS_FLYING.put(playerId, false);
        FLIGHT_TICKS.put(playerId, 0);
    }

    /**
     * 正常着陆 - 禁用飞行但不进入冷却
     */
    private static void landPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 只在非强制着陆时禁用飞行
            if (!FORCED_LANDING.getOrDefault(player.getUUID(), false)) {
                if (!serverPlayer.isCreative()) {
                    serverPlayer.getAbilities().flying = false;
                    serverPlayer.getAbilities().setFlyingSpeed(0.05f);
                    serverPlayer.onUpdateAbilities();
                }
            }
        }
    }

    /**
     * 启用飞行能力（变身时调用）
     */
    private static void enableFlight(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            UUID playerId = player.getUUID();

            // ✅ 保存创造模式飞行状态
            WAS_CREATIVE_FLYING.put(playerId, serverPlayer.isCreative());

            // 清除强制着陆状态
            FORCED_LANDING.remove(playerId);
            FLIGHT_COOLDOWN_END.remove(playerId);

            // ✅ 创造模式直接启用，不消耗饥饿
            if (serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = true;
                serverPlayer.getAbilities().flying = true;
                serverPlayer.getAbilities().setFlyingSpeed(FLIGHT_SPEED_MULTIPLIER);
                serverPlayer.onUpdateAbilities();
                IS_FLYING.put(playerId, true);
                return;
            }

            serverPlayer.getAbilities().mayfly = true;
            serverPlayer.getAbilities().setFlyingSpeed(FLIGHT_SPEED_MULTIPLIER);
            serverPlayer.onUpdateAbilities();

            IS_FLYING.put(playerId, false);
            FLIGHT_TICKS.put(playerId, 0);
        }
    }

    /**
     * 禁用飞行能力（解除变身后调用）
     */
    private static void disableFlight(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            UUID playerId = player.getUUID();

            // ✅ 如果玩家之前是创造模式，恢复创造飞行
            Boolean wasCreative = WAS_CREATIVE_FLYING.remove(playerId);
            if (wasCreative != null && wasCreative) {
                // 创造模式保持飞行能力
                serverPlayer.getAbilities().mayfly = true;
                serverPlayer.getAbilities().flying = true;
                serverPlayer.getAbilities().setFlyingSpeed(0.05f);
                serverPlayer.onUpdateAbilities();
                IS_FLYING.put(playerId, true);
                return;
            }

            if (!serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
                serverPlayer.getAbilities().setFlyingSpeed(0.05f);
                serverPlayer.onUpdateAbilities();
            }

            FORCED_LANDING.remove(playerId);
            FLIGHT_COOLDOWN_END.remove(playerId);
            IS_FLYING.remove(playerId);
            FLIGHT_TICKS.remove(playerId);
        }
    }

    /**
     * 检查是否是Blood骑士
     */
    private static boolean isBloodRider(Player player) {
        return RideBattleAPI.isSpecificForm(player, BloodConfig.BLOOD_BASE_ID);
    }

    /**
     * 飞行粒子效果
     */
    private static void spawnFlightParticles(Player player) {
        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        Integer tick = FLIGHT_TICKS.get(player.getUUID());
        if (tick == null || tick % 5 != 0) return;

        net.minecraft.core.particles.DustParticleOptions particle =
                new net.minecraft.core.particles.DustParticleOptions(
                        new org.joml.Vector3f(0.6f, 0.0f, 0.0f), 0.8f
                );

        for (int i = 0; i < 3; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;
            double offsetY = -0.3 - player.getRandom().nextDouble() * 0.3;

            serverLevel.sendParticles(particle,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, -0.1, 0, 0
            );
        }
    }

    // ==================== 变身和解除变身 ====================

    public static void onShakingComplete(Player player, ItemStack beltStack) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        BuildDriver belt = (BuildDriver) beltStack.getItem();
        belt.setShowing(beltStack, false);
        belt.setActive(beltStack, true);

        RideBattleAPI.scheduleTicks(10, () -> {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
                try {
                    forceCompleteHenshin(serverPlayer);
                } catch (Exception e) {
                    KamenRiderBossYOUandME.LOGGER.error("❌ 完成Blood变身失败", e);
                }
            }
        });
    }

    private static void forceCompleteHenshin(ServerPlayer player) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);

        FormConfig formConfig = com.jpigeon.ridebattlelib.common.registry.RiderRegistry.getForm(player, BloodConfig.BLOOD_BASE_ID);
        if (formConfig == null) {
            KamenRiderBossYOUandME.LOGGER.warn("找不到Blood形态配置");
            return;
        }

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

        applyBloodArmor(player, formConfig);
        applyBloodEffects(player, formConfig);

        enableFlight(player);

        RideBattleAPI.syncHenshinState(player);
        RideBattleAPI.syncClientState(player);

        RideBattleAPI.scheduleTicks(2, () -> {
            if (player.isAlive()) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                        new HenshinEvent.Post(player, RiderIds.BLOOD_ID, BloodConfig.BLOOD_BASE_ID)
                );
            }
        });
    }

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

            disableFlight(player);

            CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BuildDriver).ifPresent(slotResult -> {
                ItemStack beltStack = slotResult.stack();
                BuildDriver belt = (BuildDriver) beltStack.getItem();

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
                }

                if (belt.hasUsedHazardTrigger(beltStack)) {
                    ItemStack hazardTrigger = new ItemStack(
                            com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems.HAZARD_TRIGGER.get(),
                            1
                    );
                    if (!player.getInventory().add(hazardTrigger)) {
                        player.drop(hazardTrigger, false);
                    }
                }

                belt.resetBelt(beltStack);
            });

            serverPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            serverPlayer.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            serverPlayer.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            serverPlayer.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

            serverPlayer.removeAllEffects();

            RiderData data = serverPlayer.getData(RiderAttachments.RIDER_DATA);
            data.endHenshinSession();

            RideBattleAPI.syncHenshinState(serverPlayer);
            RideBattleAPI.syncClientState(serverPlayer);

        } finally {
            unhenshinInProgress.remove(playerId);
        }
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        RideBattleAPI.playPublicSound(player, soundEvent, ((float) Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }

    public static void executeHenshin(Player player, ItemStack beltStack) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        if (FusionCommand.FUSION_REQUIRED && player instanceof ServerPlayer serverPlayer) {
            java.util.List<Player> targets = FusionTagManager.getNearbyFusionTargets(serverPlayer, 10.0);
            if (targets.size() < 3) {
                serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§c⚠ 需要至少3个融合者！当前: " + targets.size() + "/3")
                );
                return;
            }
        }

        if (beltStack != null && !beltStack.isEmpty()) {
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            belt.setShowing(beltStack, false);
            belt.setActive(beltStack, true);
        }

        if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
            try {
                forceCompleteHenshin(serverPlayer);
            } catch (Exception e) {
                KamenRiderBossYOUandME.LOGGER.error("❌ 完成Blood变身失败", e);
            }
        }
    }
}