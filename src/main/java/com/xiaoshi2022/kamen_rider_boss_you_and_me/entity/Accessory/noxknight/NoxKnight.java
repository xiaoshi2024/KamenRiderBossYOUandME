package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.noxknight.NoxKnightArmorRenderer;
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

public class NoxKnight extends ArmorItem implements GeoItem, KamenBossArmor, ArmorAnimationFactory.AnimatableAccessor {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public NoxKnight(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久度
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{528, 600, 640, 448}[type.getSlot().getIndex()];
            }

            // 防御值
            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{5, 9, 8, 7}[type.getSlot().getIndex()];
            }

            // 附魔能力
            @Override
            public int getEnchantmentValue() {
                return 20;
            }

            // 装备音效
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            // 修复材料
            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.EMERALD, Items.GOLD_INGOT);
            }

            // 材质名称
            @Override
            public String getName() {
                return "nox_knight";
            }

            // 韧性
            @Override
            public float getToughness() {
                return 3.5f;
            }

            // 击退抗性
            @Override
            public float getKnockbackResistance() {
                return 0.2f;
            }
        }, type, properties);
    }

    @Override
    public void tick(Player player) {
        // 添加夜视效果
        this.applyNightVisionEffect(player);
        
        // 添加生命恢复效果
        this.applyRegenerationEffect(player);
    }
    
    // 覆写getResistanceLevel方法，设置自定义抗性等级
    @Override
    public int getResistanceLevel() {
        return 1;
    }
    
    // 覆写getStrengthLevel方法，设置自定义力量等级
    @Override
    public int getStrengthLevel() {
        return 1;
    }
    
    // 添加夜视效果
    private void applyNightVisionEffect(Player player) {
        if (!player.level().isClientSide()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    800,
                    0,
                    false,
                    false
            ));
        }
    }
    
    // 添加生命恢复效果
    private void applyRegenerationEffect(Player player) {
        if (!player.level().isClientSide()) {
            if (player.getHealth() < player.getMaxHealth() * 0.8) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        200,
                        0,
                        false,
                        false
                ));
            }
        }
    }

    // 检查单个盔甲是否装备
    public static boolean isArmorEquipped(ServerPlayer player, Item armorItem) {
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (armorStack.getItem() == armorItem) {
                return true;
            }
        }
        return false;
    }

    // 检查全套Nox Knight盔甲是否装备
    public static boolean isFullArmorEquipped(ServerPlayer player) {
        return player.getInventory().armor.get(3).getItem() instanceof NoxKnight &&
               player.getInventory().armor.get(2).getItem() instanceof NoxKnight &&
               player.getInventory().armor.get(1).getItem() instanceof NoxKnight;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new NoxKnightArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    public void triggeridle(Player player, String animationName) {
        if (player.level().isClientSide) {
            AnimatableManager<?> manager = this.getAnimatableInstanceCache().getManagerForId(player.getId());
            if (manager != null) {
                manager.tryTriggerAnimation("controller", animationName);
            }
        }
    }

    private PlayState predicate(AnimationState event) {
        if (this.animationprocedure.equals("empty")) {
            this.animationprocedure = "idle";
            event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        } else if (this.animationprocedure.equals("idle")) {
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<NoxKnight> event) {
        if (!this.animationprocedure.equals("empty") &&
                !this.animationprocedure.equals("idle")) {
            event.getController().setAnimation(
                    RawAnimation.begin().thenPlay(this.animationprocedure)
            );

            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                this.animationprocedure = "empty";
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "main_controller", 5, this::mainPredicate));
    }

    private <E extends GeoItem> PlayState mainPredicate(AnimationState<E> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }
}