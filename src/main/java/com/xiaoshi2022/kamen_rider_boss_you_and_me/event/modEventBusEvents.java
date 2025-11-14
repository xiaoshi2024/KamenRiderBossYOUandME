package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.PlayerJoinSyncPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.RiderInvisibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.*;

public class modEventBusEvents {
    @Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
    public class ClientEvents {
        //        @SubscribeEvent
//        public static void onKeyInput(InputEvent.Key event) {
//            if (Minecraft.getInstance().screen == null) {
//                if (CHANGE_KEY.isDown()) {
//                    // 获取当前玩家
//                    LocalPlayer player = Minecraft.getInstance().player;
//                    if (player != null) {
//                        // 发送网络消息到服务器
//                        PacketHandler.sendToServer(new PlayerAnimationPacket(player.getId(), STORIOUS_VFX_HEIXIN));
//                    }
//                }
//            }
//        }
//    @SubscribeEvent
//    public static void onKeyInput(InputEvent.Key event) {
//        if (CHANGE_KEY.consumeClick()) {
//            // 按键被按下，发送网络包到服务器
//            if (Minecraft.getInstance().level != null) {
//                PacketHandler.INSTANCE.sendToServer(new SoundStopPacket(
//                        Minecraft.getInstance().player.getId(), "kamen_rider_boss_you_and_me:banana_lockonby"));
//                }
//            }
//        }
        // 在事件处理器类中添加
        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
                    curios.findCurio("belt", 0).ifPresent(slotResult -> {
                        ItemStack stack = slotResult.stack();

                        // 战极驱动器处理（保持原有逻辑）
                        if (stack.getItem() instanceof sengokudrivers_epmty belt) {
                            sengokudrivers_epmty.BeltMode mode = belt.getMode(stack);   // ← 读取 NBT
                            PacketHandler.sendToClient(
                                    new BeltAnimationPacket(player.getId(), "login_sync", mode),
                                    player
                            );
                        }

                        // Genesis驱动器处理（新增柠檬状态同步）
                        if (stack.getItem() instanceof Genesis_driver belt) {
                            Genesis_driver.BeltMode mode = belt.getMode(stack);
                            boolean hen = belt.getHenshin(stack);
                            boolean equipped = belt.getEquipped(stack);

                            String anim;
                            if (hen) {
                                anim = "move";
                            } else if (equipped && mode == Genesis_driver.BeltMode.LEMON) {
                                anim = "lemon_idle";
                            } else {
                                anim = "idles";
                            }

                            System.out.printf("登录同步Genesis驱动器: 模式=%s 动画=%s%n", mode, anim);

                            PacketHandler.sendToClient(
                                    new BeltAnimationPacket(player.getId(), anim, mode),
                                    player);

                            // 不需要 setTag，只是读取
                        }
                    });
                });
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            RiderInvisibilityManager.updateInvisibility(player);
        }

        /* 装备栏变化时立即刷新（防止延迟） */
        @SubscribeEvent
        public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) return;

            RiderInvisibilityManager.updateInvisibility(player);
        }

        @SubscribeEvent
        public static void onPlayerChat(ServerChatEvent event) {
            String msg = event.getMessage().getString(); // 获取玩家发送的消息
            Player player = event.getPlayer(); // 获取发送消息的玩家
            Level level = player.level(); // 获取玩家所在的世界

            // 安全检查：确保世界和玩家有效
            if (level == null || !player.isAlive()) {
                return;
            }

            /* ---------- 彩蛋：小丑蝙蝠 ---------- */
            if (KivatBatTwoNd.isClownBatCall(msg)) {
                event.setCanceled(true);

                // 使用正确的世界引用
                List<KivatBatTwoNd> clowns = level.getEntitiesOfClass(
                        KivatBatTwoNd.class,
                        player.getBoundingBox().inflate(16), //范围
                        k -> k != null && k.isAlive() && !k.isTame() // 添加安全检查
                );

                if (clowns.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("dialog.kivat.noclowntarget"));
                    return;
                }

                // 安全地召唤每个小丑蝙蝠
                clowns.forEach(k -> {
                    if (k != null && k.isAlive()) {
                        k.temptToPlayer(player);
                    }
                });

                player.sendSystemMessage(Component.translatable("dialog.kivat.clownbat"));
                return;
            }

            /* ---------- 正常 @kivat 对话 ---------- */
            if (!msg.startsWith("@kivat")) return;
            event.setCanceled(true);

            KivatBatTwoNd kivat = level.getEntitiesOfClass(
                    KivatBatTwoNd.class,
                    player.getBoundingBox().inflate(20),
                    k -> k != null && k.isAlive() && k.isTame() && k.isOwnedBy(player) // 添加安全检查
            ).stream().findFirst().orElse(null);

            if (kivat == null) {
                player.sendSystemMessage(Component.translatable("dialog.kivat.notfound"));
                return;
            }

            String query = msg.substring("@kivat".length()).trim();
            if (level instanceof ServerLevel) {
                if (query.equals("变身") || query.equals("好了!灭绝时刻到了") || query.equals("henshin")) {

                    // 1. 先检查盔甲
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                            ItemStack armor = player.getItemBySlot(slot);
                            if (!armor.isEmpty() && armor.getItem() instanceof KamenBossArmor) {
                                player.sendSystemMessage(Component.translatable("dialog.kivat.already_transformed"));
                                return;
                            }
                        }
                    }

                    // 2. 再检查距离
                    if (kivat.distanceTo(player) <= 2.0) {
                        kivat.transform(player);
                    } else {
                        player.sendSystemMessage(Component.translatable("dialog.kivat.too_far"));
                        // 如果你想让它飞过来再变身，把下面两行取消注释
                         kivat.temptToPlayer(player);
                         kivat.pendingTransformPlayer = player.getUUID();
                    }
                } else {
                    KivatBatTwoNd.reply(query, kivat, (ServerLevel) level);
                }
            }
        }

        @SubscribeEvent
        public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer newPlayer) {
                // ⭐ 给新玩家发送所有已在线玩家的状态
                for (ServerPlayer onlinePlayer : newPlayer.server.getPlayerList().getPlayers()) {
                    if (onlinePlayer != newPlayer) {
                        syncPlayerState(onlinePlayer, newPlayer); // 发送给新玩家
                    }
                }

                // ⭐ 同步自己的状态给所有玩家
                syncPlayerState(newPlayer, null);
            }
        }

        // 在 modEventBusEvents.onPlayerLoggedIn 中：
        private void syncPlayerState(ServerPlayer targetPlayer, ServerPlayer excludePlayer) {
            CuriosApi.getCuriosInventory(targetPlayer).ifPresent(inv ->
                    inv.findCurio("belt", 0).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        if (stack.getItem() instanceof DrakKivaBelt belt && belt.getHenshin(stack)) {
                            PlayerJoinSyncPacket packet = new PlayerJoinSyncPacket(
                                    targetPlayer.getId(), "henshin", "drakkiva", "DEFAULT"
                            );
                            // 广播给所有追踪者（除了 excludePlayer）
                            PacketHandler.sendToAllTrackingExcept(packet, targetPlayer, excludePlayer);
                        }
                    })
            );
        }
    }


    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.DEDICATED_SERVER)
    public static class AttackEventHandler {

        private static final Map<UUID, Long> LAST_ATTACK_TIME = new HashMap<>();
        private static final long ATTACK_COOLDOWN = 500; // 500ms冷却防止重复计算

        /**
         * 在攻击开始时应用自定义伤害
         */
        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            if (!(event.getSource().getDirectEntity() instanceof ElementaryInvesHelheim inves)) {
                return;
            }

            // 防止重复处理
            UUID attackerId = inves.getUUID();
            long currentTime = System.currentTimeMillis();
            if (LAST_ATTACK_TIME.containsKey(attackerId)) {
                if (currentTime - LAST_ATTACK_TIME.get(attackerId) < ATTACK_COOLDOWN) {
                    return;
                }
            }
            LAST_ATTACK_TIME.put(attackerId, currentTime);

            LivingEntity target = event.getEntity();

            // 获取自定义攻击伤害
            double customDamage = inves.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());

            if (customDamage > 0) {
                // 取消原版伤害计算，使用我们的自定义伤害
                event.setCanceled(true);

                // 直接应用穿透后的伤害
                applyPenetratedDamage(inves, target, (float) customDamage);
            }

            // 原有的协同攻击逻辑
            handleLordBaronSynergy(event);
        }

        /**
         * 在伤害计算阶段确保伤害不被减免
         */
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource().getDirectEntity() instanceof ElementaryInvesHelheim inves)) {
                return;
            }

            // 确保我们的自定义伤害不被减免
            if (event.getAmount() > 0) {
                // 二次伤害穿透，确保伤害生效
                float penetratedDamage = applySecondaryPenetration(inves, event.getEntity(), event.getAmount());
                event.setAmount(penetratedDamage);
            }
        }

        /**
         * 应用主要穿透伤害
         */
        private static void applyPenetratedDamage(ElementaryInvesHelheim attacker, LivingEntity target, float baseDamage) {
            // 计算穿透后的伤害
            float finalDamage = calculateFinalDamage(attacker, target, baseDamage);

            // 直接对目标造成伤害，绕过原版伤害系统
            target.hurt(attacker.damageSources().mobAttack(attacker), finalDamage);

            // 攻击效果
            spawnAttackParticles(attacker, target);

            // 调试信息
            if (target instanceof ServerPlayer player) {
                player.displayClientMessage(
                        Component.literal("§c受到穿透攻击: " + String.format("%.1f", finalDamage) + " 伤害"),
                        true
                );
                kamen_rider_boss_you_and_me.LOGGER.info(
                        "穿透攻击: {} -> {}, 伤害: {}",
                        attacker.getDisplayName().getString(),
                        player.getScoreboardName(),
                        finalDamage
                );
            }
        }

        /**
         * 计算最终伤害（完全穿透）
         */
        private static float calculateFinalDamage(ElementaryInvesHelheim attacker, LivingEntity target, float baseDamage) {
            float damage = baseDamage;

            // 1. 完全忽略护甲（强制穿透）
            damage = ignoreArmor(target, damage);

            // 2. 完全忽略抗性效果
            damage = ignoreResistance(target, damage);

            // 3. 对变身玩家额外伤害
            if (isTransformedPlayer(target)) {
                damage *= 1.3f; // 增加30%对变身玩家的伤害
            }

            // 4. 主人协同加成
            if (attacker.getMaster() instanceof LordBaronEntity) {
                damage *= 1.2f; // 增加20%协同伤害
            }

            // 确保最小伤害
            return Math.max(damage, 5.0f);
        }

        /**
         * 考虑护甲值的伤害计算
         */
        private static float ignoreArmor(LivingEntity target, float damage) {
            // 不再完全忽略护甲，而是根据装备计算适当的伤害穿透
            // 获取目标的护甲值
            double armorValue = target.getAttributeValue(Attributes.ARMOR);
            
            // 对于rider_barons装备，我们确保护甲能够正常发挥作用
            // 但仍然有一定的穿透效果
            float penetrationFactor = 0.7f; // 保留70%的伤害穿透效果
            
            // 计算最终伤害：保留部分穿透效果，但不再完全忽略护甲
            // 基础穿透伤害 + 考虑护甲后的剩余伤害
            return (float)(damage * penetrationFactor + damage * (1 - penetrationFactor) * Math.max(0, 1 - armorValue / (25 + armorValue)));
        }

        /**
         * 完全忽略抗性效果
         */
        private static float ignoreResistance(LivingEntity target, float damage) {
            // 完全忽略抗性药水效果
            return damage;
        }

        /**
         * 检查是否是变身玩家
         */
        private static boolean isTransformedPlayer(LivingEntity entity) {
            if (!(entity instanceof Player player)) return false;

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack armor = player.getItemBySlot(slot);
                    if (!armor.isEmpty() && armor.getItem() instanceof KamenBossArmor) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 二次穿透确保伤害不被减免
         */
        private static float applySecondaryPenetration(ElementaryInvesHelheim attacker, LivingEntity target, float currentDamage) {
            // 如果伤害被减免太多，强制恢复
            if (currentDamage < 5.0f) {
                float baseDamage = (float) attacker.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
                return Math.max(baseDamage * 0.5f, 8.0f); // 至少造成50%基础伤害或8点伤害
            }
            return currentDamage;
        }

        /**
         * 生成攻击粒子效果
         */
        private static void spawnAttackParticles(ElementaryInvesHelheim attacker, LivingEntity target) {
            if (attacker.level() instanceof ServerLevel serverLevel) {
                // 红色攻击粒子
                serverLevel.sendParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1
                );
            }
        }

        /**
         * 原有的协同攻击逻辑
         */
        private static void handleLordBaronSynergy(LivingAttackEvent event) {
            // 保持你原有的协同攻击逻辑...
            if (event.getSource().getDirectEntity() instanceof ElementaryInvesHelheim inves &&
                    inves.getMaster() instanceof LordBaronEntity lordBaron) {
                LivingEntity target = event.getEntity();

                if (lordBaron.isAlliedTo(target)) {
                    event.setCanceled(true);
                    return;
                }

                // LordBaron攻击时，附近受控的ElementaryInvesHelheim获得短暂的伤害提升
                List<ElementaryInvesHelheim> controlledInves = lordBaron.level().getEntitiesOfClass(
                        ElementaryInvesHelheim.class,
                        lordBaron.getBoundingBox().inflate(15.0),
                        entity -> entity.isAlive() && entity.getMaster() == lordBaron
                );

                controlledInves.forEach(minion -> {
                    minion.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST, 60, 1, false, true // 提升到等级2
                    ));
                });

            }
        }
    }
    
    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public final class CommonListener {

        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            // 统一使用自定义属性系统

            event.put(ModEntityTypes.LORD_BARON.get(), LordBaronEntity.createAttributes()
                    .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 40.0D)  // 攻击力从15.0D提高到40.0D
                    .add(Attributes.MAX_HEALTH, 350.0D)
                    .add(Attributes.ARMOR, 15.0D)
                    .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                    .add(Attributes.MOVEMENT_SPEED, 0.35D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                    .build()
            ); // 增强版：提高防御、韧性和生命值，添加击退抗性

            event.put(ModEntityTypes.GIIFUDEMOS_ENTITY.get(),
                    GiifuDemosEntity.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 30.0D)  // 攻击力从12.0D提高到30.0D
                            .add(Attributes.MAX_HEALTH, 300.0D)
                            .add(Attributes.ARMOR, 12.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                            .add(Attributes.MOVEMENT_SPEED, 0.35D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .build()
            ); // 增强版：提高防御、韧性和生命值，添加击退抗性

            event.put(ModEntityTypes.ELITE_MONSTER_NPC.get(),
                    EliteMonsterNpc.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 35.0D)  // 攻击力从30.0D提高到35.0D，比肩异类电王
                            .add(Attributes.MAX_HEALTH, 300.0D)
                            .add(Attributes.ARMOR, 12.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                            .add(Attributes.MOVEMENT_SPEED, 0.35D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .build()
            ); // 增强版：提高防御、韧性和生命值，添加击退抗性

            event.put(ModEntityTypes.STORIOUS.get(),
                    StoriousEntity.createAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 35.0D)  // 攻击力从14.0D提高到35.0D
                            .add(Attributes.MAX_HEALTH, 500.0D)
                            .add(Attributes.ARMOR, 20.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                            .add(Attributes.MOVEMENT_SPEED, 0.33D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .build()
            ); // 强化版：大幅提高防御、韧性和生命值，增强击退抗性

            event.put(ModEntityTypes.GIFFTARIAN.get(),
                    Gifftarian.createMonsterAttributes()
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 32.0D)  // 攻击力从32.0D
                            .add(Attributes.MAX_HEALTH, 350.0D)
                            .add(Attributes.ARMOR, 15.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                            .add(Attributes.MOVEMENT_SPEED, 0.34D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .build()
            ); // 强化版：大幅提高防御、韧性和生命值，增强击退抗性

            event.put(ModEntityTypes.INVES_HEILEHIM.get(),ElementaryInvesHelheim.createAttributes());

            event.put(ModEntityTypes.ANOTHER_ZI_O.get(),
                    Monster.createMonsterAttributes()
                            .add(Attributes.MAX_HEALTH, 200.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.35D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 35.0D)  // 攻击力从8.0D提高到35.0D
                            .add(Attributes.ARMOR, 15.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性，增强击退抗性


            event.put(ModEntityTypes.ANOTHER_DEN_O.get(),
                    Monster.createMonsterAttributes()
                            .add(Attributes.MAX_HEALTH, 200.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.35D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 35.0D)  // 攻击力从8.0D提高到35.0D
                            .add(Attributes.ARMOR, 15.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性，增强击退抗性
            event.put(ModEntityTypes.ANOTHER_DECADE.get(),
                    Monster.createMonsterAttributes()
                            .add(Attributes.MAX_HEALTH, 800.0D)      // 大幅提高生命值，符合最终Boss身份
                            .add(Attributes.MOVEMENT_SPEED, 0.38D)      // 移动速度进一步提高，更具威胁性
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 65.0D)  // 大幅提高攻击力，符合最终Boss实力
                            .add(Attributes.ARMOR, 30.0D)      // 大幅提高护甲，增强生存能力
                            .add(Attributes.ARMOR_TOUGHNESS, 15.0D)      // 大幅提高护甲韧性，减少高额伤害
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)      // 大幅提高击退抗性，难以被击退
                            .build()
            ); // 最终Boss版：基于百度百科资料，异类Decade作为Zi-O最终Boss，拥有召唤黑暗骑士、操控时间等强大能力

            event.put(ModEntityTypes.KIVAT_BAT_II.get(),
                    KivatBatTwoNd.createAttributes()
                            .add(Attributes.MAX_HEALTH, 150.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.30D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .add(Attributes.FLYING_SPEED, 0.8D)      // 飞行速度调整为更合理的值
                            .add(Attributes.ATTACK_DAMAGE, 16.0D)          // 攻击力从8.0D提高到16.0D
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 16.0D)  // 攻击力从8.0D提高到16.0D
                            .add(Attributes.ARMOR, 12.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性

            event.put(ModEntityTypes.GIIFU_HUMAN.get(),
                    GiifuHumanEntity.createAttributes()
                            .add(Attributes.MAX_HEALTH, 800.0D)
                            .add(Attributes.ARMOR, 30.0D)
                            .add(Attributes.ARMOR_TOUGHNESS, 15.0D)
                            .add(Attributes.ATTACK_DAMAGE, 30.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.32D)      // 移动速度大幅提高，匹敌拥有速度buff的玩家
                            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 40.0D)
                            .build()
            ); // 强化版：大幅提高生命值、防御和韧性

            event.put(ModEntityTypes.SAKURA_HURRICANE.get(), EntitySakuraHurricane.createAttributes());

            event.put(ModEntityTypes.TIME_ROYALTY.get(), TimeRoyaltyEntity.createAttributes().build());

            event.put(ModEntityTypes.NECROM_EYEX.get(), NecromEyexEntity.createAttributes());
            event.put(ModEntityTypes.DARKGHOST_RIDER_KICK.get(), DarkGhostRiderKickEntity.createAttributes());
            event.put(ModEntityTypes.GAMMA_S.get(), Gamma_s_Entity.createAttributes());
            event.put(ModEntityTypes.GHOST_EYE_ENTITY.get(), GhostEyeEntity.createAttributes());
            event.put(ModEntityTypes.GAMMA_EYECON_ENTITY.get(), GammaEyeconEntity.createAttributes());

            event.put(ModEntityTypes.BAT_DARKS.get(), BatDarksEntity.createAttributes());

            event.put(ModEntityTypes.BAT_STAMP_FINISH.get(), BatStampFinishEntity.createAttributes());

            event.put(ModEntityTypes.KNECROMGHOST.get(), KnecromghostEntity.createAttributes());

            event.put(ModEntityTypes.DUKE_KNIGHT.get(),DukeKnightEntity.createAttributes());

            event.put(ModEntityTypes.BARON_BANANA_ENERGY.get(), BaronBananaEnergyEntity.createAttributes());

            event.put(ModEntityTypes.BARON_LEMON_ENERGY.get(), BaronLemonEnergyEntity.createAttributes());

            event.put(ModEntityTypes.DARK_KIVA_SEAL_BARRIER.get(), DarkKivaSealBarrierEntity.createAttributes());
            
            // 注册时劫者实体属性
            event.put(ModEntityTypes.TIME_JACKER.get(), TimeJackerEntity.createAttributes().build());

//            event.put(ModEntityTypes.KAITO.get(),
//                    KaitoVillager.createAttributes()
//                            .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 3.0D)
//                            .add(Attributes.ATTACK_DAMAGE, 3.0D) // ⚠️ 添加这行
//                            .add(Attributes.MAX_HEALTH, 40.0D)
//                            .build()
//            );
        }

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(CHANGE_KEY);
            event.register(CHANGES_KEY);
            event.register(RELIEVE_KEY);
            event.register(KEY_GUARD);
            event.register(KEY_BLAST);
            event.register(KEY_BOOST);
            event.register(KEY_FLIGHT);
            event.register(KEY_BARRIER_PULL);
        }
    }
}