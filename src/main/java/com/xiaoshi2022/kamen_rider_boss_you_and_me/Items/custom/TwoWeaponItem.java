package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon.TwoWeaponRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class TwoWeaponItem extends SwordItem implements GeoItem {

    private static final Tier CUSTOM_TIER = new Tier() {
        public int getUses()            { return 1000; }
        public float getSpeed()         { return 4.0F; }
        public float getAttackDamageBonus() { return 4.0F; }
        public int getLevel()           { return 0; }
        public int getEnchantmentValue(){ return 15; }
        public Ingredient getRepairIngredient() { return Ingredient.of(); }
    };

    private static final RawAnimation BLADE_ANIM = RawAnimation.begin().thenPlay("blade");
    private static final RawAnimation GUN_ANIM = RawAnimation.begin().thenPlay("gun");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 模式标签
    private static final String MODE_TAG = "TwoWeaponMode"; // true=刀模式, false=枪模式

    public TwoWeaponItem(Properties p) {
        super(CUSTOM_TIER, 3, -2.4F, p);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public enum Variant {
        DEFAULT, BAT
    }

    public void setWeaponType(ItemStack stack, Variant type) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("WeaponType", type.name());
    }

    public Variant getWeaponType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("WeaponType")) {
            return Variant.valueOf(tag.getString("WeaponType"));
        }
        return Variant.DEFAULT;
    }

    private static final String VARIANT_TAG = "TwoWeaponVariant";

    public static Variant getVariant(ItemStack stack) {
        try {
            return Variant.valueOf(stack.getOrCreateTag().getString(VARIANT_TAG));
        } catch (Exception e) {
            return Variant.DEFAULT;
        }
    }

    public static void setVariant(ItemStack stack, Variant v) {
        stack.getOrCreateTag().putString(VARIANT_TAG, v.name());
    }

    public boolean isBladeMode(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(MODE_TAG);
    }

    public void setBladeMode(ItemStack stack, boolean isBladeMode) {
        stack.getOrCreateTag().putBoolean(MODE_TAG, isBladeMode);
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

    private void shootEnergyBeam(Player player, Level level) {
        Vec3 from = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0F);
        final double RANGE = 50.0D, STEP = 0.25D;
        final float GUN_DAMAGE = 10.0F;

        boolean hit = false;
        for (double d = 0; d < RANGE && !hit; d += STEP) {
            Vec3 pos = from.add(dir.scale(d));

            if (level.isClientSide) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
            } else {
                ((ServerLevel) level).sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }

            if (!level.isClientSide) {
                AABB bb = new AABB(pos, pos).inflate(0.2D);
                for (LivingEntity tgt : level.getEntitiesOfClass(LivingEntity.class, bb,
                        e -> !e.is(player) && !e.isInvulnerable())) {
                    tgt.hurt(player.damageSources().playerAttack(player), GUN_DAMAGE);
                    hit = true;
                    break;
                }
            }
        }

        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.5F);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主要的idle状态控制器
        controllers.add(new AnimationController<>(this, "idle_controller", 5, this::handleIdleAnimation));

        // 单独的触发式动画控制器 - 必须为每个可触发的动画设置单独的控制器
        controllers.add(new AnimationController<>(this, "blade_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("blade", BLADE_ANIM));

        controllers.add(new AnimationController<>(this, "gun_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("gun", GUN_ANIM));
    }

    // 处理idle动画状态
    private PlayState handleIdleAnimation(AnimationState<TwoWeaponItem> state) {
        state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    // 强制触发切换动画
    private void playSwitchAnimation(Player player, ItemStack stack, Level level, boolean toBladeMode) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (toBladeMode) {
                // 播放刀切换动画 - 使用正确的控制器名称
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "blade_controller", "blade");
                level.playSound(null, player.blockPosition(),
                        SoundEvents.PISTON_CONTRACT, SoundSource.PLAYERS, 0.8F, 1.0F);
            } else {
                // 播放枪切换动画 - 使用正确的控制器名称
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "gun_controller", "gun");
                level.playSound(null, player.blockPosition(),
                        SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.8F, 1.0F);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        /* -------- Shift + 右键：切换模式并播放动画 -------- */
        if (player.isShiftKeyDown() && !player.isUsingItem()) {
            if (!level.isClientSide) {
                boolean currentMode = isBladeMode(stack);
                boolean newMode = !currentMode; // 切换模式

                setBladeMode(stack, newMode);

                // 播放切换动画
                playSwitchAnimation(player, stack, level, newMode);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        /* -------- 正常右键：按当前形态攻击 -------- */
        if (!level.isClientSide) {
            if (isBladeMode(stack)) {
                doSlashAttack(player, level);
            } else {
                shootEnergyBeam(player, level);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    private void doSlashAttack(Player player, Level level) {
        player.sweepAttack();

        AABB box = player.getBoundingBox()
                .inflate(1.0D, 0.25D, 1.0D)
                .move(player.getViewVector(1.0F).scale(0.5D));

        float sweepDamage = 7.0F;
        float sweepKnockback = 0.4F;

        for (LivingEntity tgt : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && !player.isAlliedTo(e))) {
            if (tgt.invulnerableTime <= 0) {
                tgt.hurt(player.damageSources().playerAttack(player), sweepDamage);
                tgt.knockback(sweepKnockback,
                        player.getX() - tgt.getX(),
                        player.getZ() - tgt.getZ());
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}