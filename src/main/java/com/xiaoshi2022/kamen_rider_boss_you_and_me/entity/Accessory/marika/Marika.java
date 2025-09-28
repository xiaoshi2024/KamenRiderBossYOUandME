package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.marikas.MarikaArmorRenderer;
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
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class Marika extends ArmorItem implements GeoItem , KamenBossArmor, ArmorAnimationFactory.AnimatableAccessor  {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public Marika(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久：比钻石套略高
            @Override
            public int getDurabilityForType(Type type) {
                return new int[]{500, 580, 620, 420}[type.getSlot().getIndex()];
            }
            // 防御：提升至21 点（头盔4 胸7 腿7 靴3） - 假面骑士Marika强化版
            @Override
            public int getDefenseForType(Type type) {
                return new int[]{4, 7, 7, 3}[type.getSlot().getIndex()];
            }
            // 盔甲韧性：提高至3.5
            @Override
            public float getToughness() {
                return 3.5f;
            }
            // 击退抗性：增加10%
            @Override
            public float getKnockbackResistance() {
                return 0.10f;
            }
            // 高附魔能力
            @Override
            public int getEnchantmentValue() {
                return 28;
            }
            // 装备音效：使用铁盔甲
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_IRON;
            }
            // 修复材料：金锭
            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.GOLD_INGOT);
            }
            // 材质名
            @Override
            public String getName() {
                return "marika";
            }

        }, type, properties);
    }

    /* ---------- GeckoLib 渲染 ---------- */

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity,
                                                          ItemStack stack,
                                                          EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (this.renderer == null) this.renderer = new MarikaArmorRenderer();
                this.renderer.prepForRender(livingEntity, stack, slot, original);
                return this.renderer;
            }
        });
    }

    /* ---------- 动画 ---------- */

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        // 主控制器：播放 idle
        data.add(new AnimationController<>(this, "main_controller", 5, this::mainPredicate));
    }

    private <E extends GeoItem> PlayState mainPredicate(AnimationState<E> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /* ---------- 工具方法 ---------- */

    public static boolean isArmorEquipped(ServerPlayer player, Item armorItem) {
        return player.getInventory().armor.stream()
                .anyMatch(s -> s.getItem() == armorItem);
    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }

    @Override
    public void tick(Player player) {
        // 添加抗性效果
        this.applyResistanceEffect(player);
    }

    // 不再提供力量效果，避免与原版药水冲突
    @Override
    public int getStrengthLevel() {
        return 0; // 不使用力量效果
    }

    // 覆写getResistanceLevel方法，设置自定义抗性等级
    @Override
    public int getResistanceLevel() {
        return 3; //使用抗性2效果
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
