package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.KillProcess;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.KillProcessItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KillProcessModel extends GeoModel<KillProcessItem> {
    @Override
    public ResourceLocation getModelResource(KillProcessItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/kill_process.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KillProcessItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/kill_process.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KillProcessItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/kill_process.animation.json");
    }
}