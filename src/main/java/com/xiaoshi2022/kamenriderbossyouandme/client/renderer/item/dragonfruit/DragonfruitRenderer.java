package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.dragonfruit;

import com.xiaoshi2022.kamenriderbossyouandme.items.Dragonfruit;
import com.xiaoshi2022.kamenriderbossyouandme.client.model.item.dragonfruit.DragonfruitModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DragonfruitRenderer extends GeoItemRenderer<Dragonfruit> {
    public DragonfruitRenderer() {
        super(new DragonfruitModel());
    }
}