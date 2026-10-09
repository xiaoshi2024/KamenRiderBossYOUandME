package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrants.TyrantArmorRenderer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class Tyrant extends ArmorItem implements GeoItem {

    /** 变身动画序列：sodasx(初始化) → soda(聚合) → idle(待机循环) */
    private static final RawAnimation HENSHIN_SEQ = RawAnimation.begin()
            .thenPlay("sodasx")
            .then("soda", Animation.LoopType.PLAY_ONCE)
            .thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Tyrant(Holder<ArmorMaterial> armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
        }
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final TyrantArmorRenderer renderer = new TyrantArmorRenderer();

            @Override
            public <T extends LivingEntity> net.minecraft.client.model.HumanoidModel<?> getGeoArmorRenderer(
                    T livingEntity, ItemStack itemStack,
                    net.minecraft.world.entity.EquipmentSlot equipmentSlot,
                    net.minecraft.client.model.HumanoidModel<T> original) {
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 默认动画：sodasx → soda → idle，盔甲一穿上就自动播放
        controllers.add(new AnimationController<>(this, "controller", 0,
                state -> {
                    state.setAnimation(HENSHIN_SEQ);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}