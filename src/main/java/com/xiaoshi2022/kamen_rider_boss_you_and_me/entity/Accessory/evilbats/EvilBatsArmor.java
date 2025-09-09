package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.armor.EvilBatsArmorRenderer;
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

import java.util.Random;
import java.util.function.Consumer;

public class EvilBatsArmor extends ArmorItem implements GeoItem, KamenBossArmor, ArmorAnimationFactory.AnimatableAccessor {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";
    private static final Random RANDOM = new Random();

    public EvilBatsArmor(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{528, 600, 640, 448}[type.getSlot().getIndex()];
            }

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{9, 7, 9, 3}[type.getSlot().getIndex()]; // 总防御28
            }

            @Override
            public int getEnchantmentValue() {
                return 20;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.NETHERITE_INGOT, Items.EMERALD);
            }

            @Override
            public String getName() {
                return "evilbats";
            }

            @Override
            public float getToughness() {
                return 3.5f;
            }

            @Override
            public float getKnockbackResistance() {
                return 0.2f;
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

    // 检查全套EvilBatsArmor盔甲是否装备
    public static boolean isFullArmorEquipped(ServerPlayer player) {
        return player.getInventory().armor.get(3).getItem() instanceof EvilBatsArmor &&
               player.getInventory().armor.get(2).getItem() instanceof EvilBatsArmor &&
               player.getInventory().armor.get(1).getItem() instanceof EvilBatsArmor;
    }

    // 更新能力状态的tick方法
    public void tick(Player player) {
        // 全套盔甲提供生命恢复效果
        if (isFullArmorEquipped((ServerPlayer) player) && player.level().getGameTime() % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0));
        }
    }

    public void triggeridle(Player player, String animationName) {
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
            // 设置动画名称为 "idle"
            this.animationprocedure = "idle";
            // 触发动画
            event.getController().setAnimation(RawAnimation.begin().thenPlay("idle"));
            return PlayState.CONTINUE;
        } else if (this.animationprocedure.equals("idle")) {
            // 如果动画名称为 "idle"，表示动画正在播放
            // 等待动画播放完成
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new EvilBatsArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }
}