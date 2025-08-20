package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.PeachEnergy;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.peach_energy;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PeachEnergyRenderer extends GeoItemRenderer<peach_energy> {
    public PeachEnergyRenderer() {
        super(new PeachEnergyModel());
    }
}