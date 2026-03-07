package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.builds;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.BlackBuild;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class BlackBuildArmorRenderer extends GeoArmorRenderer<BlackBuild> {
    public BlackBuildArmorRenderer() {
        super(new BlackBuildModel());
    }

    @Override
    public RenderType getRenderType(BlackBuild animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}