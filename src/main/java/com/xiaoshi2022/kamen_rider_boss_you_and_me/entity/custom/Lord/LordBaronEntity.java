package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.Globalism;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SyncOwnerPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems.FactionLeader;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.helheim_crack;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LordBaronEntity extends GiifuDemosEntity implements FactionLeader {
    private String factionId = UUID.randomUUID().toString(); // 每个星爵巴隆有自己的阵营ID

    private static final Item GLOBALISM_WEAPON = ModItems.GLOBALISM.get();
    // 自定义动画
    private static final RawAnimation BARON_IDLE = RawAnimation.begin().thenLoop("baron_idle");
    private static final RawAnimation BARON_ATTACK = RawAnimation.begin().thenLoop("baron_attack");
    private static final RawAnimation BARON_WALK = RawAnimation.begin().thenLoop("baron_walk");

    // 对话系统
    private static final Map<String, String[]> DIALOGUE_TREE = new HashMap<>();
    private final Random random = new Random();

    private static final double CONTROL_RANGE = 15.0;
    private int commandCooldown = 0;

    private int skillCooldown = 0; // 初始无冷却

    static {
        // 初始化多语言对话树
        DIALOGUE_TREE.put("greet", new String[]{
                "msg.kamen_rider.baron.greet1",
                "msg.kamen_rider.baron.greet2",
                "msg.kamen_rider.baron.greet3"
        });
        DIALOGUE_TREE.put("ask_power", new String[]{
                "msg.kamen_rider.baron.power1",
                "msg.kamen_rider.baron.power2",
                "msg.kamen_rider.baron.power3"
        });
        DIALOGUE_TREE.put("challenge", new String[]{
                "msg.kamen_rider.baron.challenge1",
                "msg.kamen_rider.baron.challenge2",
                "msg.kamen_rider.baron.challenge3"
        });
        DIALOGUE_TREE.put("victory", new String[]{
                "msg.kamen_rider.baron.victory"
        });
    }

    private LivingEntity owner;

    public LordBaronEntity(EntityType<? extends GiifuDemosEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.translatable("entity.kamen_rider.baron.name"));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return GiifuDemosEntity.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MAX_HEALTH, 150.0);
    }

    //星爵巴隆的技能
    @Override
    public boolean doHurtTarget(Entity target) {
        System.out.println("doHurtTarget called with target: " + target);
        if (target instanceof LivingEntity) {
            this.setLastHurtMob((LivingEntity) target);
            this.level().getEntitiesOfClass(ElementaryInvesHelheim.class,
                            new AABB(this.blockPosition()).inflate(30.0))
                    .stream()
                    .filter(inves -> inves.getMaster() == this)
                    .forEach(inves -> inves.setTarget((LivingEntity) target));
        }

        // 检查技能冷却时间
        if (skillCooldown <= 0) {
            // 只有在攻击玩家时才触发技能
            if (target instanceof Player) {
                openHelheimCracksAndSpawnInves(target);
                skillCooldown = 200; // 重置冷却时间
            }
        }

        // 3. 新增逻辑：30 % 概率掏出 Globalism 并追加伤害
        if (target instanceof Player) {
            // 30 % 概率且当前主手为空
            if (random.nextFloat() < 0.3f && this.getMainHandItem().isEmpty()) {
                ItemStack globalismStack = new ItemStack(GLOBALISM_WEAPON);
                this.setItemInHand(InteractionHand.MAIN_HAND, globalismStack);

                // 拔剑音效
                this.level().playSound(
                        null,
                        this.blockPosition(),
                        SoundEvents.ARMOR_EQUIP_IRON,
                        SoundSource.HOSTILE,
                        1.0f,
                        1.2f
                );
            }

            // 若主手是 Globalism，则追加一次额外伤害
            ItemStack held = this.getMainHandItem();
            if (held.getItem() instanceof Globalism) {
                float bonusDamage = 10.4f; // Globalism 的附加攻击
                target.hurt(this.damageSources().mobAttack(this), bonusDamage);
            }
        }

        return super.doHurtTarget(target);
    }

    // 在类中添加这个成员变量
    private final List<CrackParam> crackParams = new ArrayList<>();

    private static class CrackParam {
        final double x, y, z;
        final long spawnTime;

        public CrackParam(double x, double y, double z, long spawnTime) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spawnTime = spawnTime;
        }
    }

    private void openHelheimCracksAndSpawnInves(Entity target) {
        int numCracks = random.nextInt(3) + 3; // 生成3到5个裂缝
        for (int i = 0; i < numCracks; i++) {
            double crackX = this.getX() + (random.nextDouble() - 0.5) * 16.0;
            double crackZ = this.getZ() + (random.nextDouble() - 0.5) * 16.0;
            int crackY = findGroundLevel(crackX, crackZ); // 找地面高度

            BlockPos randomPos = new BlockPos((int)crackX, crackY, (int)crackZ);

            // 检查生成位置是否为空，如果是，则放置方块
            if (this.level().isEmptyBlock(randomPos)) {
                BlockState state = com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks.HELHEIM_CRACK_BLOCK.get().defaultBlockState();
                this.level().setBlockAndUpdate(randomPos, state);
                simulateRightClickEffect(this.level(), randomPos);
            }

            // 记录裂缝参数
            crackParams.add(new CrackParam(crackX, crackY, crackZ, level().getGameTime()));

            // 播放粒子效果
            spawnCrackParticles(crackX, crackY, crackZ);
        }

        // 生成关联的异域者
        spawnInvesWithMathLink(target);
    }

    private void simulateRightClickEffect(Level level, BlockPos pos) {
        // 获取方块状态并设置动画状态为1
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof helheim_crack) {
            level.setBlockAndUpdate(pos, state.setValue(helheim_crack.ANIMATION, 1));
            // 播放音效
            level.playSound(null, pos, ModSounds.OPENDLOCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
    private int findGroundLevel(double x, double z) {
        // 从实体当前位置开始搜索
        int startY = (int) this.getY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int)x, startY, (int)z);

        // 1. 首先向下找到第一个固体方块（地面）
        while (pos.getY() > level().getMinBuildHeight() &&
                !level().getBlockState(pos).isSolid()) {
            pos.move(Direction.DOWN);
        }

        // 2. 然后向上移动1格，确保在地面之上
        if (pos.getY() < level().getMaxBuildHeight()) {
            pos.move(Direction.UP);
        }

        // 3. 验证目标位置是否可放置（非固体方块）
        if (level().isEmptyBlock(pos)) {
            return pos.getY();
        }

        // 4. 如果上方被占据，尝试继续向上找
        while (pos.getY() < level().getMaxBuildHeight() &&
                !level().isEmptyBlock(pos)) {
            pos.move(Direction.UP);
        }

        return pos.getY();
    }

    private void spawnCrackParticles(double x, double y, double z) {
        // 烟雾粒子
        for (int j = 0; j < 30; j++) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    x + (random.nextDouble() - 0.5) * 2.0,
                    y + 1.0 + random.nextDouble() * 2.0,
                    z + (random.nextDouble() - 0.5) * 2.0,
                    0, 0.1, 0);
        }

        // 火焰粒子
        for (int j = 0; j < 20; j++) {
            this.level().addParticle(ParticleTypes.FLAME,
                    x + (random.nextDouble() - 0.5) * 2.0,
                    y + 1.0 + random.nextDouble() * 2.0,
                    z + (random.nextDouble() - 0.5) * 2.0,
                    0, 0.1, 0);
        }
    }

    private void spawnInvesWithMathLink(Entity target) {
        // 获取最近生成的裂缝参数
        long currentTime = level().getGameTime();
        List<CrackParam> validCracks = crackParams.stream()
                .filter(c -> currentTime - c.spawnTime < 100) // 5秒内的裂缝
                .collect(Collectors.toList());

        // 为每个有效裂缝生成异域者
        validCracks.forEach(crack -> {
            // 极坐标偏移
            double angle = random.nextDouble() * Math.PI * 2;
            double offset = 0.5 + random.nextDouble() * 1.5;

            // 计算异域者位置
            double invesX = crack.x + Math.cos(angle) * offset;
            double invesZ = crack.z + Math.sin(angle) * offset;
            double invesY = crack.y + 0.2;

            // 生成异域者
            ElementaryInvesHelheim inves = new ElementaryInvesHelheim(
                    ModEntityTypes.INVES_HEILEHIM.get(),
                    level()
            );
            inves.moveTo(invesX, invesY, invesZ, 0, 0);
            inves.setMaster(this);
            inves.setTarget((LivingEntity)target);
            level().addFreshEntity(inves);

            // 生成连接粒子
            spawnLinkParticles(crack.x, crack.y, crack.z, invesX, invesY, invesZ);
        });

        // 清理过期裂缝
        crackParams.removeIf(c -> currentTime - c.spawnTime >= 100);
    }

    private void spawnLinkParticles(double x1, double y1, double z1,
                                    double x2, double y2, double z2) {
        for (int i = 0; i < 8; i++) {
            float progress = i / 8f;
            double px = x1 + (x2 - x1) * progress;
            double py = y1 + (y2 - y1) * progress + 0.5 * Math.sin(progress * Math.PI);
            double pz = z1 + (z2 - z1) * progress;

            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    px, py, pz,
                    0, 0.02, 0
            );
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (this.swinging) {
                return state.setAndContinue(BARON_ATTACK);
            } else if (state.isMoving()) {
                return state.setAndContinue(BARON_WALK);
            }
            return state.setAndContinue(BARON_IDLE);
        }));
    }

    // 基础交互方法
    public void interactWith(Player player) {
        if (!this.level().isClientSide) {
            sendLocalizedDialogue(player, "greet");
        }
    }

    // 带参数的高级对话
    public void sendLocalizedDialogue(Player player, String topic, Object... args) {
        if (DIALOGUE_TREE.containsKey(topic)) {
            String[] keys = DIALOGUE_TREE.get(topic);
            String randomKey = keys[random.nextInt(keys.length)];
            player.sendSystemMessage(Component.translatable(randomKey, args));
        } else {
            player.sendSystemMessage(Component.translatable("msg.kamen_rider.unknown"));
        }
    }

    // 特定交互方法
    public void askAboutPower(Player player) {
        sendLocalizedDialogue(player, "ask_power");
    }

    public void challengePlayer(Player player) {
        sendLocalizedDialogue(player, "challenge");
        this.setTarget(player);
        // 这里可以添加战斗开始的动画或逻辑
    }

    public void declareVictory(Player player, int winCount) {
        sendLocalizedDialogue(player, "victory", winCount);
    }

    @Override
    public void tick() {
        super.tick();

        // 星爵巴隆每10tick检查一次周围异域者
        if (!this.level().isClientSide && this.tickCount % 10 == 0) {
            commandNearbyInves();
        }

        // 冷却时间递减
        if (skillCooldown > 0) {
            skillCooldown--;
        }

        if (commandCooldown > 0) {
            commandCooldown--;
        }
    }

    private void commandNearbyInves() {
        // 1. 获取周围异域者
        List<ElementaryInvesHelheim> invesList = this.level().getEntitiesOfClass(
                ElementaryInvesHelheim.class,
                new AABB(this.blockPosition()).inflate(15.0),
                inves -> inves.isAlive() &&
                        (inves.getMaster() == null || inves.getMaster() == this)
        );

        // 2. 获取攻击者
        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker == null || commandCooldown > 0) return;

        // 3. 命令异域者攻击
        invesList.forEach(inves -> {
            if (inves.master == null) {
                inves.setMaster(this); // 建立控制关系
            }
            inves.setTarget(attacker);
            inves.setFlying(true);

            // 强化效果
            inves.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST, 200, 1));
            inves.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 200, 1));
        });

        // 4. 播放控制音效和粒子
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.HOSTILE,
                1.0f, 0.8f);

        for (int i = 0; i < 10; i++) {
            this.level().addParticle(ParticleTypes.ENCHANT,
                    this.getRandomX(1.5),
                    this.getRandomY() + 1.0,
                    this.getRandomZ(1.5),
                    0, 0.1, 0);
        }

        commandCooldown = 100; // 5秒冷却
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 记录攻击者
        if (source.getEntity() instanceof LivingEntity) {
            this.setLastHurtByMob((LivingEntity) source.getEntity());
        }
        return super.hurt(source, amount);
    }

    @Override
    public String getFactionId() {
        return this.factionId;
    }

    @Override
    public void commandMinions(Consumer<ElementaryInvesHelheim> action) {
        getFaction().ifPresent(faction -> {
            faction.minions.forEach(minion -> {
                if (minion.isAlive()) {
                    action.accept(minion);
                }
            });
        });
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        if (!this.level().isClientSide && owner != null) {
            // 使用PacketHandler发送
            PacketHandler.sendToAllTracking(
                    new SyncOwnerPacket(this.getId(), owner.getUUID()),
                    this
            );
        }
    }

    @Override
    public LivingEntity getOwner() {
        return owner;
    }

    // 保存/加载数据
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) {
            tag.putUUID("Owner", this.owner.getUUID());
        }
        if (this.factionId != null) {
            tag.putUUID("FactionId", UUID.fromString(this.factionId));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // 安全读取 Owner
        if (tag.contains("Owner", Tag.TAG_INT_ARRAY)) { // UUID 以 INT_ARRAY 形式存储
            this.owner = this.level().getPlayerByUUID(tag.getUUID("Owner"));
        }

        // 安全读取 FactionId
        if (tag.contains("FactionId", Tag.TAG_INT_ARRAY)) {
            this.factionId = String.valueOf(tag.getUUID("FactionId"));
        } else {
            this.factionId = String.valueOf(UUID.randomUUID()); // 不存在则新建
        }
    }

//    @SubscribeEvent
//    public static void onLivingAttack(LivingAttackEvent event) {
//        // 检查受害者是否是LordBaron
//        if (event.getEntity() instanceof LordBaronEntity) {
//            // 检查攻击者是否是异域者
//            if (event.getSource().getDirectEntity() instanceof ElementaryInvesHelheim) {
//                event.setCanceled(true); // 取消攻击
//            }
//        }
//    }

}