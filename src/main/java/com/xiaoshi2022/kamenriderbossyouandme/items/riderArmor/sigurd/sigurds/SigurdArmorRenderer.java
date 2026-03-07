package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.sigurd.sigurds;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.sigurd.Sigurd;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class SigurdArmorRenderer extends GeoArmorRenderer<Sigurd> {
    public SigurdArmorRenderer() {
        super(new SigurdModel());
    }

    @Override
    public RenderType getRenderType(Sigurd animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}