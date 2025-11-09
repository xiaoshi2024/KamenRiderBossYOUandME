package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Decades;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Another_DecadeRenderer extends GeoEntityRenderer<Another_Decade> {

    public Another_DecadeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Another_DecadeModel());
        this.shadowRadius = 0.5F;
    }
}
