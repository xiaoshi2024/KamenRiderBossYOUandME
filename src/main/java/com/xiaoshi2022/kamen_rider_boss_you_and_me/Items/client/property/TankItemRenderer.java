package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.property;

import software.bernie.geckolib.renderer.GeoItemRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.TankItem;

public class TankItemRenderer extends GeoItemRenderer<TankItem> {
    public TankItemRenderer() {
        super(new TankItemModel());
    }
}