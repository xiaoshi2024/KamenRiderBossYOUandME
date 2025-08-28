package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShin.ZangetsuShinArmorRenderer;
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
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class ZangetsuShinItem extends ArmorItem implements GeoItem , KamenBossArmor , ArmorAnimationFactory.AnimatableAccessor {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public ZangetsuShinItem(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久：比钻石套略高
            @Override
            public int getDurabilityForType(Type type) {
                return new int[]{520, 600, 640, 440}[type.getSlot().getIndex()];
            }
            // 防御：17 点（头盔3 胸6 腿5 靴3）
            @Override
            public int getDefenseForType(Type type) {
                return new int[]{3, 6, 5, 3}[type.getSlot().getIndex()];
            }
            // 高附魔能力
            @Override
            public int getEnchantmentValue() {
                return 30;
            }
            // 装备音效：使用鞘翅
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_ELYTRA;
            }
            // 修复材料：紫颂果
            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.CHORUS_FRUIT);
            }
            // 材质名
            @Override
            public String getName() {
                return "zangetsu_shin";
            }
            // 韧性：3.5（高于钻石 2.0，略低于下界合金 3.0）
            @Override
            public float getToughness() {
                return 3.5f;
            }
            // 击退抗性：20%
            @Override
            public float getKnockbackResistance() {
                return 0.2f;
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
                if (this.renderer == null) this.renderer = new ZangetsuShinArmorRenderer();
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

    }
}