package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.globalism.GlobalismRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
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

public class Globalism extends SwordItem implements GeoItem {
    private static final RawAnimation idle = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Globalism(Properties properties) {
        super(new Tier() {
            @Override
            public int getUses() {
                return 350; // 武器的耐久度
            }

            @Override
            public float getSpeed() {
                return 2.5f; // 武器的攻击速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 10.4f; // 武器的额外攻击伤害
            }

            @Override
            public int getLevel() {
                return 3; // 武器的等级
            }

            @Override
            public int getEnchantmentValue() {
                return 1; // 武器的附魔价值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(); // 修复材料
            }
        }, 3, -2.2f, new Item.Properties());

        // 注册为服务器端处理的物品，启用动画数据同步和服务器端动画触发
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }


    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GlobalismRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GlobalismRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 如果目标是异域者，追加 30 % 伤害
        if (target instanceof ElementaryInvesHelheim) {
            float bonus = 0.30f;               // 30 % 加成，可改成 1.0f = 100 %
            float extra = target.getMaxHealth() * bonus; // 或直接用固定值
            target.hurt(target.damageSources().mobAttack(attacker), extra);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }


    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, this::idleAnimController));
    }

    private PlayState idleAnimController(AnimationState<Globalism> GlobalismAnimationState) {
        GlobalismAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

