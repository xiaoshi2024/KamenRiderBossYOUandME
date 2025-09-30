package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.melon_energy_slash;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.MelonEnergySlashEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 蜜瓜能量斩击实体的渲染器
 * 负责渲染蜜瓜能量斩击实体
 */
public class MelonEnergySlashRenderer extends GeoEntityRenderer<MelonEnergySlashEntity> {

    public MelonEnergySlashRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MelonEnergySlashModel());
        this.shadowRadius = 0.1F; // 设置极小的阴影，符合能量体的特性
    }
}