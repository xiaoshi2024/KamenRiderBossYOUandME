package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.evilbats.armor;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.evilbats.EvilBats;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class EvilBatsArmorRenderer extends GeoArmorRenderer<EvilBats> {
    public EvilBatsArmorRenderer() {
        super(new EvilBatsModel());
    }

    @Override
    public RenderType getRenderType(EvilBats animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}