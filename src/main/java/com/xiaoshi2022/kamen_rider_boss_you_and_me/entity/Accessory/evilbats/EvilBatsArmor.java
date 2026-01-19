package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.armor.EvilBatsArmorRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.init.ArmorAnimationFactory;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
                return "netherite"; // 使用内置的netherite材质，避免Minecraft尝试加载不存在的纹理
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

    @Override
    public void tick(Player player) {
        // 添加抗性效果，但使用更安全的方式避免与原版药水冲突
        this.applyResistanceEffect(player);
        // 添加力量效果
        this.applyStrengthEffect(player);
        
        // 检查并更新隐密状态
        this.updateStealthStatus(player);
    }

    // 更新隐密状态
    private void updateStealthStatus(Player player) {
        if (player.level().isClientSide()) return;
        
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        boolean isFullArmor = isFullArmorEquipped((ServerPlayer) player);
        
        // 当装备全套盔甲时设置隐密状态为true
        if (isFullArmor && !variables.isEvilBatsStealthed) {
            variables.isEvilBatsStealthed = true;
            variables.syncPlayerVariables(player);
        }
        
        // 当不再装备全套盔甲时清除隐密状态
        if (!isFullArmor && variables.isEvilBatsStealthed) {
            variables.isEvilBatsStealthed = false;
            variables.syncPlayerVariables(player);
        }
    }
    
    // 不再提供力量效果，避免与原版药水冲突
    @Override
    public int getStrengthLevel() {
        return 2; // 使用力量效果
    }
    
    // 提供额外的移动速度加成
    @Override
    public double getSpeedBonus() {
        return 0.2; // 20% 额外移动速度
    }
    
    // 提供静音效果加成
    @Override
    public float getSoundReduction() {
        return 0.8f; // 80% 声音降低
    }

    // 覆写getResistanceLevel方法，设置自定义抗性等级
    @Override
    public int getResistanceLevel() {
        return 2; //使用抗性2效果，比基础高一级
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
    
    // 新增：检查并触发解除变身
    private void checkAndTriggerRelease(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 获取玩家变量
            KRBVariables.PlayerVariables variables = serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            
            // 检查玩家是否装备了全套EvilBatsArmor，但EvilBats变身状态为false
            if (isFullArmorEquipped(serverPlayer) && !variables.isEvilBatsTransformed) {
                // 自动解除变身，卸除盔甲
                releaseArmor(serverPlayer);
            }
        }
    }
    
    // 新增：独立的解除变身方法
    public static void releaseArmor(ServerPlayer player) {
        // 清除盔甲栏的所有EvilBatsArmor
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (armorStack.getItem() instanceof EvilBatsArmor) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }

        // 更新玩家变量，将EvilBats变身状态设置为false
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        variables.isEvilBatsTransformed = false;
        variables.syncPlayerVariables(player);

        // 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 同步玩家物品栏变化
        player.inventoryMenu.broadcastChanges();
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