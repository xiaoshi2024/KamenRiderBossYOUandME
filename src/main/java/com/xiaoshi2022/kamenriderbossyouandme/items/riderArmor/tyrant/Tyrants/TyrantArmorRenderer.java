package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrants;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrant;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class TyrantArmorRenderer extends GeoArmorRenderer<Tyrant> {

    public TyrantArmorRenderer() {
        super(new TyrantModel());
        this.head      = new GeoBone(null, "armorHead",      false, 0D, false, false);
        this.body      = new GeoBone(null, "armorBody",      false, 0D, false, false);
        this.rightArm  = new GeoBone(null, "armorRightArm",  false, 0D, false, false);
        this.leftArm   = new GeoBone(null, "armorLeftArm",   false, 0D, false, false);
        this.rightLeg  = new GeoBone(null, "armorRightLeg",  false, 0D, false, false);
        this.leftLeg   = new GeoBone(null, "armorLeftLeg",   false, 0D, false, false);
        this.rightBoot = new GeoBone(null, "armorRightBoot", false, 0D, false, false);
        this.leftBoot  = new GeoBone(null, "armorLeftBoot",  false, 0D, false, false);
    }
}
