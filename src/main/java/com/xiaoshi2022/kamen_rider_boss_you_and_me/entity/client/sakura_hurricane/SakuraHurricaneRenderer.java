package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.sakura_hurricane;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EntitySakuraHurricane;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SakuraHurricaneRenderer extends GeoEntityRenderer<EntitySakuraHurricane> {

    public SakuraHurricaneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SakuraHurricaneModel());
        this.shadowRadius = 0.5F; // 设置摩托车的阴影大小
    }
}