package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import net.minecraftforge.client.event.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import java.util.function.Supplier;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class TyrantAbilityHandler {
    // 网络通道常量
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("kamen_rider_boss_you_and_me", "tyrant_ability"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    static {
        // 注册数据包
        CHANNEL.registerMessage(
                packetId++,
                IntangibilitySyncPacket.class,
                IntangibilitySyncPacket::encode,
                IntangibilitySyncPacket::decode,
                IntangibilitySyncPacket::handle
        );
    }

    // 虚化技能相关常量
    private static final int INTANGIBILITY_MAX_DURATION = 1200; // 60秒 = 1200tick
    private static final float INTANGIBILITY_BREAK_THRESHOLD = 60.0f; // 打破虚化的伤害阈值
    private static final Map<UUID, Integer> INTANGIBLE_PLAYERS = new HashMap<>(); // 存储虚化状态的玩家
    private static final Map<UUID, Integer> INTANGIBILITY_TIMERS = new HashMap<>(); // 存储虚化剩余时间

    // 相位传送相关常量
    private static final int TELEPORT_RANGE = 20; // 最大传送距离
    private static final int TELEPORT_ENERGY_COST = 10; // 每次传送消耗的能量

    /* ---------------- 常驻被动 ---------------- */

    // 用于跟踪玩家最后一次自然恢复的时间
    private static final Map<UUID, Long> LAST_RECOVERY_TIME = new HashMap<>();
    private static final long RECOVERY_COOLDOWN = 20; // 1秒冷却时间(20tick)
    private static final float HEAL_AMOUNT = 2.0f; // 每次恢复的生命值

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player) && player.isAlive()) {
            int duration = 100; // 5秒

            // 周期性恢复生命值，但有冷却时间
            UUID playerId = player.getUUID();
            long currentTime = player.level().getGameTime();
            long lastTime = LAST_RECOVERY_TIME.getOrDefault(playerId, 0L);

            // 只有当生命值不是满的，且冷却时间已过，才进行恢复
            if (player.getHealth() < player.getMaxHealth() && currentTime - lastTime >= RECOVERY_COOLDOWN) {
                player.heal(HEAL_AMOUNT);
                LAST_RECOVERY_TIME.put(playerId, currentTime);
            }

            // 其他效果，只有当没有效果或效果等级低于我们提供的等级时才添加
            addEffectIfBetterOrAbsent(player, MobEffects.DAMAGE_RESISTANCE, duration, 0);
            addEffectIfBetterOrAbsent(player, MobEffects.FIRE_RESISTANCE, duration, 0);
            addEffectIfBetterOrAbsent(player, MobEffects.REGENERATION, duration, 0);
        } else {
            // 当玩家不再穿着暴君盔甲时，移除生命提升效果并保持生命百分比
            if (player.hasEffect(MobEffects.HEALTH_BOOST) && player.isAlive()) {
                // 先计算当前生命值百分比
                float healthPercentage = player.getHealth() / player.getMaxHealth();
                // 移除生命提升效果
                player.removeEffect(MobEffects.HEALTH_BOOST);
                // 保持原有的生命值百分比
                float newHealth = player.getMaxHealth() * healthPercentage;
                player.setHealth(Math.max(newHealth, 1.0f)); // 确保至少有1点生命值
            }
            // 保留其他通过原版药水获得的效果，不再直接移除
        }
    }

    // 辅助方法：只在玩家没有特定效果或现有效果等级低于我们提供的等级时才添加效果
    private static void addEffectIfBetterOrAbsent(Player player, MobEffect effect, int duration, int amplifier) {
        if (!player.hasEffect(effect) || player.getEffect(effect).getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
        }
    }

    /* ---------------- 伤害反弹 (已禁用) ---------------- */

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 反伤功能已禁用
        return;
    }

    /* ---------------- 生命偷取 ---------------- */

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (isWearingTyrant(player) && event.getEntity() instanceof LivingEntity && player.isAlive()) {
            LivingEntity target = (LivingEntity) event.getEntity();
            float healAmount = target.getMaxHealth() * 0.01f;
            player.heal(healAmount);
        }
    }

    /* ---------------- 工具方法 ---------------- */

    private static boolean isWearingTyrant(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.TYRANT_HELMET.get() ||
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.TYRANT_CHESTPLATE.get() ||
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.TYRANT_LEGGINGS.get();
    }

    // 虚化技能相关常量
    private static final int ENERGY_COST = 60; // 虚化技能消耗的骑士能量

    // 用于跟踪Shift键状态，防止连续触发
    private static final Map<UUID, Boolean> SHIFT_PRESSED = new HashMap<>();

    /* ---------------- 相位传送技能 ---------------- */

    /**
     * 执行相位传送 - 传送到视线所及障碍物的对面
     */
    public static void performPhaseTeleport(Player player) {
        if (player.level().isClientSide()) return;

        // 检查骑士能量是否足够
        if (!RiderEnergyHandler.consumeRiderEnergy(player, TELEPORT_ENERGY_COST)) {
            if (player instanceof ServerPlayer) {
                player.displayClientMessage(Component.literal("骑士能量不足，需要 " + TELEPORT_ENERGY_COST + " 点能量进行传送"), true);
            }
            return;
        }

        // 计算视线轨迹
        Vec3 startPos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = startPos.add(lookVec.scale(TELEPORT_RANGE));

        Level level = player.level();
        BlockHitResult hitResult = level.clip(new ClipContext(
                startPos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 找到障碍物，计算传送位置
            BlockPos hitPos = hitResult.getBlockPos();
            Direction hitDirection = hitResult.getDirection();

            // 计算传送目标位置（障碍物对面1-2格远的安全位置）
            Vec3 teleportPos = calculateSafeTeleportPosition(level, hitPos, hitDirection, player);

            if (teleportPos != null) {
                // 执行传送
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                    serverPlayer.displayClientMessage(Component.literal("相位传送完成！"), true);

                    // 传送粒子效果 - 使用红色系粒子
                    spawnTeleportParticles(serverPlayer);
                }
            } else {
                player.displayClientMessage(Component.literal("找不到安全的传送位置"), true);
                // 返还能量
                RiderEnergyHandler.addRiderEnergy(player, TELEPORT_ENERGY_COST);
            }
        } else {
            player.displayClientMessage(Component.literal("视线内没有可穿越的障碍物"), true);
            // 返还能量
            RiderEnergyHandler.addRiderEnergy(player, TELEPORT_ENERGY_COST);
        }
    }

    /**
     * 计算安全的传送位置
     */
    private static Vec3 calculateSafeTeleportPosition(Level level, BlockPos hitPos, Direction direction, Player player) {
        // 从障碍物表面向视线方向延伸寻找安全位置
        for (int distance = 1; distance <= 3; distance++) {
            BlockPos testPos = hitPos.relative(direction, distance);

            // 检查目标位置是否安全（有2x1x2的空间供玩家站立）
            if (isSafeTeleportLocation(level, testPos, player)) {
                return new Vec3(
                        testPos.getX() + 0.5,
                        testPos.getY(),
                        testPos.getZ() + 0.5
                );
            }
        }
        return null;
    }

    /**
     * 检查传送位置是否安全
     */
    private static boolean isSafeTeleportLocation(Level level, BlockPos pos, Player player) {
        // 检查目标位置和上方一格是否是非固体方块
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        // 检查目标位置下方是否有支撑方块
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }

        return true;
    }

    /**
     * 生成传送粒子效果 - 在服务端调用，同步到客户端
     */
    private static void spawnTeleportParticles(ServerPlayer player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        Vec3 pos = player.position();

        // 创建鲜红色粒子参数 (RGB: 1.0, 0.1, 0.1) 大小 1.2
        DustParticleOptions brightRedParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.1F, 0.1F),
                1.2F
        );

        // 在服务端发送粒子到所有客户端
        serverLevel.sendParticles(
                brightRedParticle, // 粒子参数
                pos.x, pos.y, pos.z, // 坐标
                30, // 粒子数量
                1.5, 1.5, 1.5, // 分布范围 (deltaX, deltaY, deltaZ)
                0.1 // 速度
        );

        // 创建浅红色粒子参数 (RGB: 1.0, 0.4, 0.4) 大小 0.8
        DustParticleOptions lightRedParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.4F, 0.4F),
                0.8F
        );

        serverLevel.sendParticles(
                lightRedParticle,
                pos.x, pos.y, pos.z,
                20,
                1.2, 1.2, 1.2,
                0.05
        );
    }

    /* ---------------- 虚化技能（相位穿透+飞行） ---------------- */

    /**
     * 切换玩家的虚化状态（相位穿透+飞行）
     */
    public static void toggleIntangibility(Player player) {
        UUID playerId = player.getUUID();

        if (INTANGIBLE_PLAYERS.containsKey(playerId)) {
            // 解除虚化状态
            deactivateIntangibility(player);
        } else {
            // 检查骑士能量是否足够激活持续模式
            if (!RiderEnergyHandler.consumeRiderEnergy(player, ENERGY_COST)) {
                if (player instanceof ServerPlayer) {
                    player.displayClientMessage(Component.literal("骑士能量不足，需要 " + ENERGY_COST + " 点能量激活虚化模式"), true);
                }
                return;
            }

            // 激活相位穿透+飞行模式
            activateIntangibility(player);
        }
    }

    /**
     * 激活相位穿透+飞行模式
     */
    private static void activateIntangibility(Player player) {
        UUID playerId = player.getUUID();

        INTANGIBLE_PLAYERS.put(playerId, 0);
        INTANGIBILITY_TIMERS.put(playerId, INTANGIBILITY_MAX_DURATION);
        player.displayClientMessage(Component.literal("虚化模式已激活 - 持续60秒，获得飞行能力和相位穿透能力"), true);

        // 在服务端启用飞行能力
        if (player instanceof ServerPlayer serverPlayer) {
            // 启用飞行能力
            serverPlayer.getAbilities().mayfly = true;
            serverPlayer.getAbilities().flying = true; // 自动开始飞行

            // 同步到客户端
            serverPlayer.onUpdateAbilities();

            // 添加红色发光效果
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
            
            // 发送同步数据包到所有客户端
            IntangibilitySyncPacket.sendToAll((ServerLevel) player.level(), playerId, true);
        }
    }

    /**
     * 解除相位穿透+飞行模式
     */
    private static void deactivateIntangibility(Player player) {
        UUID playerId = player.getUUID();

        INTANGIBLE_PLAYERS.remove(playerId);
        INTANGIBILITY_TIMERS.remove(playerId);
        SHIFT_PRESSED.remove(playerId); // 清理Shift状态
        player.displayClientMessage(Component.literal("虚化模式已解除"), true);

        // 在服务端禁用飞行能力
        if (player instanceof ServerPlayer serverPlayer) {
            // 如果玩家在生存模式，禁用飞行能力
            if (!serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
            }

            // 同步到客户端
            serverPlayer.onUpdateAbilities();

            // 移除发光效果
            serverPlayer.removeEffect(MobEffects.GLOWING);
            
            // 发送同步数据包到所有客户端
            IntangibilitySyncPacket.sendToAll((ServerLevel) player.level(), playerId, false);
        }
    }

    /**
     * 处理虚化模式下的玩家状态
     */
    @SubscribeEvent
    public static void onIntangiblePlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        UUID playerId = player.getUUID();

        // 检查玩家是否处于虚化模式
        if (INTANGIBLE_PLAYERS.containsKey(playerId)) {
            if (player.level().isClientSide()) {
                // 客户端Shift键检测
                handleShiftKeyPress(player);
            } else {
                // 服务端：处理持续时间和状态维持
                int remainingTime = INTANGIBILITY_TIMERS.getOrDefault(playerId, 0) - 1;

                if (remainingTime <= 0) {
                    // 时间到，解除虚化模式
                    deactivateIntangibility(player);
                } else {
                    INTANGIBILITY_TIMERS.put(playerId, remainingTime);

                    // 确保飞行能力持续生效
                    if (player instanceof ServerPlayer serverPlayer) {
                        // 确保飞行能力
                        if (!serverPlayer.getAbilities().mayfly) {
                            serverPlayer.getAbilities().mayfly = true;
                            serverPlayer.getAbilities().flying = true;
                            serverPlayer.onUpdateAbilities();
                        }

                        // 防止玩家飞得太高或太低
                        if (serverPlayer.getY() < serverPlayer.level().getMinBuildHeight()) {
                            serverPlayer.teleportTo(
                                    serverPlayer.getX(),
                                    serverPlayer.level().getMinBuildHeight() + 5,
                                    serverPlayer.getZ()
                            );
                        }

                        // 每20tick在服务端生成一次粒子效果
                        if (remainingTime % 20 == 0) {
                            spawnPhaseModeParticlesOnServer(serverPlayer);
                        }
                    }

                    // 每5秒显示剩余时间
                    if (remainingTime % 100 == 0) {
                        int seconds = remainingTime / 20;
                        player.displayClientMessage(Component.literal("虚化模式剩余时间: " + seconds + "秒"), true);
                    }
                }
            }
        }
    }

    /**
     * 客户端处理Shift键按下
     */
    @OnlyIn(Dist.CLIENT)
    private static void handleShiftKeyPress(Player player) {
        UUID playerId = player.getUUID();
        boolean isShiftPressed = player.isShiftKeyDown();
        boolean wasShiftPressed = SHIFT_PRESSED.getOrDefault(playerId, false);

        // 检测Shift键按下（从没按到按的状态变化）
        if (isShiftPressed && !wasShiftPressed) {
            // 使用玩家交互来触发服务端的相位穿透
            // 这里设置使用键为按下状态，触发交互事件
            Minecraft.getInstance().options.keyUse.setDown(true);
            // 立即设置为抬起，以模拟点击效果
            Minecraft.getInstance().options.keyUse.setDown(false);
        }

        // 更新Shift键状态
        SHIFT_PRESSED.put(playerId, isShiftPressed);
    }

    /**
     * 监听玩家交互事件来处理相位穿透
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        // 只在服务端处理，且玩家处于虚化模式，并且是右键点击空气或方块
        if (!player.level().isClientSide() && INTANGIBLE_PLAYERS.containsKey(playerId)) {
            // 检查是否是Shift状态下的交互
            if (player.isShiftKeyDown()) {
                event.setCanceled(true); // 取消原版交互
                performDirectPhasePenetration((ServerPlayer) player);
            }
        }
    }

    /**
     * 执行直接相位穿透（直接穿过前方的方块）
     */
    private static void performDirectPhasePenetration(ServerPlayer player) {
        // 消耗少量能量用于传送
        if (!RiderEnergyHandler.consumeRiderEnergy(player, 5)) {
            player.displayClientMessage(Component.literal("骑士能量不足，需要 5 点能量进行相位穿透"), true);
            return;
        }

        // 沿着视线方向寻找第一个安全的位置
        Vec3 startPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        // 从玩家当前位置开始，沿着视线方向寻找安全位置
        for (double distance = 1.0; distance <= 10.0; distance += 1.0) {
            Vec3 testPos = startPos.add(lookVec.scale(distance));
            BlockPos blockPos = new BlockPos((int) testPos.x, (int) testPos.y, (int) testPos.z);

            // 检查这个位置是否安全
            if (isSafePenetrationLocation(player.level(), blockPos, player)) {
                // 找到安全位置，传送过去
                player.teleportTo(testPos.x, testPos.y, testPos.z);
                player.displayClientMessage(Component.literal("相位穿透完成！"), true);

                // 传送粒子效果
                spawnTeleportParticles(player);
                return;
            }
        }

        // 如果没找到安全位置，尝试传送到最远的位置
        Vec3 farthestPos = startPos.add(lookVec.scale(10.0));
        BlockPos farthestBlockPos = new BlockPos((int) farthestPos.x, (int) farthestPos.y, (int) farthestPos.z);

        // 强制寻找一个相对安全的位置
        Vec3 forcedPos = findForcedSafePosition(player.level(), farthestBlockPos, player);
        if (forcedPos != null) {
            player.teleportTo(forcedPos.x, forcedPos.y, forcedPos.z);
            player.displayClientMessage(Component.literal("相位穿透完成！"), true);
            spawnTeleportParticles(player);
        } else {
            player.displayClientMessage(Component.literal("找不到安全的穿透位置"), true);
            // 返还能量
            RiderEnergyHandler.addRiderEnergy(player, 5);
        }
    }

    /**
     * 强制寻找安全位置（如果常规方法找不到）
     */
    private static Vec3 findForcedSafePosition(Level level, BlockPos startPos, Player player) {
        // 在起始位置周围寻找安全位置
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos testPos = startPos.offset(x, y, z);
                    if (isSafePenetrationLocation(level, testPos, player)) {
                        return new Vec3(
                                testPos.getX() + 0.5,
                                testPos.getY(),
                                testPos.getZ() + 0.5
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * 检查穿透位置是否安全（需要2格高的空间）
     */
    private static boolean isSafePenetrationLocation(Level level, BlockPos pos, Player player) {
        // 检查目标位置和上方一格是否是非固体方块（玩家需要2格高空间）
        if (!level.getBlockState(pos).isAir() ||
                !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        // 检查目标位置下方是否有支撑方块
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }

        return true;
    }

    /**
     * 在服务端生成虚化模式的红色系粒子效果，所有客户端都能看到
     */
    private static void spawnPhaseModeParticlesOnServer(ServerPlayer player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        Vec3 pos = player.position();

        // 创建核心能量粒子（最靠近玩家）
        DustParticleOptions coreParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.0F, 0.0F), // 纯红色
                1.5F // 较大的粒子大小
        );

        // 核心粒子 - 非常靠近玩家，模拟能量核心
        for (int i = 0; i < 30; i++) { // 增加到30个粒子
            // 较小的分布范围，使粒子更靠近玩家
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 1.0;
            double offsetY = 0.5 + player.getRandom().nextDouble() * 1.0; // 集中在玩家身体区域
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 1.0;

            serverLevel.sendParticles(
                    coreParticle,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0, // 不需要额外粒子
                    0, 0.02, 0, // 轻微向上的速度
                    0 // 不需要额外速度
            );
        }

        // 创建中间层粒子（稍大的分布范围）
        DustParticleOptions middleParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.2F, 0.2F), // 亮红色
                1.2F
        );

        // 中间层粒子 - 围绕玩家身体，添加向内汇聚效果
        for (int i = 0; i < 40; i++) { // 增加到40个粒子
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = player.getRandom().nextDouble() * 1.8;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 1.5;

            // 粒子有轻微的向内汇聚的速度
            double velocityX = -offsetX * 0.05;
            double velocityY = 0.02; // 轻微向上
            double velocityZ = -offsetZ * 0.05;

            serverLevel.sendParticles(
                    middleParticle,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0, // 不需要额外粒子
                    velocityX, velocityY, velocityZ, // 向内汇聚的速度
                    0 // 不需要额外速度
            );
        }

        // 创建外层环绕粒子（形成能量体轮廓）
        DustParticleOptions outerParticle = new DustParticleOptions(
                new Vector3f(0.8F, 0.1F, 0.1F), // 深红色
                1.0F
        );

        // 环绕玩家的粒子环，形成能量体的轮廓
        for (int angle = 0; angle < 360; angle += 10) { // 更密集的环绕
            double radians = Math.toRadians(angle);
            double radius = 1.8; // 减小环绕半径，使粒子更靠近玩家
            
            // 水平环绕
            double x = pos.x + Math.cos(radians) * radius;
            double z = pos.z + Math.sin(radians) * radius;
            
            // 垂直方向多层分布，但更靠近玩家
            for (double y = 0.2; y <= 1.8; y += 0.3) {
                // 粒子有环绕玩家旋转的速度
                double velocityX = -Math.sin(radians) * 0.1;
                double velocityY = 0.01; // 轻微向上
                double velocityZ = Math.cos(radians) * 0.1;
                
                serverLevel.sendParticles(
                        outerParticle,
                        x,
                        pos.y + y,
                        z,
                        0,
                        velocityX, velocityY, velocityZ, // 环绕速度
                        0
                );
            }
        }
        
        // 添加能量流动效果 - 向上流动的粒子流
        DustParticleOptions flowParticle = new DustParticleOptions(
                new Vector3f(1.0F, 0.4F, 0.4F), // 浅红色
                0.8F
        );
        
        // 从玩家脚部向上流动的粒子，模拟能量流动
        for (int i = 0; i < 20; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
            double offsetY = player.getRandom().nextDouble() * 0.5; // 从脚部开始
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;
            
            // 向上流动的速度
            serverLevel.sendParticles(
                    flowParticle,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0,
                    0, 0.2, 0, // 向上的速度
                    0
            );
        }
    }



    /**
     * 虚化模式下的伤害处理（保持原有的免疫机制）
     */
    @SubscribeEvent
    public static void onIntangiblePlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        if (INTANGIBLE_PLAYERS.containsKey(playerId)) {
            float damageAmount = event.getAmount();

            if (damageAmount < INTANGIBILITY_BREAK_THRESHOLD) {
                // 伤害低于阈值，完全免疫
                event.setCanceled(true);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("相位位移免疫了此次伤害"), true);
                }
            } else {
                // 伤害高于阈值，打破虚化模式
                deactivateIntangibility(player);

                // 只保留超过阈值的部分伤害
                event.setAmount(damageAmount - INTANGIBILITY_BREAK_THRESHOLD);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("虚化模式被打破！受到了强力攻击"), true);
                }
            }
        }
    }

    /**
     * 检查玩家是否处于虚化模式
     */
    public static boolean isPlayerIntangible(Player player) {
        if (player == null) return false;
        return INTANGIBLE_PLAYERS.containsKey(player.getUUID());
    }

    /**
     * 同步玩家虚化状态的数据包
     */
    public static class IntangibilitySyncPacket {
        private final UUID playerId;
        private final boolean isIntangible;

        public IntangibilitySyncPacket(UUID playerId, boolean isIntangible) {
            this.playerId = playerId;
            this.isIntangible = isIntangible;
        }

        public static void encode(IntangibilitySyncPacket packet, FriendlyByteBuf buf) {
            buf.writeUUID(packet.playerId);
            buf.writeBoolean(packet.isIntangible);
        }

        public static IntangibilitySyncPacket decode(FriendlyByteBuf buf) {
            UUID playerId = buf.readUUID();
            boolean isIntangible = buf.readBoolean();
            return new IntangibilitySyncPacket(playerId, isIntangible);
        }

        public static void handle(IntangibilitySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                // 在客户端更新玩家的虚化状态
                if (Minecraft.getInstance().level != null) {
                    if (packet.isIntangible) {
                        INTANGIBLE_PLAYERS.put(packet.playerId, 0);
                    } else {
                        INTANGIBLE_PLAYERS.remove(packet.playerId);
                    }
                }
            });
            context.setPacketHandled(true);
        }

        /**
         * 从服务端发送到所有客户端
         */
        public static void sendToAll(ServerLevel level, UUID playerId, boolean isIntangible) {
            IntangibilitySyncPacket packet = new IntangibilitySyncPacket(playerId, isIntangible);
            for (Player player : level.players()) {
                CHANNEL.sendTo(packet, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}