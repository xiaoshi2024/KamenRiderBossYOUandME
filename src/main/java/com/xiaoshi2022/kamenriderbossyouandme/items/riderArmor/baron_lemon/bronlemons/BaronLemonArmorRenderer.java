package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.baron_lemon.bronlemons;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.baron_lemon.BaronLemon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class BaronLemonArmorRenderer extends GeoArmorRenderer<BaronLemon> {
    public BaronLemonArmorRenderer() {
        super(new BaronLemonModel());
    }

    @Override
    public RenderType getRenderType(BaronLemon animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}