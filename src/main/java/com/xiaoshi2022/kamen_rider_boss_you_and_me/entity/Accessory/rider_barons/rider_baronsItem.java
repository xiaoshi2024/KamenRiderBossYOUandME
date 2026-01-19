package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.Riderbarons.RiderbaronsArmorRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.init.ArmorAnimationFactory;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class rider_baronsItem extends ArmorItem implements GeoItem , KamenBossArmor , ArmorAnimationFactory.AnimatableAccessor {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public rider_baronsItem(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久度 (高于下界合金套)
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{528, 600, 640, 448}[type.getSlot().getIndex()]; // 约等于下界合金套的1.1倍
            }


            // 防御：提升至24 点（头盔4 胸甲9 护腿8 靴子3） - 假面骑士Baron强化版
            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{4, 9, 8, 3}[type.getSlot().getIndex()];
            }
            // 盔甲韧性：添加韧性值4.0
            @Override
            public float getToughness() {
                return 4.0f;
            }
            // 击退抗性：增加15%
            @Override
            public float getKnockbackResistance() {
                return 0.15f;
            }


            // 附魔能力（中等）
            @Override
            public int getEnchantmentValue() {
                return 15; // 介于钻石和金之间
            }

            // 装备音效（使用金属重甲音效）
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            // 修复材料（金锭+苹果象征香蕉）
            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.GOLD_INGOT, Items.APPLE);
            }

            // 材质名称 - 使用内置的netherite材质，避免Minecraft尝试加载不存在的纹理
            @Override
            public String getName() {
                return "netherite";
            }

        }, type, properties);
    }

    public static boolean isArmorEquipped(ServerPlayer player, Item armorItem) {
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (armorStack.getItem() == armorItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new RiderbaronsArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    public void triggerHenshin(Player player, String animationName) {
        if (player.level().isClientSide) {
            AnimatableManager<?> manager = this.getAnimatableInstanceCache().getManagerForId(player.getId());
            if (manager != null) {
                // 触发指定动画
                manager.tryTriggerAnimation("controller", animationName);
            }
        }
    }

    private PlayState predicate(AnimationState event) {
        if (this.animationprocedure.equals("empty")) {
            // 设置动画名称为 "henshin"
            this.animationprocedure = "henshin";
            // 触发动画
            event.getController().setAnimation(RawAnimation.begin().thenLoop("henshin"));
            return PlayState.CONTINUE;
        } else if (this.animationprocedure.equals("henshin")) {
            // 如果动画名称为 "henshin"，表示动画正在播放
            // 等待动画播放完成
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState event) {
        if (!this.animationprocedure.equals("empty")) {
            // 如果动画名称不为空，表示需要播放动画
            event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
            // 动画播放完成后重置动画名称
            this.animationprocedure = "empty";
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController(this, "controller", 5, this::predicate));
        data.add(new AnimationController(this, "procedureController", 5, this::procedurePredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }

    @Override
    public void tick(Player player) {
        // 添加抗性效果
        this.applyResistanceEffect(player);
        // 添加力量效果
        this.applyStrengthEffect(player);
    }

    // 不再提供力量效果，避免与原版药水冲突
    @Override
    public int getStrengthLevel() {
        return 2; // 不使用力量效果
    }

    // 覆写getResistanceLevel方法，设置自定义抗性等级
    @Override
    public int getResistanceLevel() {
        return 1; //级别
    }

    // 重写applyResistanceEffect方法，确保不会移除玩家已有的抗性效果
    @Override
    public void applyResistanceEffect(Player player) {
        if (!player.level().isClientSide()) {
            int resistanceLevel = this.getResistanceLevel();
            if (resistanceLevel > 0) {
                int targetAmp = resistanceLevel - 1;
                MobEffectInstance existing = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
                
                // 只有在玩家没有抗性效果，或者现有效果等级低于我们提供的等级时，才添加新效果
                if (existing == null || existing.getAmplifier() < targetAmp) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            400,
                            targetAmp,
                            false,
                            false // 不显示粒子效果，避免视觉混乱
                    ));
                }
            }
        }
    }
}