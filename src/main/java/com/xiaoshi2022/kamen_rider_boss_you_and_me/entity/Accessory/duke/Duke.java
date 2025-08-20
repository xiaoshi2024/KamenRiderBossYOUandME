package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.dukes.DukeArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.Random;

public class Duke extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty"; 
    private static final Random RANDOM = new Random();
    private int energySwordCooldown = 0; // 能量剑冷却时间
    private int energyShieldDuration = 0; // 能量护盾持续时间
    private int techMasteryTicks = 0; // 技术精通效果计时

    public Duke(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            // 耐久度 (参考下界合金套)
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{528, 600, 640, 448}[type.getSlot().getIndex()]; // 约等于下界合金套的1.1倍
            }

            // 防御值
            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{3, 7, 6, 3}[type.getSlot().getIndex()]; // 总防御19
            }

            // 附魔能力
            @Override
            public int getEnchantmentValue() {
                return 20; // 介于钻石和金之间
            }

            // 装备音效
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            // 修复材料
            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.EMERALD, Items.GOLD_INGOT); // 钻石和金锭象征公爵的高贵
            }

            // 材质名称
            @Override
            public String getName() {
                return "duke";
            }

            // 韧性
            @Override
            public float getToughness() {
                return 3.5f; // 高于下界合金
            }

            // 击退抗性
            @Override
            public float getKnockbackResistance() {
                return 0.2f; // 20%击退抗性
            }
        }, type, properties);
    }

    // 技术精通 - 战极凌马作为天才科学家的能力
    public void techMastery(Player player) {
        if (techMasteryTicks <= 0) {
            // 每30秒触发一次技术精通效果
            techMasteryTicks = 600; // 30秒 = 600 ticks
            
            // 随机获得一种增益效果
            int effectType = RANDOM.nextInt(4);
            switch (effectType) {
                case 0:
                    // 速度提升
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));
                    break;
                case 1:
                    // 挖掘速度提升
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
                    break;
                case 2:
                    // 攻击力提升
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                    break;
                case 3:
                    // 抗性提升
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                    break;
            }
        } else {
            techMasteryTicks--;
        }
    }

    // 能量剑攻击 - 从手部发射能量剑
    public void energySwordAttack(Player player) {
        if (energySwordCooldown <= 0) {
            energySwordCooldown = 40; // 2秒冷却
            
            // 播放攻击音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, player.getSoundSource(), 1.0F, 1.0F);
            
            // 获取玩家面向方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(lookVec.x * 10, lookVec.y * 10, lookVec.z * 10);
            
            // 检测直线上的实体
            List<Entity> entities = player.level().getEntities(player,
                    new AABB(start, end).inflate(1.0D),
                    entity -> entity instanceof LivingEntity && entity != player);
            
            // 对命中的实体造成伤害
            for (Entity entity : entities) {
                ((LivingEntity) entity).hurt(player.damageSources().playerAttack(player), 8.0F);
                // 有几率附加凋零效果
                if (RANDOM.nextFloat() < 0.3F) {
                    ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
            }
        } else {
            energySwordCooldown--;
        }
    }

    // 激活能量护盾
    public void activateEnergyShield(Player player) {
        if (energyShieldDuration <= 0) {
            energyShieldDuration = 100; // 5秒持续时间
            
            // 播放护盾激活音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 1.0F, 1.0F);
        } else {
            energyShieldDuration--;
        }
    }

    // 检查能量护盾是否激活
    public boolean isEnergyShieldActive() {
        return energyShieldDuration > 0;
    }

    // 护盾伤害减免
    public float getShieldDamageReduction() {
        return isEnergyShieldActive() ? 0.5F : 0.0F; // 50%伤害减免
    }

    // 更新能力状态的tick方法
    public void tick(Player player) {
        // 更新技术精通状态
        techMastery(player);
        
        // 更新能量剑冷却
        if (energySwordCooldown > 0) {
            energySwordCooldown--;
        }
        
        // 更新能量护盾持续时间
        if (energyShieldDuration > 0) {
            energyShieldDuration--;
            // 护盾激活时提供伤害减免视觉反馈
            if (energyShieldDuration % 5 == 0) {
                spawnShieldParticles(player);
            }
        }
    }

    // 生成护盾粒子效果
    private void spawnShieldParticles(Player player) {
        Level level = player.level();
        if (level.isClientSide) {
            for (int i = 0; i < 5; i++) {
                double x = player.getX() + (RANDOM.nextDouble() - 0.5) * player.getBbWidth() * 2;
                double y = player.getY() + RANDOM.nextDouble() * player.getBbHeight();
                double z = player.getZ() + (RANDOM.nextDouble() - 0.5) * player.getBbWidth() * 2;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT, x, y, z, 0, 0, 0);
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

    // 检查全套Duke盔甲是否装备
    public static boolean isFullArmorEquipped(ServerPlayer player) {
        return player.getInventory().armor.get(3).getItem() instanceof Duke &&
               player.getInventory().armor.get(2).getItem() instanceof Duke &&
               player.getInventory().armor.get(1).getItem() instanceof Duke;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new DukeArmorRenderer();
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

    private PlayState procedurePredicate(AnimationState<Duke> event) {
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
    }

    private <E extends GeoItem> PlayState mainPredicate(AnimationState<E> event) {
        // 只处理基础待机动画
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
