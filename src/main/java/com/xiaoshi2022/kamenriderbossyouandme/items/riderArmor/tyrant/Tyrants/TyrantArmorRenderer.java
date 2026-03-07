package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrants;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrant;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class TyrantArmorRenderer extends GeoArmorRenderer<Tyrant> {


    public TyrantArmorRenderer() {
        super(new TyrantModel());
        this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
        this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
        this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
        this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
        this.rightLeg = new GeoBone(null, "armorRightLeg", false, (double) 0, false, false);
        this.leftLeg = new GeoBone(null, "armorLeftLeg", false, (double) 0, false, false);
        this.rightBoot = new GeoBone(null, "armorRightBoot", false, (double) 0, false, false);
        this.leftBoot = new GeoBone(null, "armorLeftBoot", false, (double) 0, false, false);
    }


}