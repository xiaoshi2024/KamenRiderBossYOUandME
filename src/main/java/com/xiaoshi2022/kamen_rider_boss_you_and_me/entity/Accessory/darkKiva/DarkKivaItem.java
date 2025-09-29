package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.armor.DarkKivaArmorRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.init.ArmorAnimationFactory;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
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

public class DarkKivaItem extends ArmorItem implements GeoItem, KamenBossArmor, ArmorAnimationFactory.AnimatableAccessor {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final String ANIMATION_IDLE = "idle";
    private boolean batWingsActive = false;
    private int bloodStealCooldown = 0;
    private String animationprocedure = "idle";

    public DarkKivaItem(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{666, 720, 750, 555}[type.getSlot().getIndex()];
            }

            // 防御：提升至30
            @Override
            public int getDefenseForType(Type type) {
                return new int[]{6, 10, 5, 9}[type.getSlot().getIndex()];
            }

            @Override
            public int getEnchantmentValue() {
                return 25;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.NETHERITE_INGOT, Items.REDSTONE_BLOCK);
            }

            @Override
            public String getName() {
                return "dark_kiva";
            }

            // 盔甲韧性：提高至5.0
            @Override
            public float getToughness() {
                return 5.0f;
            }
            // 击退抗性：增加20%
            @Override
            public float getKnockbackResistance() {
                return 0.20f;
            }

        }, type, properties);
    }

    @Override
    public void tick(Player player) {
        // 移除了所有基础buff处理逻辑，这些逻辑现在在DarkKivaAbilityHandler中处理
        // 添加抗性效果
        this.applyResistanceEffect(player);

        // 添加力量效果
        this.applyStrengthEffect(player);
    }
    
    // 不再提供力量效果，避免与原版药水冲突
    @Override
    public int getStrengthLevel() {
        return 3; // 不使用力量效果
    }
    
    // 覆写getResistanceLevel方法，为DarkKiva设置自定义抗性等级
    @Override
    public int getResistanceLevel() {
        return 4; // DarkKiva抗性等级2
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
    
    // 添加客户端专用的黑暗Kiva盔甲检测方法
    private static boolean isDarkKivaArmorEquipped(LocalPlayer player) {
        // 检查是否穿着黑暗Kiva头盔、胸甲和护腿（不需要鞋子）
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DarkKivaItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DarkKivaItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DarkKivaItem;
    }
    
    private void spawnBatParticles(Player player) {
        // 可扩展粒子：蝙蝠群飞散效果
    }
    
    public static boolean isFullArmorEquipped(ServerPlayer player) {
        return player.getInventory().armor.get(3).getItem() instanceof DarkKivaItem &&
                player.getInventory().armor.get(2).getItem() instanceof DarkKivaItem &&
                player.getInventory().armor.get(1).getItem() instanceof DarkKivaItem;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (renderer == null) renderer = new DarkKivaArmorRenderer();
                renderer.prepForRender(entity, stack, slot, original);
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private <T extends GeoItem> PlayState predicate(AnimationState<T> state) {
        state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold(ANIMATION_IDLE));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }


}