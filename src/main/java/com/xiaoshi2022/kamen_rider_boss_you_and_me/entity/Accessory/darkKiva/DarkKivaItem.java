package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.armor.DarkKivaArmorRenderer;
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

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{4, 8, 7, 4}[type.getSlot().getIndex()]; // 总防御23
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

            @Override
            public float getToughness() {
                return 4.0f;
            }

            @Override
            public float getKnockbackResistance() {
                return 0.3f;
            }
        }, type, properties);
    }

    @Override
    public void tick(Player player) {
        ServerPlayer serverPlayer = (ServerPlayer) player;

        // 冷却更新
        if (bloodStealCooldown > 0) bloodStealCooldown--;

        // 全套效果：夜视+生命偷取
        if (isFullArmorEquipped(serverPlayer)) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
            }

            // 每5秒触发一次吸血
            if (bloodStealCooldown == 0 && player.level().getGameTime() % 100 == 0) {
                player.heal(2.0f);
                bloodStealCooldown = 100;
                spawnBatParticles(player);
            }
        }
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
        state.getController().setAnimation(RawAnimation.begin().thenLoop(ANIMATION_IDLE));
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