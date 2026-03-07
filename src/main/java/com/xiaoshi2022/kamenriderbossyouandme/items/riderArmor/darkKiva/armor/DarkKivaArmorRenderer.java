package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.darkKiva.armor;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.darkKiva.DarkKiva;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DarkKivaArmorRenderer extends GeoArmorRenderer<DarkKiva> {
    public DarkKivaArmorRenderer() {
        super(new DarkKivaArmorModel());
    }

    @Override
    public RenderType getRenderType(DarkKiva animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}