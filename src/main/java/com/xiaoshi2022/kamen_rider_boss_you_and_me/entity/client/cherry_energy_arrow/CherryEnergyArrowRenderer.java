package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.cherry_energy_arrow;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.CherryEnergyArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 樱桃能量箭矢实体的渲染器
 * 负责渲染樱桃能量箭矢实体
 */
public class CherryEnergyArrowRenderer extends GeoEntityRenderer<CherryEnergyArrowEntity> {

    public CherryEnergyArrowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CherryEnergyArrowModel());
        this.shadowRadius = 0.1F; // 设置极小的阴影，符合能量体的特性
    }
}