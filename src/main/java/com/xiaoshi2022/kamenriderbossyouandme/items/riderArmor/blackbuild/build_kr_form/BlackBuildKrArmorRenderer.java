package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.build_kr_form;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.BlackBuildKr;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class BlackBuildKrArmorRenderer extends GeoArmorRenderer<BlackBuildKr> {
    public BlackBuildKrArmorRenderer() {
        super(new BlackBuildKrModel());
    }

    @Override
    public RenderType getRenderType(BlackBuildKr animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}