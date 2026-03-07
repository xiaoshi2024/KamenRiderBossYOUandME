package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.marika.marikas;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.marika.Marika;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MarikaArmorRenderer extends GeoArmorRenderer<Marika> {
    public MarikaArmorRenderer() {
        super(new MarikaModel());
    }

    @Override
    public RenderType getRenderType(Marika animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}