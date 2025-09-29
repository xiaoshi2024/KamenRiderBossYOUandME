package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon;

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
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import net.minecraft.world.item.Item.Properties;

import java.util.function.Consumer;

public class TwoWeaponSwordItem extends TwoWeaponItem {



    // 参考假面骑士艾比尔的武器性能调整数值
    private static final Tier CUSTOM_TIER = new Tier() {
        public int getUses()            { return 2500; } // 耐久度大幅提升
        public float getSpeed()         { return 8.0F; } // 攻击速度更快
        public float getAttackDamageBonus() { return 8.0F; } // 基础伤害提升
        public int getLevel()           { return 3; } // 等级提升
        public int getEnchantmentValue(){ return 20; } // 附魔能力更强
        public Ingredient getRepairIngredient() { return Ingredient.of(); }
    };
    
    // 用于物品注册的静态引用
    public static TwoWeaponSwordItem ITEM = null; // 会在ModItems中被赋值

    // 武器伤害数值
    private static final float SWORD_MODE_DAMAGE = 26.0F; // 剑模式伤害
    private static final float SWEEP_DAMAGE = 10.0F; // 横扫伤害
    private static final float SWEEP_KNOCKBACK = 1.2F; // 击退效果增强

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlayAndHold("blade");

    public TwoWeaponSwordItem(Properties p) {
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
    private PlayState handleIdleAnimation(AnimationState<TwoWeaponSwordItem> state) {
        state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主要的idle状态控制器，始终运行
        controllers.add(new AnimationController<>(this, "idle_controller", 5, this::handleIdleAnimation));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 在bat变种情况下进行检测和处理
        Variant currentVariant = getVariant(stack);

        /* -------- Shift + 右键：切换到枪模式 -------- */
        if (player.isShiftKeyDown() && !player.isUsingItem()) {
            if (!level.isClientSide) {
                // 创建新的枪形态武器
                ItemStack gunStack = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.TWO_WEAPON_GUN.get(), 1);
                
                // 复制耐久度等重要属性
                if (stack.isDamageableItem()) {
                    gunStack.setDamageValue(stack.getDamageValue());
                }
                
                // 复制NBT数据
                if (stack.hasTag()) {
                    CompoundTag tag = stack.getTag().copy();
                    gunStack.setTag(tag);
                }
                
                // 如果原武器是bat变种，确保新武器也是bat变种
                if (currentVariant == Variant.BAT) {
                    setVariant(gunStack, Variant.BAT);
                }
                
                // 替换物品
                player.setItemInHand(hand, gunStack);
                
                // 播放切换音效
                level.playSound(null, player.blockPosition(),
                        SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 1.0F, 1.1F);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 0.7F, 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        /* -------- 正常右键：剑模式攻击 -------- */
        if (!level.isClientSide) {
            doSlashAttack(player, level);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    private void doSlashAttack(Player player, Level level) {
        player.sweepAttack();

        // 增大攻击范围
        AABB box = player.getBoundingBox()
                .inflate(2.0D, 1.0D, 2.0D) // 范围增大
                .move(player.getViewVector(1.0F).scale(1.0D)); // 攻击距离增加

        for (LivingEntity tgt : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && !player.isAlliedTo(e))) {
            if (tgt.invulnerableTime <= 0) {
                // 造成更高伤害
                tgt.hurt(player.damageSources().playerAttack(player), SWORD_MODE_DAMAGE);
                // 增强击退效果
                tgt.knockback(SWEEP_KNOCKBACK,
                        player.getX() - tgt.getX(),
                        player.getZ() - tgt.getZ());

                // 对击中的实体造成额外效果
                if (tgt.getHealth() <= SWEEP_DAMAGE) {
                    // 如果目标生命值较低，直接消灭
                    tgt.hurt(player.damageSources().playerAttack(player), tgt.getHealth());
                }
            }
        }

        // 增强音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 0.9F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.1F);
    }


}