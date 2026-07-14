package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.cobra;

import com.xiaoshi2022.kamenriderbossyouandme.client.model.item.cobra.CobraModel;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.Cobra;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CobraRenderer extends GeoItemRenderer<Cobra> {

    public CobraRenderer() {
        super(new CobraModel());
    }
}