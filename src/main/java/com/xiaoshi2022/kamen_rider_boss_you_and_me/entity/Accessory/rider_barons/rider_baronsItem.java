package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.Riderbarons.RiderbaronsArmorRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animatable.GeoItem;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.client.model.HumanoidModel;

import java.util.function.Consumer;

public class rider_baronsItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public rider_baronsItem(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久度 (高于下界合金套)
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{528, 600, 640, 448}[type.getSlot().getIndex()]; // 约等于下界合金套的1.1倍
            }


            // 调整后的香蕉防御值（仍保持重甲定位但不过于OP）
            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{3, 7, 6, 3}[type.getSlot().getIndex()]; // 总防御19
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

            // 材质名称
            @Override
            public String getName() {
                return "baron_banana";
            }

            // 高韧性（高于下界合金）
            @Override
            public float getToughness() {
                return 4.0f; // 下界合金3.0
            }

            // 高击退抗性（香蕉形态稳定性）
            @Override
            public float getKnockbackResistance() {
                return 0.25f; // 25%击退抗性
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
}