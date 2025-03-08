package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ElementaryInvesHelheimRenderer extends GeoEntityRenderer<ElementaryInvesHelheim> {
    private int currentTick;

    public ElementaryInvesHelheimRenderer(EntityRendererProvider.Context context) {
        super(context, new ElementaryInvesHelheimModel());
        this.shadowRadius = 0.5f;
        //  可以添加发光纹理
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ElementaryInvesHelheim animatable) {
        String variant = animatable.getVariant();
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/inves_heilehim_" + variant + ".png");
    }

    // 渲染时在耳朵周围添加一些粒子
    @Override
    public void renderFinal(PoseStack poseStack, ElementaryInvesHelheim animatable, BakedGeoModel model, MultiBufferSource
    bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            // Find the earbone and use it as the point of reference
            this.model.getBone("leftear").ifPresent(ear -> {
                RandomSource rand = animatable.getRandom();
                Vector3d earPos = ear.getWorldPosition();

                animatable.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL,
                        earPos.x(),
                        earPos.y(),
                        earPos.z(),
                        rand.nextDouble() - 0.5D,
                        -rand.nextDouble(),
                        rand.nextDouble() - 0.5D);
            });
        }

        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
