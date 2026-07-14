package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.hazardtrigger;

import com.xiaoshi2022.kamenriderbossyouandme.items.prop.HazardTrigger;
import com.xiaoshi2022.kamenriderbossyouandme.client.model.item.hazardtrigger.HazardTriggerModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HazardTriggerRenderer extends GeoItemRenderer<HazardTrigger> {

    public HazardTriggerRenderer() {
        super(new HazardTriggerModel());
    }
}