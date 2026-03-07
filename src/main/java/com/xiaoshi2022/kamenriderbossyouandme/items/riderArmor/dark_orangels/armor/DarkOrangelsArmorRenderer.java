package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.dark_orangels.armor;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.dark_orangels.DarkOrangels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DarkOrangelsArmorRenderer extends GeoArmorRenderer<DarkOrangels> {
    public DarkOrangelsArmorRenderer() {
        super(new DarkOrangelsModel());
    }

    @Override
    public RenderType getRenderType(DarkOrangels animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}