package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_banana_energy;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BaronBananaEnergyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BaronBananaEnergyRenderer extends GeoEntityRenderer<BaronBananaEnergyEntity> {

    public BaronBananaEnergyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BaronBananaEnergyModel());
        this.shadowRadius = 0.1F; // 设置极小的阴影，符合能量体的特性
    }
}