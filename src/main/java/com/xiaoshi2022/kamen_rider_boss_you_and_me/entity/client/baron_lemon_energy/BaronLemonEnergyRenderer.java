package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_lemon_energy;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BaronLemonEnergyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 巴隆柠檬能量实体的渲染器
 * 负责渲染柠檬能量特效实体
 */
public class BaronLemonEnergyRenderer extends GeoEntityRenderer<BaronLemonEnergyEntity> {

    public BaronLemonEnergyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BaronLemonEnergyModel());
        this.shadowRadius = 0.1F; // 设置极小的阴影，符合能量体的特性
    }
}