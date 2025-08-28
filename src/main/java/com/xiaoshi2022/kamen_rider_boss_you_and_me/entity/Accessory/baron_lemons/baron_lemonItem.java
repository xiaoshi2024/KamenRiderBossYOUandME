package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.BronLemons.baronLemonArmorRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.init.ArmorAnimationFactory;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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

public class baron_lemonItem extends ArmorItem implements GeoItem , KamenBossArmor, ArmorAnimationFactory.AnimatableAccessor {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public baron_lemonItem(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久度 (参考下界合金套，但略低)
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{481, 555, 592, 407}[type.getSlot().getIndex()]; // 约等于钻石套的1.2倍
            }

            // 防御值 (平衡为轻甲类型)
            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{3, 6, 5, 3}[type.getSlot().getIndex()]; // 总防御点数：17（钻石套20）
            }

            // 附魔能力（高于钻石）
            @Override
            public int getEnchantmentValue() {
                return 25; // 类似金质装备的高附魔能力
            }

            // 装备音效（使用鞘翅音效模拟变身音）
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_ELYTRA;
            }

            // 修复材料（柠檬）
            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.GOLDEN_APPLE); // 用金苹果象征柠檬能量
            }

            // 材质名称
            @Override
            public String getName() {
                return "baron_lemon";
            }

            // 韧性（中等，低于下界合金）
            @Override
            public float getToughness() {
                return 2.5f; // 钻石2.0，下界合金3.0
            }

            // 击退抗性（部分抗性）
            @Override
            public float getKnockbackResistance() {
                return 0.1f; // 10%击退抗性
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
                    this.renderer = new baronLemonArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
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
            event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        } else if (this.animationprocedure.equals("idle")) {
            // 如果动画名称为 "idle"，表示动画正在播放
            // 等待动画播放完成
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<baron_lemonItem> event) {
        if (!this.animationprocedure.equals("empty") &&
                !this.animationprocedure.equals("idle")) {
            // 只处理非idle的特殊动画
            event.getController().setAnimation(
                    RawAnimation.begin().thenPlay(this.animationprocedure)
            );

            // 不立即重置，等待动画自然结束
            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                this.animationprocedure = "empty";
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        // 只保留一个主控制器（优先级5）和一个披风控制器（优先级0，最高）
        data.add(new AnimationController<>(this, "main_controller", 5, this::mainPredicate));
        data.add(new AnimationController<>(this, "cloak_controller", 0, this::cloakPredicate));
    }

    private <E extends GeoItem> PlayState mainPredicate(AnimationState<E> event) {
        // 只处理基础待机动画
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    private <E extends GeoItem> PlayState cloakPredicate(AnimationState<E> event) {
        // 强制覆盖主控制器，始终播放披风动画
        event.getController().setAnimation(RawAnimation.begin().thenLoop("cloak"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void tick(Player player) {

    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }
}
