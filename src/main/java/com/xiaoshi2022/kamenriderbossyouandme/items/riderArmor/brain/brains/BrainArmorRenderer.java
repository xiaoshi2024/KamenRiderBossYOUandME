package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.brain.brains;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.brain.Brain;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class BrainArmorRenderer extends GeoArmorRenderer<Brain> {
    public BrainArmorRenderer() {
        super(new BrainModel());
    }

    @Override
    public RenderType getRenderType(Brain animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}