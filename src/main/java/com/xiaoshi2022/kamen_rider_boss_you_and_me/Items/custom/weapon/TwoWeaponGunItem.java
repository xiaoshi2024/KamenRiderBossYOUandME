package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon.TwoWeaponRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.BatStampItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.function.Consumer;

public class TwoWeaponGunItem extends TwoWeaponItem {



    // 参考假面骑士艾比尔的武器性能调整数值
    private static final Tier CUSTOM_TIER = new Tier() {
        public int getUses()            { return 2500; } // 耐久度大幅提升
        public float getSpeed()         { return 8.0F; } // 攻击速度更快
        public float getAttackDamageBonus() { return 8.0F; } // 基础伤害提升
        public int getLevel()           { return 3; } // 等级提升
        public int getEnchantmentValue(){ return 20; } // 附魔能力更强
        public Ingredient getRepairIngredient() { return Ingredient.of(); }
    };

    // 武器伤害数值
    private static final float GUN_MODE_DAMAGE = 28.0F; // 枪模式伤害
    private static final int COOLDOWN_TICKS = 21; // 冷却时间（以tick为单位，20tick=1秒）
    
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlayAndHold("gun");

    // 用于物品注册的静态引用
    public static TwoWeaponGunItem ITEM = null; // 会在ModItems中被赋值

    public TwoWeaponGunItem(Properties p) {
        super(p); // 调用父类构造函数
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private TwoWeaponRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new TwoWeaponRenderer();

                return this.renderer;
            }
        });
    }

    // 处理idle动画状态
    private PlayState handleIdleAnimation(AnimationState<TwoWeaponGunItem> state) {
        state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主要的idle状态控制器，始终运行
        controllers.add(new AnimationController<>(this, "idle_controller", 5, this::handleIdleAnimation));
    }

    private void shootEnergyBeam(Player player, Level level) {
        Vec3 from = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0F);
        final double RANGE = 60.0D, STEP = 0.2D; // 射程增加，步长减小以提高精度

        boolean hit = false;
        for (double d = 0; d < RANGE && !hit; d += STEP) {
            Vec3 pos = from.add(dir.scale(d));

            // 增强粒子效果
            if (level.isClientSide) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
                // 添加更多粒子效果
                level.addParticle(ParticleTypes.ENCHANT,
                        pos.x, pos.y, pos.z,
                        0.1, 0.1, 0.1);
            } else {
                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z, 2, 0, 0, 0, 0.1);
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }

            if (!level.isClientSide) {
                AABB bb = new AABB(pos, pos).inflate(0.3D); // 碰撞箱增大
                for (LivingEntity tgt : level.getEntitiesOfClass(LivingEntity.class, bb,
                        e -> !e.is(player) && !e.isInvulnerable())) {
                    // 造成更高伤害
                    tgt.hurt(player.damageSources().playerAttack(player), GUN_MODE_DAMAGE);
                    // 添加击退效果
                    Vec3 knockbackDir = tgt.position().subtract(player.position()).normalize();
                    tgt.setDeltaMovement(knockbackDir.x * 0.8, 0.3, knockbackDir.z * 0.8);
                    hit = true;
                    break;
                }
            }
        }

        if (!level.isClientSide) {
            // 增强音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 1.3F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 2.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 在bat变种情况下进行检测和处理
        Variant currentVariant = getVariant(stack);

        /* -------- 副手拿枪形态武器，主手拿蝙蝠印章，右键将武器变为bat形态 -------- */
        if (hand == InteractionHand.OFF_HAND && !level.isClientSide) {
            ItemStack mainHandStack = player.getMainHandItem();
            if (mainHandStack.getItem() instanceof BatStampItem && 
                currentVariant == Variant.DEFAULT) {
                // 将副手中的枪形态武器设置为BAT变种
                setVariant(stack, Variant.BAT);
                
                // 播放转换音效
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.5F);
                level.playSound(null, player.blockPosition(),
                        ModBossSounds.BAT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // 从主手中移除蝙蝠印章
                mainHandStack.shrink(1);
                if (mainHandStack.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                } else {
                    player.setItemInHand(InteractionHand.MAIN_HAND, mainHandStack);
                }
                
                // 发送消息提示玩家
                player.sendSystemMessage(
                        Component.literal("已将枪形态武器注入蝙蝠力量！")
                );
                
                return InteractionResultHolder.success(stack);
            }
        }

        /* -------- Shift + 右键：切换到剑模式 -------- */
        if (player.isShiftKeyDown() && !player.isUsingItem()) {
            if (!level.isClientSide) {
                // 创建新的剑形态武器
                ItemStack swordStack = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.TWO_WEAPON_SWORD.get(), 1);
                
                // 复制耐久度等重要属性
                if (stack.isDamageableItem()) {
                    swordStack.setDamageValue(stack.getDamageValue());
                }
                
                // 复制NBT数据
                if (stack.hasTag()) {
                    CompoundTag tag = stack.getTag().copy();
                    swordStack.setTag(tag);
                }
                
                // 如果原武器是bat变种，确保新武器也是bat变种
                if (currentVariant == Variant.BAT) {
                    setVariant(swordStack, Variant.BAT);
                }
                
                // 替换物品
                player.setItemInHand(hand, swordStack);
                
                // 播放切换音效
                level.playSound(null, player.blockPosition(),
                        SoundEvents.PISTON_CONTRACT, SoundSource.PLAYERS, 1.0F, 0.9F);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.7F, 1.2F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        /* -------- 正常右键：枪模式攻击 -------- */
        if (!level.isClientSide) {
            // 检查是否处于冷却中
            if (!player.getCooldowns().isOnCooldown(this)) {
                shootEnergyBeam(player, level);
                // 设置冷却时间
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.consume(stack);
    }


}