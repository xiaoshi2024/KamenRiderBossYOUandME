package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SyncOwnerPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems.FactionLeader;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class LordBaronEntity extends GiifuDemosEntity implements FactionLeader {
    private String factionId = UUID.randomUUID().toString(); // 每个星爵巴隆有自己的阵营ID
    // 自定义动画
    private static final RawAnimation BARON_IDLE = RawAnimation.begin().thenLoop("baron_idle");
    private static final RawAnimation BARON_ATTACK = RawAnimation.begin().thenLoop("baron_attack");
    private static final RawAnimation BARON_WALK = RawAnimation.begin().thenLoop("baron_walk");

    // 对话系统
    private static final Map<String, String[]> DIALOGUE_TREE = new HashMap<>();
    private final Random random = new Random();

    private static final double CONTROL_RANGE = 15.0;
    private int commandCooldown = 0;

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
                SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE,
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
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity) {
            this.setLastHurtMob((LivingEntity) target);
            // 通知所有跟随的异域者
            this.level().getEntitiesOfClass(ElementaryInvesHelheim.class,
                            new AABB(this.blockPosition()).inflate(30.0))
                    .stream()
                    .filter(inves -> inves.getMaster() == this)
                    .forEach(inves -> inves.setTarget((LivingEntity) target));
        }
        return super.doHurtTarget(target);
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