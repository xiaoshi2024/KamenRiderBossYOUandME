package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.duke.dukes;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.duke.Duke;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DukeArmorRenderer extends GeoArmorRenderer<Duke> {
    public DukeArmorRenderer() {
        super(new DukeModel());
    }

    @Override
    public RenderType getRenderType(Duke animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}