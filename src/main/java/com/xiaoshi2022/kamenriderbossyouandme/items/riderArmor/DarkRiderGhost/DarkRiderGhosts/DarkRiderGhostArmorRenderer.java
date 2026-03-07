package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost.DarkRiderGhosts;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost.DarkRiderGhost;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DarkRiderGhostArmorRenderer extends GeoArmorRenderer<DarkRiderGhost> {
    public DarkRiderGhostArmorRenderer() {
        super(new DarkRiderGhostModel());
    }

    @Override
    public RenderType getRenderType(DarkRiderGhost animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}